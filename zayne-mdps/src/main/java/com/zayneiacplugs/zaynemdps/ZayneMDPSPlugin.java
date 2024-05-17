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
    private ZayneMDPSOverlay overlay;

    @Inject
    private StateInfoOverlay stateInfoOverlay;
    private ClientTick clientTick;
    private State state;
    private ExecutorService executorService;
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
        if (client.getGameState() == GameState.LOGGED_IN) {
            initializeExecutorService();
            this.state = new State(client, config, overlay);
            overlay.addState(state);
            stateInfoOverlay.addState(state);
            overlayManager.add(overlay);
            overlayManager.add(stateInfoOverlay);
            state.refreshState();
        }
    }


    @Override
    protected void shutDown() throws IOException {
        try {
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdownNow();
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        overlayManager.remove(overlay);
        overlayManager.remove(stateInfoOverlay);
        state.clearState();
    }

    private void initializeExecutorService() {
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(2);
        }
    }
    @Subscribe
    public void onGameTick(GameTick tick) throws Exception {
        if (executor.isShutdown()) {
            return;
        }
        if (state == null) {
            startUp();
        } else {
            executor.submit(() -> {
                state.refreshState();
            });
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (overlay != null) {
            overlayManager.remove(overlay);
            overlayManager.remove(stateInfoOverlay);
            overlay = new ZayneMDPSOverlay();
            stateInfoOverlay = new StateInfoOverlay();
            overlay.addState(state);
            stateInfoOverlay.addState(state);
            overlayManager.add(stateInfoOverlay);
            overlayManager.add(overlay);
        } else {
            overlay = new ZayneMDPSOverlay();
            stateInfoOverlay = new StateInfoOverlay();
            overlay.addState(state);
            stateInfoOverlay.addState(state);
            overlayManager.add(overlay);
            overlayManager.add(stateInfoOverlay);
        }
    }
}
