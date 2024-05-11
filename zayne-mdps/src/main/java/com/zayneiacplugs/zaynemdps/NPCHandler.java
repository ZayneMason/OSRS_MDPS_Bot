package com.zayneiacplugs.zaynemdps;

import net.runelite.api.Client;
import net.runelite.api.CollisionDataFlag;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.groundmarkers.GroundMarkerOverlay;
import net.runelite.client.plugins.wiki.WikiPlugin;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NPCHandler {
    private volatile List<NPC> cachedNPCs = new ArrayList<>();
    private volatile List<NPCConfig> cachedNPCConfigs = new ArrayList<>();

    public void updateNPCs(java.util.List<NPC> npcs) {
        cachedNPCs = npcs;
    }

    public void updateNPCConfig(List<NPCConfig> npcConfigs) {
        cachedNPCConfigs = npcConfigs;
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

    public void handleNPCPosition(NPC npc, NPCConfig configNPC, TileMap tileMap, Client client) {
        WorldArea npcArea = npc.getWorldArea();
        int attackRange = configNPC.getRange();
        WorldArea extendedArea = new WorldArea(
                npcArea.getX() - configNPC.config().overlayRange(),
                npcArea.getY() - configNPC.config().overlayRange(),
                npcArea.getWidth() + 2 * configNPC.config().overlayRange(),
                npcArea.getHeight() + 2 * configNPC.config().overlayRange(),
                npcArea.getPlane()
        );

        for (WorldPoint worldPoint : extendedArea.toWorldPointList()) {
            WorldPoint point = new WorldPoint(worldPoint.getX(), worldPoint.getY(), npcArea.getPlane());
            LocalPoint localPoint = LocalPoint.fromWorld(client, point);
            boolean flagHasLineOfSight = hasLineOfSight(npcArea.toWorldPoint(), point, client);
            boolean tileIsUnderNPC = point.isInArea2D(npcArea);

            if (tileIsUnderNPC) {
                // tiles under npc are out of los
                tileMap.addTile(localPoint, npc, ZayneMDPSConfig.Option.OUT_OF_RANGE_IN_LOS, false);
                continue;
            }
            if (localPoint != null && flagHasLineOfSight) {
                double distance = calculateDistance(npcArea, point, configNPC.getAttackStyle());
                if (distance <= attackRange) {
                    // In attack range in los, attack style and los
                    tileMap.addTile(localPoint, npc, configNPC.getAttackStyle(), true);
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

    public boolean hasLineOfSight(WorldPoint from, WorldPoint to, Client client) {
        if (from.getPlane() != to.getPlane()) {
            return false;
        }

        List<net.runelite.api.Point> linePoints = bresenhamLine(from.getX(), from.getY(), to.getX(), to.getY());
        int plane = from.getPlane();
        int[][] flags = Objects.requireNonNull(client.getCollisionMaps())[plane].getFlags();

        for (net.runelite.api.Point p : linePoints) {
            int x = p.getX() - client.getBaseX();
            int y = p.getY() - client.getBaseY();

            if (x < 0 || y < 0 || x >= 104 || y >= 104) {
                continue;
            }

            int flag = flags[x][y];
            if (isObstacle(flag)) {
                return false;
            }
        }
        return true;
    }

    private boolean isObstacle(int flag) {
        return (flag & CollisionDataFlag.BLOCK_MOVEMENT_FULL) != 0 ||
                (flag & CollisionDataFlag.BLOCK_MOVEMENT_OBJECT) != 0 ||
                (flag & CollisionDataFlag.BLOCK_LINE_OF_SIGHT_FULL) != 0 ||
                (flag & CollisionDataFlag.BLOCK_LINE_OF_SIGHT_EAST) != 0 ||
                (flag & CollisionDataFlag.BLOCK_LINE_OF_SIGHT_WEST) != 0 ||
                (flag & CollisionDataFlag.BLOCK_LINE_OF_SIGHT_NORTH) != 0 ||
                (flag & CollisionDataFlag.BLOCK_LINE_OF_SIGHT_SOUTH) != 0;
    }

    private List<net.runelite.api.Point> bresenhamLine(int x0, int y0, int x1, int y1) {
        List<net.runelite.api.Point> points = new ArrayList<>();

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        int e2;

        while (true) {
            points.add(new Point(x0, y0));

            if (x0 == x1 && y0 == y1) {
                break;
            }

            e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }

        return points;
    }

    public void process(Client client, ZayneMDPSConfig config, TileMap tileMap) {
        List<NPC> npcs = new ArrayList<>();
        List<NPCConfig> npcConfigs = parseNPCConfig(config.npcList(), config);
        for (NPCConfig npcConfig : npcConfigs) {
            npcs.addAll(NPCs.getAll(npcConfig.getName()));
        }

        this.updateNPCs(npcs);
        this.updateNPCConfig(npcConfigs);
        for (NPC cachedNPC : cachedNPCs) handleNPCPosition(cachedNPC, getConfigForNPC(cachedNPC), tileMap, client);
    }

    public List<NPCConfig> parseNPCConfig(String configString, ZayneMDPSConfig config) {
        List<NPCConfig> configs = new ArrayList<>();
        String[] npcEntries = configString.split(";");
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
                configs.add(new NPCConfig(name, style, range, config));
            }
        }
        if (configs.isEmpty()) {
            MessageUtils.addMessage("Null configs");
        }
        return configs;
    }
}
