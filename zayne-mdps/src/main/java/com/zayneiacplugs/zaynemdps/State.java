package com.zayneiacplugs.zaynemdps;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.VarPlayer;
import net.runelite.api.coords.LocalPoint;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public class State {
    public Client client;
    public ZayneMDPSConfig config;
    public TileMap tileMap;
    public Map<LocalPoint, TargetTile> mapOfTiles;
    public NPCHandler npcHandler;
    public List<NPC> npcs;
    public NPCConfig npcConfig;
    public List<NPCConfig> npcConfigs;
    private int playerHealth;
    private LocalPoint playerLocation;
    private double dps;
    private int playerRunEnergy;
    private int playerSpecialAttackEnergy;
    private int playerPrayerPoints;
    private int tick;
    private boolean upToDate;

    @Inject
    public State(Client client, TileMap tileMap, NPCHandler npcHandler) {
        this.client = client;
        this.playerLocation = client.getLocalPlayer().getLocalLocation();
        this.playerHealth = client.getBoostedSkillLevel(Skill.HITPOINTS);
        this.playerPrayerPoints = client.getBoostedSkillLevel(Skill.PRAYER);
        this.playerRunEnergy = client.getEnergy();
        this.playerSpecialAttackEnergy = client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT);
        this.tileMap = tileMap;
        this.npcHandler = npcHandler;
        npcHandler.process(client, config, tileMap);
        this.npcs = npcHandler.getCachedNPCs();
        this.npcConfigs = npcHandler.getCachedNPCConfigs();
        this.mapOfTiles = tileMap.getMap();
    }

    public void refreshState() {
        this.tileMap.clean();
        this.npcHandler.process(client, config, tileMap);
        this.mapOfTiles = tileMap.getMap();
        this.playerLocation = client.getLocalPlayer().getLocalLocation();
        this.playerHealth = client.getRealSkillLevel(Skill.HITPOINTS);
        this.playerPrayerPoints = client.getBoostedSkillLevel(Skill.PRAYER);
        this.playerRunEnergy = client.getEnergy();
        this.playerSpecialAttackEnergy = client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT);
        this.upToDate = true;
    }

    public boolean getUpToDate(){
        return this.upToDate;
    }

    public void clearState(){
        this.tileMap.clean();
        this.npcHandler.getCachedNPCConfigs().clear();
        this.npcHandler.getCachedNPCs().clear();
        this.npcHandler.getEnhancedNPCS().clear();
    }
}
