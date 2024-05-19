package com.zayneiacplugs.zaynemdps;

import net.unethicalite.api.utils.MessageUtils;

import java.awt.*;
import java.io.IOException;

public class NPCConfig {
    final String attackStyle;
    private final String name;
    private final int range;
    private final ZayneMDPSConfig config;
    public MonsterStats monsterStats = null;

    public NPCConfig(String name, String attackStyle, int range, ZayneMDPSConfig config) throws IOException {
        this.name = name;
        this.attackStyle = attackStyle;
        this.range = range;
        this.config = config;
        this.monsterStats = RuneScapeWikiScraper.getMonsterStats(name);
        MessageUtils.addMessage("Config added: " + getName());
    }

    public String getName() {
        return name;
    }

    public ZayneMDPSConfig.Option getAttackStyle() {
        switch (attackStyle) {
            case "Melee":
                return ZayneMDPSConfig.Option.MELEE;
            case "Ranged":
                return ZayneMDPSConfig.Option.RANGE;
            case "Mage":
                return ZayneMDPSConfig.Option.MAGE;
            case "Special_Melee":
                return ZayneMDPSConfig.Option.SPECIAL_MELEE;
            case "Warband_Mage":
                return ZayneMDPSConfig.Option.WARBAND_MAGE;
            case "Warband_Range":
                return ZayneMDPSConfig.Option.WARBAND_RANGE;
        }
        return ZayneMDPSConfig.Option.MELEE;
    }

    public int getRange() {
        return range;
    }

    public Color getAttackColor() {
        switch (this.getAttackStyle()) {
            case MELEE:
            case SPECIAL_MELEE:
                return config.meleeColor();
            case MAGE:
            case WARBAND_MAGE:
                return config.magicColor();
            case RANGE:
            case WARBAND_RANGE:
                return config.rangedColor();
            case OUT_OF_RANGE_IN_LOS:
                return config.inLoSColor();
            case OUT_OF_RANGE_OUT_LOS:
                return config.outLoSColor();
        }
        return config.meleeColor();
    }

    public int getHealth() {
        return 0;
    }

    public MonsterStats getMonsterStats() {
        return monsterStats;
    }
}