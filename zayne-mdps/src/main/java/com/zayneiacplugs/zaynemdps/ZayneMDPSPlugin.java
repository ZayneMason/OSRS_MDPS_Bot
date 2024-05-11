package com.zayneiacplugs.zaynemdps;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.utils.MessageUtils;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

@Extension
@PluginDescriptor(
        name = "<html><font color=\"#C70039\">Zayne MDPS</font></html>",
        description = "A plugin for testing combat decision using mdps.",
        enabledByDefault = false,
        tags = {"zayne"}
)
public class ZayneMDPSPlugin extends Plugin {
    @Inject
    private ExecutorService executor;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private ZayneMDPSConfig config;
    private TileMap tileMap;
    @Inject
    private ZayneMDPSOverlay overlay;
    private NPCHandler npcHandler;
    private ClientTick clientTick;
    private State state;

    @Inject
    private Logger log;

    public ZayneMDPSPlugin() {
    }

    @Provides
    ZayneMDPSConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ZayneMDPSConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        this.tileMap = new TileMap(config);
        this.npcHandler = new NPCHandler();
        this.overlay = new ZayneMDPSOverlay(client, config, tileMap, npcHandler);
        List<NPCConfig> npcConfigs = npcHandler.parseNPCConfig(config.npcList(), config);
        this.state = new State(client, tileMap, npcHandler, npcConfigs);
        overlayManager.add(overlay);
        MessageUtils.addMessage("Zayne MDPS Plugin started.");
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
        npcHandler.getCachedNPCs().clear();
        MessageUtils.addMessage("Zayne MDPS Plugin stopped.");
        executor.shutdown();
    }

    @Subscribe
    public void onGameTick(GameTick tick) {
        state.refreshState();
    }


}
