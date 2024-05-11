package com.zayneiacplugs.zaynemdps;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("zayne-mdps")
public interface ZayneMDPSConfig extends Config {
    @ConfigSection(
            name = "NPC Settings",
            description = "Configure settings for npc information",
            position = 0,
            keyName = "npcSettings"
    )
    String npcSettings = "npcSettings";
    @ConfigSection(
            name = "Tile Settings",
            description = "Configure settings for tile information",
            position = 1,
            keyName = "tileSettings"
    )
    String tileSettings = "tileSettings";

    @ConfigItem(
            position = 2,
            keyName = "npcList",
            name = "NPC List",
            description = "List of NPCs in the format: 'NPC Name,Attack Style,Range; NPC Name,Attack Style,Range;'",
            section = npcSettings
    )
    default String npcList() {
        return "";
    }

    @ConfigItem(
            position = 6,
            keyName = "meleeColor",
            name = "Melee Color",
            description = "Color for Melee attack range"
    )
    @Alpha
    default Color meleeColor() {
        return new Color(255, 0, 0, 64);
    }

    @ConfigItem(
            position = 7,
            keyName = "rangedColor",
            name = "Ranged Color",
            description = "Color for Ranged attack range"
    )
    @Alpha
    default Color rangedColor() {
        return new Color(0, 255, 0, 64);
    }

    @ConfigItem(
            position = 8,
            keyName = "magicColor",
            name = "Magic Color",
            description = "Color for Magic attack range"
    )
    @Alpha
    default Color magicColor() {
        return new Color(0, 0, 255, 64);
    }

    @ConfigItem(
            position = 1,
            keyName = "overlayRange",
            name = "Range",
            description = "Range of the line of sight overlay.",
            section = tileSettings
    )
    @Range(
            min = 1,
            max = 104
    )
    default int overlayRange() {
        return 10;
    }

    @ConfigItem(
            keyName = "inLoSColor",
            name = "In-Line of Sight Color",
            description = "Color of tiles that are in the line of sight",
            position = 2,
            section = tileSettings
    )
    @Alpha
    default Color inLoSColor() {
        return new Color(0, 255, 0, 64);  // Light green with transparency
    }

    @ConfigItem(
            keyName = "outLoSColor",
            name = "Out-of-Line of Sight Color",
            description = "Color of tiles that are out of the line of sight",
            position = 3,
            section = tileSettings
    )
    @Alpha
    default Color outLoSColor() {
        return new Color(255, 0, 0, 64);  // Light red with transparency
    }

    @ConfigItem(
            keyName = "fillTiles",
            name = "Fill Tiles",
            description = "Show attackstyle colors in tiles",
            section = tileSettings
    )
    default boolean fillTiles() {
        return true;
    }

    enum Option {
        RANGE, MELEE, MAGE, SPECIAL_MELEE, WARBAND_MAGE, WARBAND_RANGE, OUT_OF_RANGE_IN_LOS, OUT_OF_RANGE_OUT_LOS,
    }
}
