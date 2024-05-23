package com.zayneiacplugs.zaynemdps;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import java.util.*;

public class TileMap {
    private final Map<LocalPoint, TargetTile> tiles = new HashMap<>();
    private boolean upToDate = false;

    @Inject
    private Client client;
    private State state;

    @Inject
    public TileMap(Client client) {
        this.upToDate = false;
        this.client = client;
    }

    public TargetTile getTile(LocalPoint point) {
        return tiles.get(point);
    }

    public void addOrUpdateTile(LocalPoint point, int npcId, int ticksUntilAttack, ZayneMDPSConfig.Option attackType) {
        if (tiles.containsKey(point)) {
            tiles.get(point).addAttackInfo(npcId, ticksUntilAttack, attackType);
        } else {
            tiles.put(point, new TargetTile(point, client));
            tiles.get(point).addAttackInfo(npcId, ticksUntilAttack, attackType);
        }
    }

    public void addTile(LocalPoint localPoint, EnhancedNPC npc) {
        if (!tiles.containsKey(localPoint)) {
            tiles.put(localPoint, new TargetTile(localPoint, client));
            tiles.get(localPoint).addAttackInfo(0, 0, ZayneMDPSConfig.Option.OUT_OF_RANGE_OUT_LOS);
        } else {
            tiles.get(localPoint).addAttackInfo(npc.getUniqueId(), npc.getTicksUntilAttack(), npc.getNpcConfig().getAttackStyle());
        }
    }

    public void clearTiles() {
        tiles.clear();
    }

    public Map<LocalPoint, TargetTile> getAllTiles() {
        return tiles;
    }

    @Override
    public String toString() {
        return "TileMap{" +
                "tiles=" + tiles +
                '}';
    }

    public void upToDate(boolean b) {
        this.upToDate = b;
    }

    public TileMap cloneTiles() {
        TileMap copy = new TileMap(client);
        for (LocalPoint localPoint : tiles.keySet()) {
            copy.tiles.put(localPoint, this.getTile(localPoint));
        }
        return copy;
    }

    public void stateUpdated(State state) {
        // Update the state if needed
    }

    public Map<ZayneMDPSConfig.Option, Integer> getDistinctAttackStyles() {
        Map<ZayneMDPSConfig.Option, Integer> distinctAttackStyles = new HashMap<>();
        if (tiles.isEmpty()){
            return distinctAttackStyles;
        }
        for (TargetTile targetTile : tiles.values()) {
            for (AttackInfo attackInfo : targetTile.getAttackInfos()) {
                distinctAttackStyles.put(attackInfo.getAttackType(), attackInfo.getTicksUntilAttack());
            }
        }
        return distinctAttackStyles;
    }

    public List<TargetTile> getAdjacentTiles(LocalPoint localLocation) {
        List<TargetTile> adjacentTiles = new ArrayList<>();
        if (localLocation == null) {
            return adjacentTiles; // Return empty list if localLocation is null
        }
        int baseX = localLocation.getX();
        int baseY = localLocation.getY();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue; // Skip the tile itself
                LocalPoint adjacentPoint = new LocalPoint(baseX + dx, baseY + dy);
                if (ZayneUtils.validTile(WorldPoint.fromLocal(client, adjacentPoint), client)) {
                    TargetTile adjacentTile = getTile(adjacentPoint);
                    if (adjacentTile != null) {
                        adjacentTiles.add(adjacentTile);
                    }
                }
            }
        }
        return adjacentTiles;
    }

    public List<TargetTile> getSafeTiles() {
        List<TargetTile> safeTiles = new ArrayList<>();
        for (TargetTile tile : tiles.values()) {
            if (!tile.hasOverlappingAttackStyles()) {
                safeTiles.add(tile);
            }
        }
        return safeTiles;
    }
}
