package com.zayneiacplugs.zaynemdps;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import java.util.HashMap;

public class EnhancedNPC extends GameTick {
        private final boolean interacting;
        private final int uniqueId;
        private final NPC npc;
        final NPCConfig npcConfig;
        private final int attackSpeed;
    @Inject
        private Client client;
        private LocalPoint location;
        private int health = 0;
        int ticksUntilAttack;
        private MonsterStats monsterStats;
        private HashMap<WorldPoint, Integer> safeSpotCache = new HashMap<>();
        private ZayneMDPSConfig.Option nextAttack;

        public EnhancedNPC(NPC npc, NPCConfig npcConfig, boolean getStats, int id) {
            this.npc = npc;
            this.location = npc.getLocalLocation();
            this.npcConfig = npcConfig;
            this.uniqueId = id;
            this.health = 1;
            this.ticksUntilAttack = 0;  // Example default value
            this.monsterStats = npcConfig.getMonsterStats();
            this.attackSpeed = monsterStats.getAttackSpeed();
            this.interacting = npc.isInteracting();
            this.nextAttack = npcConfig.getAttackStyle();
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

        public void updateNextAttack(ZayneMDPSConfig.Option nextAttack, int ticks) {
            this.nextAttack = nextAttack;
            setTicksUntilAttack(ticks);
        }

        public void updateAttack(boolean canAttack) {
            if (canAttack) {
                if (ticksUntilAttack > 0) {
                    ticksUntilAttack--;
                } else if (ticksUntilAttack == 0) {
                    updateNextAttack(nextAttack, attackSpeed);
                }
            } else {
                if (ticksUntilAttack > 0) {
                    ticksUntilAttack--;
                }
            }
        }

    public void update(boolean canAttack) {
            updateLocation();
            updateAttack(canAttack);
    }

    public void resetTicksUntilAttack() {
            setTicksUntilAttack(attackSpeed);
    }
}

