package com.zayneiacplugs.zaynemdps;

public class MonsterStats {
    private int npcId;
    private int combatLevel;
    private int hitpoints;
    private String attackStyles;  // Keeping this as String for simplicity
    private int attackSpeed;
    private int defenceLevel;
    private int stabDefenceBonus;
    private int slashDefenceBonus;
    private int crushDefenceBonus;
    private int magicLevel;
    private int magicDefenceBonus;
    private int rangeDefenceBonus;

    // Getters and Setters
    public int getNpcId() {
        return npcId;
    }

    public void setNpcId(int npcId) {
        this.npcId = npcId;
    }

    public int getCombatLevel() {
        return combatLevel;
    }

    public void setCombatLevel(int combatLevel) {
        this.combatLevel = combatLevel;
    }

    public int getHitpoints() {
        return hitpoints;
    }

    public void setHitpoints(int hitpoints) {
        this.hitpoints = hitpoints;
    }

    public String getAttackStyles() {
        return attackStyles;
    }

    public void setAttackStyles(String attackStyles) {
        this.attackStyles = attackStyles;
    }

    public int getAttackSpeed() {
        return attackSpeed;
    }

    public void setAttackSpeed(int attackSpeed) {
        this.attackSpeed = attackSpeed;
    }

    public int getDefenceLevel() {
        return defenceLevel;
    }

    public void setDefenceLevel(int defenceLevel) {
        this.defenceLevel = defenceLevel;
    }

    public int getStabDefenceBonus() {
        return stabDefenceBonus;
    }

    public void setStabDefenceBonus(int stabDefenceBonus) {
        this.stabDefenceBonus = stabDefenceBonus;
    }

    public int getSlashDefenceBonus() {
        return slashDefenceBonus;
    }

    public void setSlashDefenceBonus(int slashDefenceBonus) {
        this.slashDefenceBonus = slashDefenceBonus;
    }

    public int getCrushDefenceBonus() {
        return crushDefenceBonus;
    }

    public void setCrushDefenceBonus(int crushDefenceBonus) {
        this.crushDefenceBonus = crushDefenceBonus;
    }

    public int getMagicLevel() {
        return magicLevel;
    }

    public void setMagicLevel(int magicLevel) {
        this.magicLevel = magicLevel;
    }

    public int getMagicDefenceBonus() {
        return magicDefenceBonus;
    }

    public void setMagicDefenceBonus(int magicDefenceBonus) {
        this.magicDefenceBonus = magicDefenceBonus;
    }

    public int getRangeDefenceBonus() {
        return rangeDefenceBonus;
    }

    public void setRangeDefenceBonus(int rangeDefenceBonus) {
        this.rangeDefenceBonus = rangeDefenceBonus;
    }

    @Override
    public String toString() {
        return "MonsterStats {" +
                "npcId=" + npcId +
                ", combatLevel=" + combatLevel +
                ", hitpoints=" + hitpoints +
                ", attackStyles='" + attackStyles + '\'' +
                ", attackSpeed=" + attackSpeed +
                ", defenceLevel=" + defenceLevel +
                ", stabDefenceBonus=" + stabDefenceBonus +
                ", slashDefenceBonus=" + slashDefenceBonus +
                ", crushDefenceBonus=" + crushDefenceBonus +
                ", magicLevel=" + magicLevel +
                ", magicDefenceBonus=" + magicDefenceBonus +
                ", rangeDefenceBonus=" + rangeDefenceBonus +
                '}';
    }
}
