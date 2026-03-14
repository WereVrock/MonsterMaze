package wv.monstermaze.main;

import java.awt.Rectangle;

public class Player {

    public double x;
    public double y;
    public final int size = Game.TILE / 2; // half-tile size for collision

    public Player(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double distance(double ox, double oy) {
        double dx = x - ox;
        double dy = y - oy;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public Rectangle getBounds(double nextX, double nextY) {
        int half = size / 2;
        // use floor for integer conversion to avoid rounding errors
        int bx = (int) Math.floor(nextX - half);
        int by = (int) Math.floor(nextY - half);
        return new Rectangle(bx, by, size, size);
    }

    @Override
    public String toString() {
        return String.format("Player[x=%.2f, y=%.2f]", x, y);
    }
}