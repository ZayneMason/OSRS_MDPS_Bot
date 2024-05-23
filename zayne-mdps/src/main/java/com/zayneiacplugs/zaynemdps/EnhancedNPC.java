package com.zayneiacplugs.zaynemdps;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.NpcUtil;


import javax.inject.Inject;
import java.util.HashMap;
public class EnhancedNPC extends GameTick {
    final NPCConfig npcConfig;
    private final boolean interacting;
    private final int uniqueId;
    private final NPC npc;
    private final int attackSpeed;
    private final int range;
    private final int priority;
    int ticksUntilAttack;
    private int lastAttackTick;
    @Inject
    private Client client;
    private LocalPoint location;
    private int health = 0;
    private MonsterStats monsterStats;
    private HashMap<WorldPoint, Integer> safeSpotCache = new HashMap<>();
    private ZayneMDPSConfig.Option nextAttack;
    private int lastAnimationId;
    private boolean isAttacking;

    public EnhancedNPC(NPC npc, NPCConfig npcConfig, boolean getStats, int id) {
        int priority1 = 0;
        this.npc = npc;
        this.location = npc.getLocalLocation();
        this.npcConfig = npcConfig;
        this.uniqueId = npc.getIndex();
        this.health = 1;
        this.ticksUntilAttack = 0;  // Example default value
        this.monsterStats = npcConfig.getMonsterStats();
        this.attackSpeed = monsterStats.getAttackSpeed();
        this.interacting = npc.isInteracting();
        this.nextAttack = npcConfig.getAttackStyle();
        this.range = npcConfig.getRange();
        this.lastAttackTick = 0;
        this.lastAnimationId = -1;

        switch (npcConfig.getName()){
            case "Minotaur":
                priority1 = 1;
            case "Fremennik warband berserker":
                priority1 = 2;
            case "Fremennik warband seer":
                priority1 = 3;
            case "Javelin Colossus":
                priority1 = 4;
            case "Manticore":
                priority1 = 5;
            case "Shockwave Colossus":
                priority1 = 6;
            case "Serpent shaman":
                priority1 = 7;
            case "Jaguar warrior":
                priority1 = 8;
            case "Fremennik warband archer":
                priority1 = 9;
        }
        this.priority = priority1;
    }

    public int getPriority() { return priority; }
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
        if (attacked) {
            this.resetTicksUntilAttack();
        }
    }


    public void resetTicksUntilAttack() {
        this.ticksUntilAttack = attackSpeed;
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public void setAttacking(boolean isAttacking) {
        this.isAttacking = isAttacking;
    }

    public int getLastAnimationId() {
        return lastAnimationId;
    }

    public void setLastAnimationId(int lastAnimationId) {
        this.lastAnimationId = lastAnimationId;
    }

    public void attackThisNPC(){
        this.getNpc().interact("Attack");
    }
}

