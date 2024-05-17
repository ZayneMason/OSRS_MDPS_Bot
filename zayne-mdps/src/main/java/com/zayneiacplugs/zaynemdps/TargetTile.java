package com.zayneiacplugs.zaynemdps;

import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GraphicsObjectCreated;
import net.unethicalite.api.utils.MessageUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TargetTile {
    private final LocalPoint localPoint;
    private final ZayneMDPSConfig config;
    private ArrayList<EnhancedNPC> npcs = new ArrayList<>();
    private ArrayList<ZayneMDPSConfig.Option> damageTypes = new ArrayList<>();
    private volatile boolean inLoS;

    public TargetTile(LocalPoint localPoint, EnhancedNPC npc, ZayneMDPSConfig.Option damageType, boolean inLoS, ZayneMDPSConfig config) {
        this.localPoint = localPoint;
        this.config = config;
        this.addNPC(npc);
        this.addDamageType(damageType);
        this.inLoS = inLoS;
    }

    public TargetTile(LocalPoint localPoint, ZayneMDPSConfig config) {
        this.localPoint = localPoint;
        this.config = config;
        this.damageTypes.add(ZayneMDPSConfig.Option.OUT_OF_RANGE_OUT_LOS);
    }

    public TargetTile(LocalPoint localPoint, ZayneMDPSConfig config, ArrayList<EnhancedNPC> npcs, ArrayList<ZayneMDPSConfig.Option> damageTypes, boolean inLoS){
        this.localPoint = localPoint;
        this.config = config;
        this.npcs = npcs;
        this.damageTypes = damageTypes;
        this.inLoS = inLoS;
    }

    public LocalPoint getLocalPoint() {
        return this.localPoint;
    }

    public ArrayList<EnhancedNPC> getNPCs() {
        return this.npcs;
    }

    public ArrayList<ZayneMDPSConfig.Option> getDamageTypes() {
        return this.damageTypes;
    }

    public boolean getInLoS() {
        return this.inLoS;
    }

    public void setInLoS(boolean newInLoS) {
        if (!inLoS) {
            this.inLoS = newInLoS;
        }
    }

    public void addDamageType(ZayneMDPSConfig.Option damageType) {
        this.damageTypes.add(damageType);
    }

    public void addNPC(EnhancedNPC npc) {
        if (npc == null) {
            MessageUtils.addMessage("NPC CAN'T BE NULL");
        }
        this.npcs.add(npc);
    }

    public int distinctDamageTypes() {
        List<ZayneMDPSConfig.Option> relevantDamageTypes = getDamageTypes().stream()
                .filter(dt -> dt != ZayneMDPSConfig.Option.OUT_OF_RANGE_IN_LOS && dt != ZayneMDPSConfig.Option.OUT_OF_RANGE_OUT_LOS)
                .collect(Collectors.toList());
        // Get the distinct colors from the filtered damage types
        Set<Color> distinctColors = relevantDamageTypes.stream()
                .map(this::getAttackColor)  // Convert each damage type to its color
                .collect(Collectors.toSet());

        return distinctColors.size();
    }

    private Color getAttackColor(ZayneMDPSConfig.Option attackStyle) {
        switch (attackStyle) {
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
        return Color.BLACK;
    }

    public void clear() {
        npcs = new ArrayList<>();
        damageTypes = new ArrayList<>();
        inLoS = false;
    }
}
