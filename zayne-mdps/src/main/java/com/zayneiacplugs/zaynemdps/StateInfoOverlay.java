package com.zayneiacplugs.zaynemdps;

import java.awt.*;
import java.util.stream.Collectors;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class StateInfoOverlay extends OverlayPanel {
    private  Client client;
    private final PanelComponent panelComponent = new PanelComponent();
    private State state;

    @Inject
    StateInfoOverlay()
    {
        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.HIGH);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();

        // Example state info, replace with your actual state data
        String stateInfo = getStateInfo();

        panelComponent.getChildren().add(TitleComponent.builder()
                .text("State Info")
                .color(Color.WHITE)
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("State: " + getStateInfo())
                .build());
        panelComponent.setPreferredSize(new Dimension(300, 0));
        return panelComponent.render(graphics);
    }

    private String getStateInfo()
    {
        // Replace this with your actual state retrieval logic
        // Example state data
        String ticksUntilNextAttack = String.valueOf(state.getTicksUntilNextNPCAttack());
        String attackType = state.getTypeOfAttack() != null ? state.getTypeOfAttack() : "Unknown";
        String playerHealth = String.valueOf(state.getPlayerHealth());
        String totalHeals = String.valueOf(state.getTotalHeals());
        String playerPrayer = String.valueOf(state.getPlayerPrayerPoints());
        String totalPrayerRestore = String.valueOf(state.getTotalPrayerRestore());
        String playerRunEnergy = String.valueOf(state.getRunEnergy());
        String playerSpecialAttackEnergy = String.valueOf(state.getPlayerSpecialAttackEnergy());
        String npcs = (state.getNpcs() != null && !state.getNpcs().isEmpty()) ?
                String.valueOf(state.getNpcs().stream().map(EnhancedNPC::getNpcConfig).map(NPCConfig::getName).collect(Collectors.joining(", "))) :
                "No NPCs";

        return String.format("\nTicks until next attack: %s\nAttack Type: %s\nPlayer health: %s\nHeals: %s\nPlayer prayer: %s\nPrayer restores: %s\nPlayer run energy: %s\nSpecial Energy: %s\nNPCs: %s",
                ticksUntilNextAttack, attackType, playerHealth, totalHeals, playerPrayer, totalPrayerRestore, playerRunEnergy, playerSpecialAttackEnergy, npcs);
    }


    public void addState(State state){
        this.state = state;
        this.client = client;
    }
}
