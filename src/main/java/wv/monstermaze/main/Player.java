package wv.monstermaze.main;

import wv.monstermaze.fx.FootstepSound;
import wv.monstermaze.fx.SpeedWooshSound;

import java.awt.Rectangle;

public class Player {

    public double x;
    public double y;
    public final int size = Game.TILE / 2;

    private double lastFootstepX;
    private double lastFootstepY;

    public SpeedBoost speedBoost = new SpeedBoost();
    private PlayerFreeze freeze = new PlayerFreeze();

    private GameContext ctx;

    public Player(double x, double y) {
        this.x = x;
        this.y = y;
        this.lastFootstepX = x;
        this.lastFootstepY = y;
    }

    public void setContext(GameContext ctx) {
        this.ctx = ctx;
    }

    public void update() {
        speedBoost.update();
        updateWooshSound();

        // Player only moves if context exists, game UI is not active, and selection is not happening
        if (ctx == null) return;
        if (!ctx.settings.isActive() ) {
            handleMovement();
        }
    }

    private void handleMovement() {
        if (isFrozen()) return;

        double lx = ctx.controller.getLX();
        double ly = -ctx.controller.getLY();

        if (Math.abs(lx) < 0.15) lx = 0;
        if (Math.abs(ly) < 0.15) ly = 0;

        double len = Math.sqrt(lx * lx + ly * ly);
        if (len > 1) {
            lx /= len;
            ly /= len;
        }

        double speed = 4 * getSpeedMultiplier();
        double dx = lx * speed;
        double dy = ly * speed;

        Rectangle nextPos = getBounds(x + dx, y + dy);
        boolean collided = ctx.maze.isColliding(nextPos);

        if (!collided) {
            x += dx;
            y += dy;
        } else {
            Rectangle nextX = getBounds(x + dx, y);
            Rectangle nextY = getBounds(x, y + dy);

            boolean collideX = ctx.maze.isColliding(nextX);
            boolean collideY = ctx.maze.isColliding(nextY);

            if (speedBoost.isGreenBoost()) {
                if (collideX) destroyWalls(nextX);
                if (collideY) destroyWalls(nextY);
                x += dx;
                y += dy;
            } else {
                if (!collideX) x += dx;
                if (!collideY) y += dy;
            }
        }

        if (ctx.settings.areFootstepsEnabled()) {
            checkFootstep();
        }
    }

    private void destroyWalls(Rectangle r) {
        int startX = (int) Math.floor((double) r.x / Game.TILE);
        int startY = (int) Math.floor((double) r.y / Game.TILE);
        int endX = (int) Math.floor((double) (r.x + r.width - 1) / Game.TILE);
        int endY = (int) Math.floor((double) (r.y + r.height - 1) / Game.TILE);

        for (int tx = startX; tx <= endX; tx++) {
            for (int ty = startY; ty <= endY; ty++) {
                if (ctx.maze.isWallTile(tx, ty)) {
                    ctx.maze.removeWall(tx, ty);
                }
            }
        }
    }

    private void updateWooshSound() {
        double multiplier = getSpeedMultiplier();
        if (multiplier > 1.0) {
            SpeedWooshSound.start(multiplier - 1.0);
        } else {
            SpeedWooshSound.update(0);
        }
    }

    public boolean isFrozen() {
        return freeze.isFrozen();
    }

    public void freeze(double seconds) {
        freeze.trigger(seconds);
    }

    public double getSpeedMultiplier() {
        return speedBoost.getMultiplier();
    }

    public void triggerSpeedBoost(double mult, int seconds, SpeedBoost.Type type) {
        speedBoost.trigger(mult, seconds, type);
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

        if (Math.sqrt(dx * dx + dy * dy) >= Game.TILE / 2.0) {
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