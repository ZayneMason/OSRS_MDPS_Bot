package com.zayneiacplugs.zaynemdps;

import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TargetTile {
    public LocalPoint localPoint;
    @Inject
    private Client client;
    private Set<AttackInfo> attackInfos;

    public TargetTile(LocalPoint localPoint, Client client) {
        this.attackInfos = new HashSet<>();
        this.localPoint = localPoint;
        this.client = client;
    }

    public Set<AttackInfo> getAttackInfos() {
        return attackInfos;
    }

    public Set<ZayneMDPSConfig.Option> getAttackStyles() {
        Set<ZayneMDPSConfig.Option> styles = new HashSet<>();
        for (AttackInfo attackInfo : getAttackInfos()) {
            styles.add(attackInfo.attackType);
        }
        return styles;
    }

    public void addAttackInfo(int npcId, int ticksUntilAttack, ZayneMDPSConfig.Option attackType) {
        attackInfos.add(new AttackInfo(npcId, ticksUntilAttack, attackType));
    }

    public boolean hasOverlappingAttackStyles() {
        Map<Integer, Set<ZayneMDPSConfig.Option>> ticksToAttackStyles = new HashMap<>();

        for (AttackInfo attackInfo : getAttackInfos()) {
            int ticksUntilAttack = attackInfo.getTicksUntilAttack();
            ZayneMDPSConfig.Option attackStyle = attackInfo.getAttackType();

            ticksToAttackStyles
                    .computeIfAbsent(ticksUntilAttack, k -> new HashSet<>())
                    .add(attackStyle);
        }

        for (Set<ZayneMDPSConfig.Option> attackStyles : ticksToAttackStyles.values()) {
            if (attackStyles.size() > 1) {
                return true; // There are overlapping attack styles
            }
        }

        return false; // No overlapping attack styles
    }

    @Override
    public String toString() {
        return "TargetTile{" +
                "attackInfos=" + attackInfos +
                '}';
    }
}
