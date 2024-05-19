package com.zayneiacplugs.zaynemdps;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
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
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
    @Inject
    private StateInfoOverlay stateInfoOverlay;
    @Inject
    private ZayneMDPSOverlay overlay;
    private ClientTick clientTick;
    private State state;
    private ExecutorService executorService;
    @Inject
    private Logger log;
    private boolean isStartingUp;

    public ZayneMDPSPlugin() {
    }

    @Provides
    ZayneMDPSConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ZayneMDPSConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        if (client.getGameState() == GameState.LOGGED_IN) {
            this.isStartingUp = true;
            initializeExecutorService();
            state = new State(client, config);
            state.refreshState();
            overlay.addState(state);
            stateInfoOverlay.addState(state);
            overlayManager.add(stateInfoOverlay);
            overlayManager.add(overlay);
            MessageUtils.addMessage("Checking state shit: ");
            MessageUtils.addMessage("\n" + state.getClient().toString() +
                    "\n" + state.getNpcs().toString() +
                    "\n" + state.tileMap.getAllTiles().size() +
                    "\n" + state.playerTiles.size());
            this.isStartingUp = false;
        }
    }


    @Override
    protected void shutDown() throws IOException {
        try {
            overlayManager.remove(stateInfoOverlay);
            overlayManager.remove(overlay);
            state.clearState();
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdownNow();
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void initializeExecutorService() {
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(2);
        }
    }

    private void initializeState() throws IOException {

        if (client == null) {
            MessageUtils.addMessage("initializeState: client is null");
        }
        if (config == null) {
            MessageUtils.addMessage("initializeState: config is null");
        }
        if (executorService == null) {
            MessageUtils.addMessage("initializeState: executorService is null");
        }

    }

    @Subscribe
    public void onGameTick(GameTick tick) throws Exception {
        if (isStartingUp) {
            return;
        }
        if (executor.isShutdown()) {
            return;
        } else {
            executor.submit(() -> {
                state.refreshState();
            });
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {

    }
}
