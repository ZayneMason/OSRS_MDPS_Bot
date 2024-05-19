package com.zayneiacplugs.zaynemdps;

import net.runelite.api.Client;
import net.runelite.api.CollisionDataFlag;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;

import java.util.*;

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

    static boolean validTile(WorldPoint from, Client client) {

        if (from.getPlane() != client.getPlane()) {
            return false;
        }

        int[][] flags = Objects.requireNonNull(client.getCollisionMaps())[client.getPlane()].getFlags();

        int x = from.getX() - client.getBaseX();
        int y = from.getY() - client.getBaseY();

        int flag = flags[x][y];

        if (isMovementObstacle(flag)) {
            return false;
        }
        return hasPathing(client.getLocalPlayer().getWorldLocation(), from, client);
    }

    public static boolean hasPathing(WorldPoint from, WorldPoint to, Client client) {
        if (from.getPlane() != to.getPlane()) {
            return false;
        }

        // Get collision flags
        int[][] flags = Objects.requireNonNull(client.getCollisionMaps())[client.getPlane()].getFlags();

        return bfsPathExists(from, to, flags, client);
    }

    private static boolean bfsPathExists(WorldPoint from, WorldPoint to, int[][] flags, Client client) {
        Queue<WorldPoint> queue = new LinkedList<>();
        Set<WorldPoint> visited = new HashSet<>();
        int plane = from.getPlane();

        queue.add(from);
        visited.add(from);

        while (!queue.isEmpty()) {
            WorldPoint current = queue.poll();

            if (current.equals(to)) {
                return true;
            }

            for (WorldPoint neighbor : getNeighbors(current, flags, client)) {
                if (!visited.contains(neighbor) && neighbor.getPlane() == plane) {
                    queue.add(neighbor);
                    visited.add(neighbor);
                }
            }
        }

        return false;
    }

    private static List<WorldPoint> getNeighbors(WorldPoint point, int[][] flags, Client client) {
        List<WorldPoint> neighbors = new ArrayList<>();
        int x = point.getX();
        int y = point.getY();
        int plane = point.getPlane();
        int baseX = client.getBaseX();
        int baseY = client.getBaseY();

        // Define potential moves (right, left, up, down)
        int[][] moves = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        for (int[] move : moves) {
            int newX = x + move[0];
            int newY = y + move[1];

            if (isValidTile(newX, newY, plane, flags, baseX, baseY)) {
                neighbors.add(new WorldPoint(newX, newY, plane));
            }
        }

        return neighbors;
    }

    private static boolean isValidTile(int x, int y, int plane, int[][] flags, int baseX, int baseY) {
        // Check if the tile is within the game boundaries
        if (x < baseX || y < baseY || x >= baseX + 104 || y >= baseY + 104) {
            return false;
        }

        // Check if the tile is walkable
        int localX = x - baseX;
        int localY = y - baseY;
        int flag = flags[localX][localY];

        return !isMovementObstacle(flag);
    }
}

