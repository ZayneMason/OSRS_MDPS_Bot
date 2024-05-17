package com.zayneiacplugs.zaynemdps;

import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.client.eventbus.Subscribe;
import net.unethicalite.api.utils.MessageUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileMap {
    private final ZayneMDPSConfig config;
    private Map<LocalPoint, TargetTile> tileMap = new HashMap<>();
    private boolean upToDate = false;

    @Inject
    private Client client;
    private State state;

    @Inject
    public TileMap(State state) {
        this.config = state.config;
    }

    public void addTile(LocalPoint localPoint, EnhancedNPC npc, ZayneMDPSConfig.Option damageType, boolean inLoS) {
        if (!tileMap.containsKey(localPoint)) {
            tileMap.put(localPoint, new TargetTile(localPoint, npc, damageType, inLoS, this.config));
        } else {
            tileMap.get(localPoint).addNPC(npc);
            tileMap.get(localPoint).addDamageType(damageType);
            tileMap.get(localPoint).setInLoS(inLoS);
        }
    }

    public Map<LocalPoint, TargetTile> getMap() {
        return tileMap;
    }

    public void upToDate(boolean upToDate) {
        this.upToDate = upToDate;
    }

    public boolean isUpToDate() {
        return upToDate;
    }

    public void clean(List<LocalPoint> playerTiles) {
        try {
            for (LocalPoint localPoint : this.getMap().keySet()) {
                if (!playerTiles.contains(localPoint)) {
                    tileMap.remove(localPoint);
                } else {
                    tileMap.get(localPoint).clear();
                }
            }
            upToDate = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fillMap(Client client, List<LocalPoint> playerTiles) {
        for (LocalPoint playTile : playerTiles) {
            addTile(playTile, config);
        }
    }

    private void addTile(LocalPoint playTile, ZayneMDPSConfig config) {
        if (!tileMap.containsKey(playTile)) {
            tileMap.put(playTile, new TargetTile(playTile, this.config));
        }
    }

    public TileMap clone(State state) {
        TileMap mapCopy = new TileMap(state);
        for (LocalPoint localPoint : getMap().keySet()) {
            mapCopy.getMap()
                    .put(
                            localPoint,
                            new TargetTile(localPoint,
                                    state.config,
                                    (ArrayList<EnhancedNPC>) getMap().get(localPoint).getNPCs().clone(),
                                    (ArrayList<ZayneMDPSConfig.Option>) getMap().get(localPoint).getDamageTypes().clone(),
                                    getMap().get(localPoint).getInLoS()
                            )
                    );
        }
        return mapCopy;
    }

    public void stateInitialized(State state) {
        try {
            this.clean(state.playerTiles);
            this.fillMap(state.client, state.playerTiles);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stateProcessing(State state) {
        try {
            fillMap(state.client, state.playerTiles);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stateUpdated(State state) {
        try {
            clean(state.playerTiles);
            this.state = state;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onGraphicsObjectCreated(GraphicsObjectCreated event) {
        LocalPoint localPoint = event.getGraphicsObject().getLocation();
        if (state.playerTiles.contains(localPoint)) {
            addTile(localPoint, null, ZayneMDPSConfig.Option.SPOT_ANIM, false);
        }
    }
}
