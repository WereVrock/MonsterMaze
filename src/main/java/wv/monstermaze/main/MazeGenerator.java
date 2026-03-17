package wv.monstermaze.main;

import java.awt.*;
import java.util.*;
import java.util.List;

public class MazeGenerator {

    private Set<Point> corridor = new HashSet<>();
    private int dir = 0;
    private int cx = 0;
    private int cy = 0;
    private Random rand = new Random();

    // --- NEW: drilling tracking ---
    private int drillCount = 0;
    private long lastPocketTime = 0;

    public MazeGenerator() {
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                corridor.add(new Point(x, y));
            }
        }
        cx = 4;
        cy = 2;
    }

    public void ensureArea(double px, double py) {
        int tx = (int) (px / Game.TILE);
        int ty = (int) (py / Game.TILE);

        while (distance(cx, cy, tx, ty) < 30)
            extend();
    }

    private void extend() {
        int turn = rand.nextInt(3) - 1;
        dir = (dir + turn + 4) % 4;

        if (dir == 0) cy--;
        if (dir == 1) cx++;
        if (dir == 2) cy++;
        if (dir == 3) cx--;

        corridor.add(new Point(cx, cy));
        corridor.add(new Point(cx + 1, cy));
    }

    private int distance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    public boolean isWallTile(int x, int y) {
        return !corridor.contains(new Point(x, y));
    }

    public boolean isColliding(Rectangle r) {
        int startX = (int) Math.floor((double) r.x / Game.TILE);
        int startY = (int) Math.floor((double) r.y / Game.TILE);
        int endX = (int) Math.floor((double) (r.x + r.width - 1) / Game.TILE);
        int endY = (int) Math.floor((double) (r.y + r.height - 1) / Game.TILE);

        for (int tx = startX; tx <= endX; tx++) {
            for (int ty = startY; ty <= endY; ty++) {
                if (isWallTile(tx, ty)) return true;
            }
        }
        return false;
    }

    public Point randomCorridorFarFrom(double px, double py, int minTiles) {
        int tx = (int) (px / Game.TILE);
        int ty = (int) (py / Game.TILE);

        List<Point> list = new ArrayList<>();
        for (Point p : corridor) {
            int d = Math.abs(p.x - tx) + Math.abs(p.y - ty);
            if (d >= minTiles)
                list.add(p);
        }

        if (list.isEmpty()) return null;
        return list.get(rand.nextInt(list.size()));
    }

    public void removeWall(int x, int y) {
        Point p = new Point(x, y);

        // track drilling only if this was actually a wall
        if (!corridor.contains(p)) {
            drillCount++;
        }

        corridor.add(p);

        // attempt spawn
        trySpawnPocket(x, y);
    }

    // =========================
    // NEW LOGIC
    // =========================

    private void trySpawnPocket(int x, int y) {

        long now = System.currentTimeMillis();

        // cooldown
        if (now - lastPocketTime < 4000) return;

        // require some drilling effort
        if (drillCount < 12) return;

        // check if mostly walls around
        if (!isDenseWallArea(x, y)) return;

        // spawn mini maze
        generatePocket(x, y);

        drillCount = 0;
        lastPocketTime = now;
    }

    private boolean isDenseWallArea(int cx, int cy) {
        int walls = 0;
        int total = 0;

        for (int x = cx - 4; x <= cx + 4; x++) {
            for (int y = cy - 4; y <= cy + 4; y++) {
                total++;
                if (isWallTile(x, y)) walls++;
            }
        }

        return walls > total * 0.7; // mostly walls
    }

    private void generatePocket(int startX, int startY) {

        int size = 12 + rand.nextInt(6); // ~screen sized

        Set<Point> pocket = new HashSet<>();

        int x = startX;
        int y = startY;

        pocket.add(new Point(x, y));

        int dir = rand.nextInt(4);

        for (int i = 0; i < size * size; i++) {

            // random walk with bias = creates messy maze
            if (rand.nextFloat() < 0.3f)
                dir = rand.nextInt(4);

            switch (dir) {
                case 0 -> y--;
                case 1 -> x++;
                case 2 -> y++;
                case 3 -> x--;
            }

            Point p = new Point(x, y);
            pocket.add(p);

            // occasional branching
            if (rand.nextFloat() < 0.15f) {
                carveBranch(x, y, pocket, 6 + rand.nextInt(6));
            }
        }

        // merge into main corridor
        corridor.addAll(pocket);
    }

    private void carveBranch(int sx, int sy, Set<Point> pocket, int length) {

        int dir = rand.nextInt(4);
        int x = sx;
        int y = sy;

        for (int i = 0; i < length; i++) {

            switch (dir) {
                case 0 -> y--;
                case 1 -> x++;
                case 2 -> y++;
                case 3 -> x--;
            }

            pocket.add(new Point(x, y));

            if (rand.nextFloat() < 0.25f)
                dir = rand.nextInt(4);
        }
    }
}