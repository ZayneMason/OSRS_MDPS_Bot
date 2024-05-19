package com.zayneiacplugs.zaynemdps;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TileMap {
    private final Map<LocalPoint, TargetTile> tiles = new HashMap<>();
    private boolean upToDate = false;

    @Inject
    private Client client;
    private State state;

    @Inject
    public TileMap() {
        this.upToDate = false;
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
            tiles.get(localPoint).addAttackInfo(npc.getUniqueId(), npc.getTicksUntilAttack(), npc.npcConfig.getAttackStyle());
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
        TileMap copy = new TileMap();
        for (LocalPoint localPoint : tiles.keySet()) {
            copy.tiles.put(localPoint, this.getTile(localPoint));
        }
        return copy;
    }

    public void stateUpdated(State state) {

    }

    public Set<ZayneMDPSConfig.Option> getDistinctAttackStyles() {
        Set<ZayneMDPSConfig.Option> distinctAttackStyles = new HashSet<>();
        for (TargetTile targetTile : tiles.values()) {
            for (AttackInfo attackInfo : targetTile.getAttackInfos()) {
                distinctAttackStyles.add(attackInfo.getAttackType());
            }
        }
        return distinctAttackStyles;
    }
}
