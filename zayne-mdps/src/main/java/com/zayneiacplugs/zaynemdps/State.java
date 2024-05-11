package com.zayneiacplugs.zaynemdps;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.entities.NPCs;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public class State {
    private Client client;
    private ZayneMDPSConfig config;
    private TileMap tileMap;
    private Map<LocalPoint, TargetTile> mapOfTiles;
    private NPCHandler npcHandler;
    private List<NPC> npcs;
    private NPCConfig npcConfig;
    private List<NPCConfig> npcConfigs;
    private int playerHealth;
    private LocalPoint playerLocation;
    private double dps;
    private int playerRunEnergy;
    private int playerSpecialAttackEnergy;
    private int playerPrayerPoints;
    private int tick;

    @Inject
    public State(Client client, TileMap tileMap, NPCHandler npcHandler, List<NPCConfig> npcConfigs){
        this.client = client;
        this.playerLocation = client.getLocalPlayer().getLocalLocation();
        this.playerHealth = client.getBoostedSkillLevel(Skill.HITPOINTS);
        this.playerPrayerPoints = client.getBoostedSkillLevel(Skill.PRAYER);
        this.playerRunEnergy = client.getEnergy();
        this.playerSpecialAttackEnergy = client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT);
        this.tileMap = tileMap;
        this.mapOfTiles = tileMap.getMap();
        this.npcHandler = npcHandler;
        this.npcs = npcHandler.getCachedNPCs();
        this.npcConfigs = npcConfigs;
    }

    public void refreshState(){
        this.tileMap.clean();
        this.npcHandler.process(client, config, tileMap);
        this.mapOfTiles = tileMap.getMap();
        this.playerLocation = client.getLocalPlayer().getLocalLocation();
        this.playerHealth = client.getRealSkillLevel(Skill.HITPOINTS);
        this.playerPrayerPoints = client.getBoostedSkillLevel(Skill.PRAYER);
        this.playerRunEnergy = client.getEnergy();
        this.playerSpecialAttackEnergy = client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT);

    }
}
