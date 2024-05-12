package com.zayneiacplugs.zaynemdps;

import com.google.common.base.Strings;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.unethicalite.api.utils.MessageUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.awt.*;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import static net.runelite.client.ui.overlay.OverlayPriority.LOW;

public class ZayneMDPSOverlay extends Overlay {
    private Client client;
    private ZayneMDPSConfig config;
    private NPCHandler npcHandler;
    private HashMap<LocalPoint, TargetTile> map;
    private State state;
    private volatile TileMap tileMap;

    @Inject
    public ZayneMDPSOverlay() {
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(LOW);
        setLayer(OverlayLayer.UNDER_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        for (EnhancedNPC npc : npcHandler.getEnhancedNPCS()) {
            drawNPCInfo(graphics, npc);
        }
            if (state.getUpToDate()) {
                for (TargetTile targetTile : map.values()) {
                    renderTile(graphics, targetTile);
                }
            }

            return null;
    }

    private void drawNPCInfo(Graphics2D graphics, EnhancedNPC npc) {
        OverlayUtil.renderActorParagraph(graphics, npc.getNpc(), npc.getNpcConfig().getName() + npc.getUniqueId(), Color.BLACK);
    }

    private void renderTile(Graphics2D graphics, TargetTile targetTile) {
        if (targetTile == null || client == null) {
            MessageUtils.addMessage("targetTile or client is null");
            return;
        }
        Polygon poly = Perspective.getCanvasTilePoly(client, targetTile.getLocalPoint());
        if (poly == null) {
            MessageUtils.addMessage("Unable to compute polygon for tile");
            return;
        }
        if (config.fillTiles()) renderAttackStyles(graphics, targetTile, poly);
    }

    private void renderAttackStyles(Graphics2D graphics, TargetTile tile, Polygon poly) {
        if (tile.getDamageTypes() == null){
            MessageUtils.addMessage("damageTypes null at renderAttackStyles");
        }
        Set<ZayneMDPSConfig.Option> relevantDamageTypes = tile.getDamageTypes().stream()
                .filter(dt -> dt != ZayneMDPSConfig.Option.OUT_OF_RANGE_IN_LOS && dt != ZayneMDPSConfig.Option.OUT_OF_RANGE_OUT_LOS)
                .collect(Collectors.toSet());
        // Get the distinct colors from the filtered damage types
        Set<Color> distinctColors = relevantDamageTypes.stream()
                .map(this::getAttackColor)  // Convert each damage type to its color
                .collect(Collectors.toSet());

        if (distinctColors == null){
            MessageUtils.addMessage("distictColors null at renderAttackStyles");
        }
        int total = distinctColors.size();
        if (total > 0) {
            double angleStep = 360.0 / total;
            int radius = 48;  // Adjust based on your need
            double startAngle = 0;
            for (Color color : distinctColors) {
                Polygon segment = createCircleSegment(client, tile.getLocalPoint(), startAngle, angleStep, radius);
                graphics.setColor(color);
                graphics.fillPolygon(segment);
                graphics.drawPolygon(segment);
                startAngle += angleStep;
            }
        } else {
            if (tile.getDamageTypes().contains(ZayneMDPSConfig.Option.OUT_OF_RANGE_IN_LOS)) {
                drawTile(graphics, WorldPoint.fromLocal(client, tile.getLocalPoint()), getAttackColor(ZayneMDPSConfig.Option.OUT_OF_RANGE_IN_LOS), null, new BasicStroke(1));
            } else {
                drawTile(graphics, WorldPoint.fromLocal(client, tile.getLocalPoint()), getAttackColor(ZayneMDPSConfig.Option.OUT_OF_RANGE_OUT_LOS), null, new BasicStroke(1));
            }

        }
    }

    private Rectangle getTileBounds(LocalPoint localLocation, int size) {
        final int baseX = localLocation.getX() - (size * Perspective.LOCAL_TILE_SIZE / 2);
        final int baseY = localLocation.getY() - (size * Perspective.LOCAL_TILE_SIZE / 2);
        final int height = size * Perspective.LOCAL_TILE_SIZE;

        return new Rectangle(baseX, baseY, height, height);
    }

    private Polygon createCircleSegment(Client client, LocalPoint center, double startAngle, double sweepAngle, int radius) {
        Polygon poly = new Polygon();
        double radStep = Math.toRadians(120);  // Step for arc resolution

        // Create the arc from startAngle to startAngle + sweepAngle
        for (double angle = startAngle; angle <= startAngle + sweepAngle; angle += radStep) {
            int x = center.getX() + (int) (radius * Math.cos(Math.toRadians(angle)));
            int y = center.getY() + (int) (radius * Math.sin(Math.toRadians(angle)));

            Point canvasPoint = Perspective.localToCanvas(client, new LocalPoint(x, y), client.getPlane(), 0);
            if (canvasPoint != null) {
                poly.addPoint(canvasPoint.getX(), canvasPoint.getY());
            }
        }

        // Connect back to the center to close the segment
        Point centerPoint = Perspective.localToCanvas(client, center, client.getPlane());
        if (centerPoint != null) {
            poly.addPoint(centerPoint.getX(), centerPoint.getY());
        }

        return poly;
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

    private void drawTile(Graphics2D graphics, WorldPoint point, Color color, @Nullable String label, Stroke borderStroke) {
        WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();

        if (point.distanceTo(playerLocation) >= 32) {
            return;
        }

        LocalPoint lp = LocalPoint.fromWorld(client, point);
        if (lp == null) {
            return;
        }

        Polygon poly = Perspective.getCanvasTilePoly(client, lp);
        if (poly != null) {
            OverlayUtil.renderPolygon(graphics, poly, color, color, borderStroke);
        }

        if (!Strings.isNullOrEmpty(label)) {
            Point canvasTextLocation = Perspective.getCanvasTextLocation(client, graphics, lp, label, 0);
            if (canvasTextLocation != null) {
                OverlayUtil.renderTextLocation(graphics, canvasTextLocation, label, color);
            }
        }
    }

    private int getHeightAtLocalPoint(int[][][] tileHeights, LocalPoint point, int plane) {
        int x = point.getSceneX();
        int y = point.getSceneY();
        if (x < 0 || x >= tileHeights[plane].length || y < 0 || y >= tileHeights[plane][x].length) {
            return 0;  // Return default height if out of bounds
        }
        return tileHeights[plane][x][y];
    }

    public void addState(State state) {
        if (this.state == null) {
            this.state = state;
            this.client = state.client;
            this.config = state.config;
            this.tileMap = state.tileMap;
            if (tileMap == null){
                MessageUtils.addMessage("Tile map null");
            }
            this.npcHandler = state.npcHandler;
            this.map = (HashMap<LocalPoint, TargetTile>) tileMap.getMap();
        }
    }

    public void updateState(){
        if (state != null) {
            state.refreshState();
            this.config = state.config;
            this.tileMap = state.tileMap;
            this.map = (HashMap<LocalPoint, TargetTile>) tileMap.getMap();
        }
    }

    public State getState() {
        return state;
    }
}

