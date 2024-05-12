package com.zayneiacplugs.zaynemdps;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.utils.MessageUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class NPCHandler {
    private final ZayneMDPSConfig config;
    public volatile List<NPCConfig> cachedNPCConfigs;
    private volatile List<NPC> cachedNPCs;
    private volatile List<EnhancedNPC> enhancedNPCS;

    @Inject
    public NPCHandler(ZayneMDPSConfig config) {
        this.config = config;
        this.enhancedNPCS = new ArrayList<>();
        this.cachedNPCConfigs = parseNPCConfig(config);
    }

    public void updateNPCs() {
        List<EnhancedNPC> compareList = generateEnhancedNPCList(false);
        for (EnhancedNPC newNPC : compareList){
            if (!getEnhancedNPCIds(enhancedNPCS).contains(newNPC.getUniqueId())) enhancedNPCS.add(newNPC);
        }
        for (EnhancedNPC enhancedNPC : enhancedNPCS) {
            if (!getEnhancedNPCIds(compareList).contains(enhancedNPC.getUniqueId())) {
                enhancedNPCS.remove(enhancedNPC);
                continue;
            }
            enhancedNPC.updateLocation();
            //enhancedNPC.updateHitpoints();
        }
    }

    private List<Integer> getEnhancedNPCIds(List<EnhancedNPC> enhancedNPCList) {
        List<Integer> idList = new ArrayList<>();
        for (EnhancedNPC enhancedNPC : enhancedNPCList) {
            idList.add(enhancedNPC.getUniqueId());
        }
        return idList;
    }

    private List<EnhancedNPC> generateEnhancedNPCList(boolean getStats) {
        List<EnhancedNPC> enhancedNPCSList = new ArrayList<>();
        for (NPCConfig npcConfig : cachedNPCConfigs) {
            for (NPC npc : NPCs.getAll(npcConfig.getName())) {
                enhancedNPCSList.add(new EnhancedNPC(npc, npcConfig, getStats));
            }
        }
        return enhancedNPCSList;
    }

    public void updateNPCConfig(List<NPCConfig> npcConfigs) {
        if (cachedNPCConfigs != npcConfigs) {
            cachedNPCConfigs = npcConfigs;
        }
    }

    public List<NPC> getCachedNPCs() {
        return cachedNPCs;
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

    public void handleNPCPosition(EnhancedNPC npc, TileMap tileMap, Client client) {
        WorldArea npcArea = npc.getNpc().getWorldArea();
        if (npcArea == null) MessageUtils.addMessage("Null npc area");
        int attackRange = npc.getNpcConfig().getRange();
        WorldArea extendedArea = new WorldArea(
                npcArea.getX() - config.overlayRange(),
                npcArea.getY() - config.overlayRange(),
                npcArea.getWidth() + 2 * config.overlayRange(),
                npcArea.getHeight() + 2 * config.overlayRange(),
                npcArea.getPlane()
        );

        for (WorldPoint worldPoint : extendedArea.toWorldPointList()) {
            WorldPoint point = new WorldPoint(worldPoint.getX(), worldPoint.getY(), npcArea.getPlane());
            LocalPoint localPoint = LocalPoint.fromWorld(client, point);
            boolean flagHasLineOfSight = ZayneUtils.hasLineOfSight(npcArea.toWorldPoint(), point, client);
            boolean tileIsUnderNPC = point.isInArea2D(npcArea);

            if (tileIsUnderNPC) {
                // tiles under npc are out of los
                tileMap.addTile(localPoint, npc, ZayneMDPSConfig.Option.OUT_OF_RANGE_IN_LOS, false);
                continue;
            }
            if (localPoint != null && flagHasLineOfSight) {
                double distance = calculateDistance(npcArea, point, npc.getNpcConfig().getAttackStyle());
                if (distance <= attackRange) {
                    // In attack range in los, attack style and los
                    tileMap.addTile(localPoint, npc, npc.getNpcConfig().getAttackStyle(), true);
                } else {
                    // has los, not in attack range
                    tileMap.addTile(localPoint, npc, ZayneMDPSConfig.Option.OUT_OF_RANGE_IN_LOS, true);
                }
            } else {
                // out of los
                tileMap.addTile(localPoint, npc, ZayneMDPSConfig.Option.OUT_OF_RANGE_OUT_LOS, false);
            }
        }
        tileMap.upToDate(true);
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

    public void process(Client client, ZayneMDPSConfig config, TileMap tileMap) {
        this.updateNPCConfig(parseNPCConfig(config));
        this.updateNPCs();
        for (EnhancedNPC enhancedNPC : enhancedNPCS) handleNPCPosition(enhancedNPC, tileMap, client);
    }

    private List<NPCConfig> parseNPCConfig(ZayneMDPSConfig config) {
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
}
