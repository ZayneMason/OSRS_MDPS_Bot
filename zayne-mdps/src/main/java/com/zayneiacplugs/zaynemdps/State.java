package com.zayneiacplugs.zaynemdps;

import com.google.inject.Inject;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.utils.MessageUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class State {
    private static final HashMap<Integer, Integer> healingValues = new HashMap<Integer, Integer>();
    private static final HashMap<Integer, Integer> prayerRestoreValues = new HashMap<Integer, Integer>();
    public Client client;
    @Inject
    public ZayneMDPSConfig config;
    public TileMap tileMap;
    public NPCHandler npcHandler;
    public List<EnhancedNPC> npcs;
    public List<NPCConfig> npcConfigs;
    public int playerHealth;
    public LocalPoint playerLocation;
    public double dps;
    public int playerRunEnergy;
    public int playerSpecialAttackEnergy;
    public int playerPrayerPoints;
    public int tick;
    public AtomicBoolean upToDate = new AtomicBoolean(false);
    public List<LocalPoint> playerTiles;
    private ItemContainer playerInventory;
    private int ticksUntilPlayerAttack;
    private Item[] inventoryItems;
    private int totalHeals;
    private int totalPrayerRestore;

    @Inject
    public State(Client client, ZayneMDPSConfig config) throws IOException {
        this.config = config;
        this.client = client;
        this.tileMap = new TileMap();
        this.npcHandler = new NPCHandler(config);
        this.inventoryItems = new Item[0];
        initializeState();
        populateHealPrayerValues();
        refreshState();
    }

    public Client getClient() {
        return client;
    }

    public TileMap getTileMap() {
        return tileMap;
    }

    public NPCHandler getNpcHandler() {
        return npcHandler;
    }

    WorldArea getPlayerArea() {
        return client.getLocalPlayer().getWorldArea();
    }

    public void refreshState() {
        long startTime = System.currentTimeMillis();
        try {
            updatePlayerInfo();
            processState();
            MessageUtils.addMessage("Tile count: " + tileMap.getAllTiles().size());
            stateUpdated();
            setUpToDate(true);
        } catch (Exception e) {
            upToDate.set(false);
            MessageUtils.addMessage("refreshState: Exception occurred - " + e.getMessage());
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        MessageUtils.addMessage("State refreshed: " + (endTime - startTime) + " ms");
    }


    private void updatePlayerInfo() {
        this.playerLocation = client.getLocalPlayer().getLocalLocation();
        this.playerTiles = getPlayerTiles();
        this.playerInventory = client.getItemContainer(InventoryID.INVENTORY);
        this.inventoryItems = playerInventory != null ? playerInventory.getItems() : new Item[0];
        this.playerHealth = client.getBoostedSkillLevel(Skill.HITPOINTS);
        this.totalHeals = getHeals();
        this.playerPrayerPoints = client.getBoostedSkillLevel(Skill.PRAYER);
        this.totalPrayerRestore = getPrayerRestores();
        this.playerRunEnergy = client.getEnergy();
        this.playerSpecialAttackEnergy = client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) / 10;
    }

    private int getPrayerRestores() {
        int sum = 0;
        for (Item item : inventoryItems) {
            if (item != null && prayerRestoreValues.containsKey(item.getId())) {
                sum += prayerRestoreValues.get(item.getId());
            }
        }
        return sum;
    }

    private int getHeals() {
        int sum = 0;
        for (Item item : inventoryItems) {
            if (item != null && healingValues.containsKey(item.getId())) {
                sum += healingValues.get(item.getId());
            }
        }
        return sum;
    }

    private void stateUpdated() {
        this.npcs = npcHandler.getEnhancedNPCS();
        this.npcConfigs = npcHandler.getCachedNPCConfigs();
        for (EnhancedNPC enhancedNPC : getNpcs()) {
            MessageUtils.addMessage(enhancedNPC.getNpc().getName() + ": " + enhancedNPC.getTicksUntilAttack() + "ticks until attack");
        }
        ;
        setUpToDate(true);
    }

    private void processState() {
        upToDate.set(false);
        tileMap.clearTiles();
        npcHandler.stateProcessing(this);
    }

    private void initializeState() {
        long startTime = System.nanoTime();
        npcHandler.stateInitialized(this);
        long endTime = System.nanoTime();
    }

    public boolean isUpToDate() {
        return upToDate.get();
    }

    public void setUpToDate(boolean isUpToDate) {
        upToDate.set(isUpToDate);
    }

    public synchronized void clearState() throws IOException {
        tileMap.clearTiles();
        npcHandler.clearCache();  // Assumes a clearCache method in NPCHandler
    }

    List<LocalPoint> getPlayerTiles() {
        WorldArea worldArea = new WorldArea(
                client.getLocalPlayer().getWorldLocation().getX() - config.overlayRange(),
                client.getLocalPlayer().getWorldLocation().getY() - config.overlayRange(),
                1 + (2 * config.overlayRange()),
                1 + (2 * config.overlayRange()),
                client.getLocalPlayer().getWorldArea().getPlane()
        );

        List<TileObject> tileObjects = TileObjects.getSurrounding(
                Players.getLocal().getWorldLocation(),
                35,
                tru -> true
        );

        List<WorldPoint> playerTiles = new ArrayList<>();
        for (WorldPoint worldPoint : worldArea.toWorldPointList()) {
            if (ZayneUtils.validTile(worldPoint, client)) {
                playerTiles.add(worldPoint);
            }
        }
        List<LocalPoint> localPlayerTiles = new ArrayList<>();
        for (WorldPoint worldPoint : playerTiles) {
            localPlayerTiles.add(LocalPoint.fromWorld(client, worldPoint));
        }
        return localPlayerTiles;
    }

    public List<EnhancedNPC> getNpcs() {
        return this.npcs;
    }

    public int getPlayerHealth() {
        return this.playerHealth;
    }

    public int getRunEnergy() {
        return this.playerRunEnergy / 100;
    }

    public int getPlayerSpecialAttackEnergy() {
        return this.playerSpecialAttackEnergy;
    }

    public int getTicksUntilNextNPCAttack() {
        return 1;
    }

    public int getTicksUntilNextPlayerAttack() {
        return this.ticksUntilPlayerAttack;
    }

    public String getTypeOfAttack() {
        return ZayneMDPSConfig.Option.MAGE.toString();
    }

    public ItemContainer getPlayerInventory() {
        return this.playerInventory;
    }

    private void populateHealPrayerValues() {
        healingValues.put(ItemID.SHARK, 20);
        healingValues.put(ItemID.MONKFISH, 16);
        healingValues.put(ItemID.ANGLERFISH, 22);
        healingValues.put(ItemID.SARADOMIN_BREW1, (client.getRealSkillLevel(Skill.HITPOINTS) * 15 / 100) + 2);
        healingValues.put(ItemID.SARADOMIN_BREW2, healingValues.get(ItemID.SARADOMIN_BREW1) * 2);
        healingValues.put(ItemID.SARADOMIN_BREW3, healingValues.get(ItemID.SARADOMIN_BREW1) * 3);
        healingValues.put(ItemID.SARADOMIN_BREW4, healingValues.get(ItemID.SARADOMIN_BREW1) * 4);

        prayerRestoreValues.put(ItemID.PRAYER_POTION1,
                hasHolyWrenchItem() ? (client.getRealSkillLevel(Skill.PRAYER) * 27 / 100) + 8 :
                        (client.getRealSkillLevel(Skill.PRAYER) * 27 / 100) + 7
        );
        prayerRestoreValues.put(ItemID.PRAYER_POTION2, prayerRestoreValues.get(ItemID.PRAYER_POTION1) * 2);
        prayerRestoreValues.put(ItemID.PRAYER_POTION3, prayerRestoreValues.get(ItemID.PRAYER_POTION1) * 3);
        prayerRestoreValues.put(ItemID.PRAYER_POTION4, prayerRestoreValues.get(ItemID.PRAYER_POTION1) * 4);
        prayerRestoreValues.put(ItemID.SUPER_RESTORE1, prayerRestoreValues.get(ItemID.PRAYER_POTION1) + 1);
        prayerRestoreValues.put(ItemID.SUPER_RESTORE2, prayerRestoreValues.get(ItemID.SUPER_RESTORE1) * 2);
        prayerRestoreValues.put(ItemID.SUPER_RESTORE3, prayerRestoreValues.get(ItemID.SUPER_RESTORE1) * 3);
        prayerRestoreValues.put(ItemID.SUPER_RESTORE4, prayerRestoreValues.get(ItemID.SUPER_RESTORE1) * 4);

    }

    public boolean hasHolyWrenchItem() {
        for (Item item : inventoryItems) {
            if (item != null && (item.getId() == ItemID.HOLY_WRENCH ||
                    item.getId() == ItemID.PRAYER_CAPE ||
                    item.getId() == ItemID.MAX_CAPE ||
                    item.getId() == ItemID.RING_OF_THE_GODS ||
                    item.getId() == ItemID.RING_OF_THE_GODS_I
            )) {
                return true;
            }
        }
        return false;
    }

    public int getTotalHeals() {
        return totalHeals;
    }

    public int getTotalPrayerRestore() {
        return totalPrayerRestore;
    }

    public int getPlayerPrayerPoints() {
        return playerPrayerPoints;
    }
}
