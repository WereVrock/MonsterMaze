package wv.monstermaze.main;

import java.awt.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MazeGenerator {

    private Set<Point> corridor = new HashSet<>();
    private int dir = 0; // 0 up 1 right 2 down 3 left
    private int cx = 0;
    private int cy = 0;
    private Random rand = new Random();

    public MazeGenerator() {
        // Create initial 5x5 free area at top-left
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                corridor.add(new Point(x, y));
            }
        }
        // Start maze generation from the edge of the free area
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

        java.util.List<Point> list = new java.util.ArrayList<>();
        for (Point p : corridor) {
            int d = Math.abs(p.x - tx) + Math.abs(p.y - ty);
            if (d >= minTiles)
                list.add(p);
        }

        if (list.isEmpty()) return null;

        return list.get(rand.nextInt(list.size()));
    }

    public void removeWall(int x, int y) {
        corridor.add(new Point(x, y));
    }
}