package com.zayneiacplugs.zaynemdps;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.utils.MessageUtils;
import net.unethicalite.api.widgets.Prayers;
import net.runelite.api.Prayer;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
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
    @Inject
    private Logger log;
    private State state;
    private ExecutorService executorService;
    private boolean isStartingUp;
    private MinimaxAlphaBeta minimax;

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
            MessageUtils.addMessage("Checking state initialization:");
            MessageUtils.addMessage("\n" + state.getClient().toString() +
                    "\n" + state.getNpcs().toString() +
                    "\n" + state.tileMap.getAllTiles().size() +
                    "\n" + state.playerTiles.size());
            this.isStartingUp = false;
            this.minimax = new MinimaxAlphaBeta(client, state);
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
            executorService = Executors.newSingleThreadExecutor();
        }
    }

    @Subscribe
    public void onGameTick(GameTick tick) throws Exception {
        if (isStartingUp) {
            return;
        }
        executorService.submit(() -> {
            try {
                state.refreshState();
                List<MinimaxAlphaBeta.Action> bestActions = minimax.getBestActions();
                for (MinimaxAlphaBeta.Action action : bestActions) {
                    executeAction(action);
                    MessageUtils.addMessage(action.getType());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void executeAction(MinimaxAlphaBeta.Action action) {
        switch (action.getType()) {
            case "Attack":
                EnhancedNPC target = action.getTarget();
                if (target != null) {
                    state.attack(target);
                    state.setLastAttackTick(state.getTick());
                }
                break;
            case "Heal":
                state.heal();
                break;
            case "Move":
                LocalPoint targetTile = action.getTargetTile();
                if (targetTile != null) {
                    state.move(targetTile);
                }
                break;
            case "Pray":
                togglePrayer(action.getPrayer());
                break;
            default:
                throw new IllegalArgumentException("Unknown action type: " + action.getType());
        }
    }

    private void togglePrayer(Prayer prayer) {
        Prayers.toggle(prayer);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN) {
            overlayManager.remove(stateInfoOverlay);
            overlayManager.remove(overlay);
            executorService.shutdown();
        } else if (gameStateChanged.getGameState() == GameState.LOGGED_IN && isStartingUp) {
            overlayManager.add(stateInfoOverlay);
            overlayManager.add(overlay);
            initializeExecutorService();
        }
    }
}
