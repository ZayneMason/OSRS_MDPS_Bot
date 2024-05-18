package com.zayneiacplugs.zaynemdps;

import com.zayneiacplugs.zaynemdps.AttackInfo;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class TargetTile {
    @Inject
    private Client client;
    private List<AttackInfo> attackInfos;
    WorldPoint worldPoint;

    public TargetTile(WorldPoint worldPoint, Client client) {
        this.attackInfos = new ArrayList<>();
        this.worldPoint = worldPoint;
        this.client = client;
    }

    public List<AttackInfo> getAttackInfos() {
        return attackInfos;
    }

    public void addAttackInfo(int npcId, int ticksUntilAttack, ZayneMDPSConfig.Option attackType) {
        attackInfos.add(new AttackInfo(npcId, ticksUntilAttack, attackType));
    }

    @Override
    public String toString() {
        return "TargetTile{" +
                "attackInfos=" + attackInfos +
                '}';
    }

    public LocalPoint getLocalPoint() {
        return LocalPoint.fromWorld(client, worldPoint);
    }
}