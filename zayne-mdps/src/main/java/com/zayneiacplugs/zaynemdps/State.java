package com.zayneiacplugs.zaynemdps;

import com.google.inject.Inject;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.utils.MessageUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
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
    public WorldArea playerArea;
    public List<WorldPoint> playerTiles;
    public TileMap cachedTileMap;
    private ItemContainer playerInventory;
    private int ticksUntilPlayerAttack;
    private Item[] inventoryItems;
    private int totalHeals;
    private int totalPrayerRestore;

    @Inject
    public State(Client client, ZayneMDPSConfig config, ExecutorService executorService) throws IOException {
        this.config = config;
        this.client = client;
        this.tileMap = new TileMap(this);
        this.npcHandler = new NPCHandler(config);
        this.playerArea = getPlayerArea();
        this.playerTiles = getPlayerTiles();
        this.playerInventory = client.getItemContainer(InventoryID.INVENTORY);
        this.inventoryItems = playerInventory != null ? playerInventory.getItems() : new Item[0];
        tileMap.stateInitialized(this);
        npcHandler.stateInitialized(this);
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
        MessageUtils.addMessage("refreshState: Started at " + startTime);

        try {
            MessageUtils.addMessage("refreshState: Updating player info");
            updatePlayerInfo();
            MessageUtils.addMessage("refreshState: Player info updated");

            MessageUtils.addMessage("refreshState: Processing state");
            processState();
            MessageUtils.addMessage("refreshState: State processed");

            MessageUtils.addMessage("refreshState: Cloning tile map");
            cachedTileMap = tileMap.cloneTiles(this);
            MessageUtils.addMessage("refreshState: Tile map cloned");

            MessageUtils.addMessage("refreshState: Setting state as up to date");

            MessageUtils.addMessage("refreshState: State set as up to date");

            MessageUtils.addMessage("refreshState: Running stateUpdated");
            MessageUtils.addMessage("refreshState: stateUpdated complete");

            MessageUtils.addMessage("refreshState: Updating NPCs");
            this.npcs = npcHandler.getEnhancedNPCS();
            MessageUtils.addMessage("refreshState: NPCs updated, count: " + (npcs != null ? npcs.size() : 0));

            MessageUtils.addMessage("refreshState: Updating NPC configs");
            this.npcConfigs = npcHandler.getCachedNPCConfigs();
            MessageUtils.addMessage("refreshState: NPC configs updated, count: " + (npcConfigs != null ? npcConfigs.size() : 0));
            setUpToDate(true);
        } catch (Exception e) {
            upToDate.set(false);
            MessageUtils.addMessage("refreshState: Exception occurred - " + e.getMessage());
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        MessageUtils.addMessage("refreshState: Completed at " + endTime + ", duration: " + (endTime - startTime) + " ms");
    }


    private void updatePlayerInfo() {
            playerArea = getPlayerArea();
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
        setUpToDate(false);
        tileMap.stateUpdated(this);
    }

    private void processState() {
        upToDate.set(false);
        tileMap.stateProcessing(this);
        npcHandler.stateProcessing(this);
    }

    private void initializeState() {
        long startTime = System.nanoTime();

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

    private List<WorldPoint> getPlayerTiles() {
        WorldArea worldArea = new WorldArea(
                client.getLocalPlayer().getWorldArea().getX() - config.overlayRange(),
                client.getLocalPlayer().getWorldArea().getY() - config.overlayRange(),
                1 + (2 * config.overlayRange()),
                1 + (2 * config.overlayRange()),
                client.getLocalPlayer().getWorldArea().getPlane()
        );
        List<WorldPoint> playerTiles = new ArrayList<>();
        for (WorldPoint worldPoint : worldArea.toWorldPointList()) {
            if (ZayneUtils.validTile(worldPoint, client)) {
                playerTiles.add(worldPoint);
            }
        }
        return playerTiles;
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
