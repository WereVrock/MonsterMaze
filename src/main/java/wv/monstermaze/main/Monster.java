package wv.monstermaze.main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Set;

public class Monster {

    public double x;
    public double y;
    public BufferedImage img;

    private int targetTileX;
    private int targetTileY;
    private double speed = 1.5;

    private double lastFootstepX;
    private double lastFootstepY;

    private SettingsMenu settingsMenu;

    private static final double STICK_THRESHOLD = 0.2;

    public Monster(double x, double y, BufferedImage img, SettingsMenu settingsMenu) {
        this.x = x;
        this.y = y;
        this.img = img;
        this.settingsMenu = settingsMenu;
        this.targetTileX = (int) (x / Game.TILE);
        this.targetTileY = (int) (y / Game.TILE);
        this.lastFootstepX = x;
        this.lastFootstepY = y;
    }

    public void setTargetTile(int tx, int ty) {
        this.targetTileX = tx;
        this.targetTileY = ty;
    }

    public void update(MazeGenerator maze, Set<Point> visibleTiles, float stickX, float stickY) {

        double sx = stickX;
        double sy = -stickY;

        if (Math.abs(sx) < STICK_THRESHOLD) sx = 0;
        if (Math.abs(sy) < STICK_THRESHOLD) sy = 0;

        if (sx != 0 || sy != 0) {
            manualMove(maze, visibleTiles, sx, sy);
            return;
        }

        aiMove(maze, visibleTiles);
    }

    private void manualMove(MazeGenerator maze, Set<Point> visibleTiles, double sx, double sy) {

        double len = Math.sqrt(sx * sx + sy * sy);
        if (len > 1) {
            sx /= len;
            sy /= len;
        }

        double moveSpeed = speed * 3;

        double nx = sx * moveSpeed;
        double ny = sy * moveSpeed;

        Rectangle nextX = new Rectangle((int) (x + nx - Game.TILE / 4), (int) (y - Game.TILE / 4), Game.TILE / 2, Game.TILE / 2);
        Rectangle nextY = new Rectangle((int) (x - Game.TILE / 4), (int) (y + ny - Game.TILE / 4), Game.TILE / 2, Game.TILE / 2);

        boolean moved = false;

        if (!maze.isColliding(nextX)) {
            x += nx;
            moved = true;
        }

        if (!maze.isColliding(nextY)) {
            y += ny;
            moved = true;
        }

        if (moved && isVisible(visibleTiles) && settingsMenu.areFootstepsEnabled()) {
            checkFootstep();
        }
    }

    private void aiMove(MazeGenerator maze, Set<Point> visibleTiles) {

        double targetX = targetTileX * Game.TILE + Game.TILE / 2;
        double targetY = targetTileY * Game.TILE + Game.TILE / 2;

        double dx = targetX - x;
        double dy = targetY - y;

        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist < speed) {
            x = targetX;
            y = targetY;
        } else {
            double nx = dx / dist * speed;
            double ny = dy / dist * speed;

            Rectangle nextX = new Rectangle((int) (x + nx - Game.TILE / 4), (int) (y - Game.TILE / 4), Game.TILE / 2, Game.TILE / 2);
            Rectangle nextY = new Rectangle((int) (x - Game.TILE / 4), (int) (y + ny - Game.TILE / 4), Game.TILE / 2, Game.TILE / 2);

            boolean moved = false;

            if (!maze.isColliding(nextX)) {
                x += nx;
                moved = true;
            }

            if (!maze.isColliding(nextY)) {
                y += ny;
                moved = true;
            }

            if (moved && isVisible(visibleTiles) && settingsMenu.areFootstepsEnabled()) {
                checkFootstep();
            }
        }
    }

    private boolean isVisible(Set<Point> visibleTiles) {
        Point currentTile = new Point((int)(x / Game.TILE), (int)(y / Game.TILE));
        return visibleTiles.contains(currentTile);
    }

    private void checkFootstep() {
        double dx = x - lastFootstepX;
        double dy = y - lastFootstepY;

        if (Math.sqrt(dx * dx + dy * dy) >= Game.TILE / 2.0) {
            FootstepSound.play();
            lastFootstepX = x;
            lastFootstepY = y;
        }
    }

    @Override
    public String toString() {
        return String.format("Monster[x=%.2f, y=%.2f]", x, y);
    }
}