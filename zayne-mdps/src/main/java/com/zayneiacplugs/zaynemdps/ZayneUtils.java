package com.zayneiacplugs.zaynemdps;

import net.runelite.api.Client;
import net.runelite.api.CollisionDataFlag;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface ZayneUtils {
    static boolean hasLineOfSight(WorldPoint from, WorldPoint to, Client client) {
        if (from.getPlane() != to.getPlane()) {
            return false;
        }

        List<Point> linePoints = bresenhamLine(from.getX(), from.getY(), to.getX(), to.getY());
        int plane = from.getPlane();
        int[][] flags = Objects.requireNonNull(client.getCollisionMaps())[plane].getFlags();

        for (net.runelite.api.Point p : linePoints) {
            int x = p.getX() - client.getBaseX();
            int y = p.getY() - client.getBaseY();

            if (x < 0 || y < 0 || x >= 104 || y >= 104) {
                continue;
            }

            int flag = flags[x][y];
            if (isObstacle(flag)) {
                return false;
            }
        }
        return true;
    }

    static boolean isObstacle(int flag) {
        return (flag & CollisionDataFlag.BLOCK_MOVEMENT_FULL) != 0 ||
                (flag & CollisionDataFlag.BLOCK_LINE_OF_SIGHT_FULL) != 0;
    }

    static boolean isMovementObstacle(int flag) {
        return (flag & CollisionDataFlag.BLOCK_MOVEMENT_FULL) != 0 ||
                (flag & CollisionDataFlag.BLOCK_MOVEMENT_OBJECT) != 0;
    }

    static List<net.runelite.api.Point> bresenhamLine(int x0, int y0, int x1, int y1) {
        List<net.runelite.api.Point> points = new ArrayList<>();

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        int e2;

        while (true) {
            points.add(new Point(x0, y0));

            if (x0 == x1 && y0 == y1) {
                break;
            }

            e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }

        return points;
    }

    static boolean validTile(WorldPoint from, WorldPoint to, Client client) {
        if (from.getPlane() != to.getPlane()) {
            return false;
        }

        List<Point> linePoints = bresenhamLine(from.getX(), from.getY(), to.getX(), to.getY());
        int plane = from.getPlane();
        int[][] flags = Objects.requireNonNull(client.getCollisionMaps())[plane].getFlags();

        for (net.runelite.api.Point p : linePoints) {
            int x = p.getX() - client.getBaseX();
            int y = p.getY() - client.getBaseY();

            if (x < 0 || y < 0 || x >= 104 || y >= 104) {
                continue;
            }

            int flag = flags[x][y];
            if (isMovementObstacle(flag)) {
                return false;
            }
        }
        return true;
    }
}
