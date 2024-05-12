package com.zayneiacplugs.zaynemdps;

import net.runelite.api.NPC;
import net.runelite.api.coords.LocalPoint;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class TileMap {
    private final ZayneMDPSConfig config;
    private final Map<LocalPoint, TargetTile> tileMap = new HashMap<>();
    private boolean upToDate = false;

    @Inject
    public TileMap(ZayneMDPSConfig config) {
        this.config = config;
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

    public void clean() {
        this.upToDate(false);
        this.getMap().clear();
    }
}
