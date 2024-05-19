package com.zayneiacplugs.zaynemdps;

import net.runelite.api.AnimationID;
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
    private final String configInfo;
    private final CopyOnWriteArrayList<EnhancedNPC> enhancedNPCS;
    public volatile List<NPCConfig> cachedNPCConfigs;
    @Inject
    private ZayneMDPSConfig config;
    private volatile WorldArea playerArea;
    private volatile int npcCounter;
    private Client client;

    @Inject
    public NPCHandler(ZayneMDPSConfig config) {
        this.config = config;
        this.enhancedNPCS = new CopyOnWriteArrayList<>();
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
        for (EnhancedNPC npc : enhancedNPCS) {
            npc.updateLocation();
            npc.updateAttack(npc.getNpc().getAnimation() != AnimationID.IDLE);
        }
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
            if (NPCs.getAll(npcConfig.getName()).isEmpty()) continue;
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
        if (npcArea == null) {
            MessageUtils.addMessage("Null npc area");
            return;
        }

        int attackRange = npc.getNpcConfig().getRange();
        ZayneMDPSConfig.Option attackStyle = npc.getNpcConfig().getAttackStyle();

        for (LocalPoint playerLocalPoint : playerTiles) {
            WorldPoint playerWorldPoint = WorldPoint.fromLocal(client, playerLocalPoint);
            boolean hasLineOfSight = ZayneUtils.hasLineOfSight(npcArea.toWorldPoint(), playerWorldPoint, client);

            double distance = calculateDistance(npcArea, playerLocalPoint, attackStyle);
            boolean isInRange = distance <= attackRange;
            boolean isUnderNPC = playerWorldPoint.isInArea2D(npcArea);

            ZayneMDPSConfig.Option attackStatus;
            int ticksUntilAttack = npc.getTicksUntilAttack();
            if (!hasLineOfSight) {
                attackStatus = ZayneMDPSConfig.Option.OUT_OF_RANGE_OUT_LOS;
            } else {
                if (isUnderNPC) {
                    attackStatus = ZayneMDPSConfig.Option.OUT_OF_RANGE_IN_LOS;
                } else {
                    if (!isInRange) {
                        attackStatus = ZayneMDPSConfig.Option.OUT_OF_RANGE_IN_LOS;
                    } else {
                        attackStatus = attackStyle;
                    }
                }
            }
            tileMap.addOrUpdateTile(playerLocalPoint, npc.getNpc().getId(), ticksUntilAttack, attackStatus);
        }
    }


    private double calculateDistance(WorldArea npcArea, LocalPoint targetPoint, ZayneMDPSConfig.Option attackStyle) {
        double minDistance = Double.MAX_VALUE;
        WorldPoint targetWorldPoint = WorldPoint.fromLocal(client, targetPoint);
        for (int x = npcArea.getX(); x < npcArea.getX() + npcArea.getWidth(); x++) {
            for (int y = npcArea.getY(); y < npcArea.getY() + npcArea.getHeight(); y++) {
                double currentDistance = 0;
                switch (attackStyle) {
                    case MELEE:
                    case WARBAND_MAGE:
                    case WARBAND_RANGE:
                        int dx = Math.abs(x - targetWorldPoint.getX());
                        int dy = Math.abs(y - targetWorldPoint.getY());
                        if (dx != 0 && dy != 0) {
                            continue; // Skip diagonals
                        }
                        currentDistance = dx + dy;
                    case MAGE:
                    case RANGE:
                    case SPECIAL_MELEE:
                        currentDistance = Math.max(Math.abs(x - targetWorldPoint.getX()), Math.abs(y - targetWorldPoint.getY()));
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
            tileMap.upToDate(true);
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

    public void updateLocations() {
        enhancedNPCS.forEach(EnhancedNPC::updateLocation);
    }

    public void stateInitialized(State state) {
        try {
            this.client = state.client;
            this.cachedNPCConfigs = parseNPCConfig(config);
            process(state.client, state.config, state.tileMap, state.playerTiles);
            MessageUtils.addMessage("NPCHandler state initialized.");
        } catch (Exception e) {
            System.err.println(e);
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
