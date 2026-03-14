package wv.monstermaze.main;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Monster {

    public double x;
    public double y;

    public BufferedImage img;

    private int targetTileX;
    private int targetTileY;
    private double speed = 1.5; // pixels per frame

    public Monster(double x, double y, BufferedImage img) {
        this.x = x;
        this.y = y;
        this.img = img;
        this.targetTileX = (int) (x / Game.TILE);
        this.targetTileY = (int) (y / Game.TILE);
    }

    public void setTargetTile(int tx, int ty) {
        this.targetTileX = tx;
        this.targetTileY = ty;
    }

    public void update(MazeGenerator maze) {
        double targetX = targetTileX * Game.TILE + Game.TILE / 2;
        double targetY = targetTileY * Game.TILE + Game.TILE / 2;

        double dx = targetX - x;
        double dy = targetY - y;

        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist < speed) {
            x = targetX;
            y = targetY;
        } else {
            // Normalize direction
            double nx = dx / dist * speed;
            double ny = dy / dist * speed;

            // Check collision separately for X and Y like player
            Rectangle nextX = new Rectangle((int) (x + nx - Game.TILE / 4), (int) (y - Game.TILE / 4), Game.TILE / 2, Game.TILE / 2);
            Rectangle nextY = new Rectangle((int) (x - Game.TILE / 4), (int) (y + ny - Game.TILE / 4), Game.TILE / 2, Game.TILE / 2);

            if (!maze.isColliding(nextX)) {
                x += nx;
            }

            if (!maze.isColliding(nextY)) {
                y += ny;
            }
        }
    }
}