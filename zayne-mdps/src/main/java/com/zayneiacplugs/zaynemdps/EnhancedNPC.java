package com.zayneiacplugs.zaynemdps;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;

import javax.inject.Inject;
import java.util.HashMap;

public class EnhancedNPC extends GameTick {
    final NPCConfig npcConfig;
    private final boolean interacting;
    private final int uniqueId;
    private final NPC npc;
    private final int attackSpeed;
    private final int range;
    int ticksUntilAttack;
    @Inject
    private Client client;
    private LocalPoint location;
    private int health = 0;
    private MonsterStats monsterStats;
    private HashMap<WorldPoint, Integer> safeSpotCache = new HashMap<>();
    private ZayneMDPSConfig.Option nextAttack;

    public EnhancedNPC(NPC npc, NPCConfig npcConfig, boolean getStats, int id) {
        this.npc = npc;
        this.location = npc.getLocalLocation();
        this.npcConfig = npcConfig;
        this.uniqueId = id;
        this.health = 1;
        this.ticksUntilAttack = 1;  // Example default value
        this.monsterStats = npcConfig.getMonsterStats();
        this.attackSpeed = monsterStats.getAttackSpeed();
        this.interacting = npc.isInteracting();
        this.nextAttack = npcConfig.getAttackStyle();
        this.range = npcConfig.getRange();
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public NPC getNpc() {
        return npc;
    }

    public NPCConfig getNpcConfig() {
        return npcConfig;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getTicksUntilAttack() {
        return ticksUntilAttack;
    }

    public void setTicksUntilAttack(int ticks) {
        this.ticksUntilAttack = ticks;
    }

    public void updateLocation() {
        if (npc.getLocalLocation() != location) {
            location = npc.getLocalLocation();
        }
    }

    public MonsterStats getMonsterStats() {
        return monsterStats;
    }

    public void updateNextAttack(int ticks) {
        setTicksUntilAttack(ticks);
    }

    public void updateAttack(boolean attacked) {
        if (ticksUntilAttack > 0) {
            ticksUntilAttack--;
        } else if (ticksUntilAttack == 0) {
            if (attacked) {
                resetTicksUntilAttack();
            }
        }
        if (attacked) {
            resetTicksUntilAttack();
        }
    }

    public void resetTicksUntilAttack() {
        this.ticksUntilAttack = attackSpeed;
    }
}

