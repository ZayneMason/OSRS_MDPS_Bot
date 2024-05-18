package com.zayneiacplugs.zaynemdps;

public class AttackInfo {
    private final int npcId;
    private final int ticksUntilAttack;
    private final ZayneMDPSConfig.Option attackType;

    public AttackInfo(int npcId, int ticksUntilAttack, ZayneMDPSConfig.Option attackType) {
        this.npcId = npcId;
        this.ticksUntilAttack = ticksUntilAttack;
        this.attackType = attackType;
    }

    public int getNpcId() {
        return npcId;
    }

    public int getTicksUntilAttack() {
        return ticksUntilAttack;
    }

    public ZayneMDPSConfig.Option getAttackType() {
        return attackType;
    }

    @Override
    public String toString() {
        return "AttackInfo{" +
                "npcId=" + npcId +
                ", ticksUntilAttack=" + ticksUntilAttack +
                ", attackType='" + attackType + '\'' +
                '}';
    }
}
