package wv.monstermaze.main;

import java.awt.Rectangle;

public class Player {

    public double x;
    public double y;
    public final int size = Game.TILE / 2;

    private int lastTileX;
    private int lastTileY;

    public Player(double x, double y) {
        this.x = x;
        this.y = y;
        this.lastTileX = (int)(x / Game.TILE);
        this.lastTileY = (int)(y / Game.TILE);
    }

    public double distance(double ox, double oy) {
        double dx = x - ox;
        double dy = y - oy;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public Rectangle getBounds(double nextX, double nextY) {
        int half = size / 2;
        int bx = (int) Math.floor(nextX - half);
        int by = (int) Math.floor(nextY - half);
        return new Rectangle(bx, by, size, size);
    }

    public void checkFootstep() {
        int currentTileX = (int)(x / Game.TILE);
        int currentTileY = (int)(y / Game.TILE);

        if (currentTileX != lastTileX || currentTileY != lastTileY) {
            FootstepSound.play(); // player sound
            lastTileX = currentTileX;
            lastTileY = currentTileY;
        }
    }

    @Override
    public String toString() {
        return String.format("Player[x=%.2f, y=%.2f]", x, y);
    }
}