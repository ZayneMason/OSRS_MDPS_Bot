package com.zayneiacplugs.zaynemdps;

import net.runelite.api.NPC;

public class ManticoreConfig extends NPCConfig {

    private NPC manticore;

    public ManticoreConfig(String name, String attackStyle, int range, ZayneMDPSConfig config, NPC manticore) {
        super(name, attackStyle, range, config);
        this.manticore = manticore;
    }

    public void determineAttackPattern() {
        // Implement logic to determine the attack pattern based on tier and proximity
    }

    public void executeAttackPattern() {
        // Execute the determined attack pattern
    }

    public void copyAttackPattern(NPC other) {
        if (this.checkProximity(other)) {
            // Copy attack pattern logic
        }
    }

    private boolean checkProximity(NPC other) {
        if (this.manticore.getLocalLocation().distanceTo(other.getLocalLocation()) <= 15) {
            return true;
        }
        return false;
    }
}

