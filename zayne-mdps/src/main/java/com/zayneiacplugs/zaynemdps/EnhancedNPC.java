package com.zayneiacplugs.zaynemdps;

import net.runelite.api.NPC;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.game.NpcInfo;

public class EnhancedNPC extends NpcInfo {
    private LocalPoint location;
    private int uniqueId;
    private int health = 0;
    private int ticksUntilAttack;
    private MonsterStats monsterStats;
    private NPC npc;
    private NPCConfig npcConfig;

    public EnhancedNPC(NPC npc, NPCConfig npcConfig, boolean getStats) {
        super();
        this.npc = npc;
        this.location = npc.getLocalLocation();
        this.npcConfig = npcConfig;
        this.uniqueId = npc.getIndex();
        if (getStats) this.health = npcConfig.getMonsterStats().getHitpoints();
        this.ticksUntilAttack = 10;  // Example default value
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
        this.location = npc.getLocalLocation();
    }

//    public void updateHitpoints() {
//
//        this.health = npc.getHealthRatio() / health;
//    }
}

