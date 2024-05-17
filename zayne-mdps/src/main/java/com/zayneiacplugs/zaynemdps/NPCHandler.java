package com.zayneiacplugs.zaynemdps;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.utils.MessageUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NPCHandler {
    private final ZayneMDPSConfig config;
    private final String configInfo;
    private final CopyOnWriteArrayList<EnhancedNPC> enhancedNPCS;
    private final State state;
    public volatile List<NPCConfig> cachedNPCConfigs;
    private volatile WorldArea playerArea;
    private volatile int npcCounter;

    @Inject
    public NPCHandler(State state) throws IOException {
        this.state = state;
        this.config = state.config;
        this.enhancedNPCS = new CopyOnWriteArrayList<>();
        this.cachedNPCConfigs = parseNPCConfig(config);
        this.configInfo = config.npcList();
        this.npcCounter = 0;
    }

    public void clearCache() throws IOException {
        // Clearing each cache by initializing it with a new collection
        this.cachedNPCConfigs.clear();
        this.enhancedNPCS.clear();

        // Optionally, re-initialize with default values or re-parse configurations if necessary
        if (configInfo != config.npcList()) this.cachedNPCConfigs = parseNPCConfig(config);

        // Log or message to indicate the cache has been cleared
        MessageUtils.addMessage("All NPC caches have been cleared.");
    }

    public void updateNPCs() {
        List<EnhancedNPC> newNPCs = generateEnhancedNPCList(false);
        if (newNPCs.isEmpty()) {
            enhancedNPCS.clear();
            return;
        }
        List<Integer> currentIds = getEnhancedNPCIds(enhancedNPCS);
        List<Integer> newIds = getEnhancedNPCIds(newNPCs);

        // Add new NPCs
        for (EnhancedNPC newNPC : newNPCs) {
            if (!currentIds.contains(newNPC.getUniqueId())) {
                enhancedNPCS.add(newNPC);
            }
        }
        // Remove missing NPCs
        enhancedNPCS.removeIf(npc -> !newIds.contains(npc.getUniqueId()));
        enhancedNPCS.forEach(EnhancedNPC::updateLocation);
    }

    private List<Integer> getEnhancedNPCIds(List<EnhancedNPC> enhancedNPCList) {
        List<Integer> idList = new ArrayList<>();
        if (!enhancedNPCList.isEmpty()) {
            for (EnhancedNPC enhancedNPC : enhancedNPCList) {
                idList.add(enhancedNPC.getUniqueId());
            }
        }
        return idList;
    }

    private List<EnhancedNPC> generateEnhancedNPCList(boolean getStats) {
        List<EnhancedNPC> enhancedNPCSList = new ArrayList<>();
        for (NPCConfig npcConfig : cachedNPCConfigs) {
            if (NPCs.getAll(npcConfig.getName()).isEmpty()) return enhancedNPCSList;
            for (NPC npc : NPCs.getAll(npcConfig.getName())) {
                if (npc == null) continue;
                enhancedNPCSList.add(new EnhancedNPC(npc, npcConfig, getStats, npcCounter));
                npcCounter++;
            }
        }
        return enhancedNPCSList;
    }

    public void updateNPCConfig(List<NPCConfig> npcConfigs) {
        if (cachedNPCConfigs != npcConfigs) {
            cachedNPCConfigs = npcConfigs;
        }
    }

    public NPCConfig getConfigForNPC(NPC npc) {
        for (NPCConfig npcConfiguration : cachedNPCConfigs) {
            if (npcConfiguration.getName().equalsIgnoreCase(npc.getName())) {
                return npcConfiguration;
            }
        }
        return null;
    }

    public List<NPCConfig> getCachedNPCConfigs() {
        return cachedNPCConfigs;
    }

    public void handleNPCPosition(EnhancedNPC npc, TileMap tileMap, Client client, List<LocalPoint> playerTiles) {
        if (npc == null) {
            return;
        }
        WorldArea npcArea = npc.getNpc().getWorldArea();
        if (npcArea == null) MessageUtils.addMessage("Null npc area");
        int attackRange = npc.getNpcConfig().getRange();


        for (LocalPoint playerLocalPoint : playerTiles) {
            WorldPoint point = WorldPoint.fromLocal(client, playerLocalPoint);
            boolean flagHasLineOfSight = ZayneUtils.hasLineOfSight(npcArea.toWorldPoint(), point, client);
            if (!flagHasLineOfSight) {
                tileMap.addTile(playerLocalPoint, npc, ZayneMDPSConfig.Option.OUT_OF_RANGE_OUT_LOS, false);
                continue;
            }
            boolean tileIsUnderNPC = point.isInArea2D(npcArea);
            if (tileIsUnderNPC) {
                // tiles under npc are out of los
                tileMap.addTile(playerLocalPoint, npc, ZayneMDPSConfig.Option.OUT_OF_RANGE_IN_LOS, false);
            } else {
                double distance = calculateDistance(npcArea, point, npc.getNpcConfig().getAttackStyle());
                if (distance <= attackRange) {
                    // In attack range in los, attack style and los
                    tileMap.addTile(playerLocalPoint, npc, npc.getNpcConfig().getAttackStyle(), true);
                } else {
                    // has los, not in attack range
                    tileMap.addTile(playerLocalPoint, npc, ZayneMDPSConfig.Option.OUT_OF_RANGE_IN_LOS, true);
                }
            }
        }
    }

    private double calculateDistance(WorldArea npcArea, WorldPoint targetPoint, ZayneMDPSConfig.Option attackStyle) {
        double minDistance = Double.MAX_VALUE;

        for (int x = npcArea.getX(); x < npcArea.getX() + npcArea.getWidth(); x++) {
            for (int y = npcArea.getY(); y < npcArea.getY() + npcArea.getHeight(); y++) {
                double currentDistance = 0;
                switch (attackStyle) {
                    case MELEE:
                    case WARBAND_MAGE:
                    case WARBAND_RANGE:
                        int dx = Math.abs(x - targetPoint.getX());
                        int dy = Math.abs(y - targetPoint.getY());
                        if (dx != 0 && dy != 0) {
                            continue; // Skip diagonals
                        }
                        currentDistance = dx + dy;
                    case MAGE:
                    case RANGE:
                    case SPECIAL_MELEE:
                        currentDistance = Math.max(Math.abs(x - targetPoint.getX()), Math.abs(y - targetPoint.getY()));
                }
                if (currentDistance < minDistance) {
                    minDistance = currentDistance;
                }
            }
        }
        return minDistance;
    }

    public List<EnhancedNPC> getEnhancedNPCS() {
        return enhancedNPCS;
    }

    public void process(Client client, ZayneMDPSConfig config, TileMap tileMap, List<LocalPoint> playerTiles) {
        this.updateNPCs();
        if (enhancedNPCS.isEmpty()) {
            return;
        }
        for (EnhancedNPC enhancedNPC : enhancedNPCS) {
            handleNPCPosition(enhancedNPC, tileMap, client, playerTiles);
        }
        tileMap.upToDate(true);
    }

    private List<NPCConfig> parseNPCConfig(ZayneMDPSConfig config) throws IOException {
        List<NPCConfig> configs = new ArrayList<>();
        String[] npcEntries = this.config.npcList().split(";");
        for (String entry : npcEntries) {
            String[] details = entry.split(",");
            if (details.length == 3) {
                String name = details[0].trim();
                String style = details[1].trim();
                int range;
                try {
                    range = Integer.parseInt(details[2].trim());
                } catch (NumberFormatException e) {
                    MessageUtils.addMessage("Invalid range: " + details[2]);
                    continue; // Skip this entry if the range is not a valid integer
                }
                configs.add(new NPCConfig(name, style, range, this.config));
            }
        }
        if (configs.isEmpty()) {
            MessageUtils.addMessage("Null configs");
        }
        return configs;
    }

    public void updateLocations(){
        enhancedNPCS.forEach(EnhancedNPC::updateLocation);
    }

    public void stateInitialized(State state) {
        try {
            process(state.client, state.config, state.tileMap, state.playerTiles);
            MessageUtils.addMessage("NPCHandler state initialized.");
        } catch (Exception e) {
            MessageUtils.addMessage("Error during NPCHandler state initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stateProcessing(State state) {
        try {
            process(state.client, state.config, state.tileMap, state.playerTiles);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
