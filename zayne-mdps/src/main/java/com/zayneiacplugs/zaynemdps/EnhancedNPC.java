package com.zayneiacplugs.zaynemdps;

import net.runelite.api.NPC;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.game.NpcInfo;
import net.unethicalite.api.utils.MessageUtils;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

public class EnhancedNPC extends GameTick {
    private final int uniqueId;
    private final NPC npc;
    private final NPCConfig npcConfig;
    private LocalPoint location;
    private int health = 0;
    private int ticksUntilAttack;
    private MonsterStats monsterStats;

    public EnhancedNPC(NPC npc, NPCConfig npcConfig, boolean getStats, int id) {
        this.npc = npc;
        this.location = npc.getLocalLocation();
        this.npcConfig = npcConfig;
        this.uniqueId = id;
        this.health = 1;
        this.ticksUntilAttack = 10;  // Example default value
        this.monsterStats = npcConfig.getMonsterStats();
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
        if (npc.getLocalLocation() != location){
            location = npc.getLocalLocation();
        }
    }

    public MonsterStats getMonsterStats(){
        return monsterStats;
    }

//    public void updateHitpoints() {
//
//        this.health = npc.getHealthRatio() / health;
//    }
}

