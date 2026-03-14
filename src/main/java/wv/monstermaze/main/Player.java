package wv.monstermaze.main;

import java.awt.Rectangle;

public class Player {

    public double x;
    public double y;
    public final int size = Game.TILE / 2;

    private double lastFootstepX;
    private double lastFootstepY;

    public Player(double x, double y) {
        this.x = x;
        this.y = y;
        this.lastFootstepX = x;
        this.lastFootstepY = y;
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
        double dx = x - lastFootstepX;
        double dy = y - lastFootstepY;

        if (Math.sqrt(dx * dx + dy * dy) >= Game.TILE / 2.0) { // half-tile check
            FootstepSound.play();
            lastFootstepX = x;
            lastFootstepY = y;
        }
    }

    @Override
    public String toString() {
        return String.format("Player[x=%.2f, y=%.2f]", x, y);
    }
}