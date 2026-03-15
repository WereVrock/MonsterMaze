package wv.monstermaze.main;

import java.awt.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class ToiletManager {

    private Set<Point> toilets = new HashSet<>();
    private Random rand = new Random();

    public void onTileGenerated(int x, int y, MazeGenerator maze) {

        if (maze.isWallTile(x, y)) return;

        if (rand.nextDouble() < 0.02) {
            toilets.add(new Point(x, y));
        }
    }

    public boolean isToilet(int x, int y) {
        return toilets.contains(new Point(x, y));
    }

    public boolean isPlayerOnToilet(Player p) {

        int tx = (int)(p.x / Game.TILE);
        int ty = (int)(p.y / Game.TILE);

        return toilets.contains(new Point(tx, ty));
    }

    public void draw(Graphics2D g2, double camX, double camY) {

        for (Point t : toilets) {

            int sx = t.x * Game.TILE - (int)camX;
            int sy = t.y * Game.TILE - (int)camY;

            g2.setColor(Color.WHITE);
            g2.fillRect(sx + 20, sy + 20, Game.TILE - 40, Game.TILE - 40);

            g2.setColor(Color.BLACK);
            g2.drawString("🚽", sx + Game.TILE/2 - 8, sy + Game.TILE/2 + 6);
        }
    }
}