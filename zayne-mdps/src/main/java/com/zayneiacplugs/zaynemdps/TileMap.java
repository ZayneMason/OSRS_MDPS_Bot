package com.zayneiacplugs.zaynemdps;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.utils.MessageUtils;

import java.util.HashMap;
import java.util.Map;
public class TileMap {
    private Map<WorldPoint, TargetTile> tiles;
    private boolean upToDate = false;

    @Inject
    private Client client;
    private State state;

    @Inject
    public TileMap(State state) {
        this.tiles = new HashMap<>();
        this.upToDate(false);
    }

    public TargetTile getTile(WorldPoint point) {
        return tiles.get(point);
    }

    public void addOrUpdateTile(WorldPoint point, int npcId, int ticksUntilAttack, ZayneMDPSConfig.Option attackType) {
        if (tiles.containsKey(point)) {
            tiles.get(point).addAttackInfo(npcId, ticksUntilAttack, attackType);
        } else {
            tiles.put(point, new TargetTile(point, client));
            tiles.get(point).addAttackInfo(npcId, ticksUntilAttack, attackType);
        }
    }

    public void clearTiles() {
        tiles.clear();
    }

    public Map<WorldPoint, TargetTile> getAllTiles() {
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

    public TileMap cloneTiles(State state) {
        TileMap copy = new TileMap(state);
        for (WorldPoint worldPoint : tiles.keySet()){
            copy.tiles.put(worldPoint, this.getTile(worldPoint));
        }
        return copy;
    }

    public void stateProcessing(State state) {
        tiles.clear();
        for (WorldPoint worldPoint : state.getPlayerArea().toWorldPointList()){
            tiles.put(worldPoint, new TargetTile(worldPoint, client));
            tiles.get(worldPoint).addAttackInfo(0, -1, ZayneMDPSConfig.Option.OUT_OF_RANGE_OUT_LOS );
        }
    }

    public void stateInitialized(State state) {
        clearTiles();
        for (WorldPoint worldPoint : state.playerTiles){
            tiles.put(worldPoint, new TargetTile(worldPoint, client));
        }
        MessageUtils.addMessage("TileMap initialized");
    }

    public void stateUpdated(State state) {
    }
}
