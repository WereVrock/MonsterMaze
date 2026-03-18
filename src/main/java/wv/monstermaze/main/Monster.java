package wv.monstermaze.main;

import wv.monstermaze.fx.FootstepSound;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.Set;

public class Monster {

    private double x, y;
    private final BufferedImage img;
    private int targetTileX, targetTileY;
    private double speed = 1.5;
    private double lastFootstepX, lastFootstepY;
    private final SettingsMenu settingsMenu;
    public boolean vip = false;

    private static final double STICK_THRESHOLD = 0.2;
    private boolean flipped = false;
    private boolean flipping = false;
    private double flipProgress = 0;
    private final double flipSpeed = 0.08;
    private boolean joker;
    private boolean active = true;

    private boolean driller;
    private boolean swapper;

    private final Random random = new Random();

    public Monster(double x, double y, BufferedImage img, boolean vip, SettingsMenu settingsMenu) {
        this.x = x;
        this.y = y;
        this.img = img;
        this.vip = vip;
        this.settingsMenu = settingsMenu;

        this.targetTileX = (int) (x / Game.TILE);
        this.targetTileY = (int) (y / Game.TILE);

        this.lastFootstepX = x;
        this.lastFootstepY = y;

        double jokerChance = vip ? 0.6 : 0.2;
        double drillerChance = vip ? 0.15 : 0.05;

        double swapperChance = vip ? 0.20 : 0.05;

        this.joker = Math.random() < jokerChance;
        if (this.joker) {
            this.speed = 8;
        }

        this.driller = Math.random() < drillerChance;
        this.swapper = Math.random() < swapperChance;

    }

    public void update(Game game) {
        updateFlip();

        if (joker && random.nextDouble() < 0.01) {
            triggerFlip();
        }

        float stickX = game.getController().getLX2();
        float stickY = game.getController().getLY2();
        double sx = stickX, sy = -stickY;

        if (game.getController().isXPressedController2()) {
            triggerFlip();
        }

        if (Math.abs(sx) < STICK_THRESHOLD) {
            sx = 0;
        }
        if (Math.abs(sy) < STICK_THRESHOLD) {
            sy = 0;
        }

        if (sx != 0 || sy != 0) {
            manualMove(game.getMaze(), game.getVisibleTiles(), sx, sy);
        } else {
            double targetX = targetTileX * Game.TILE + Game.TILE / 2;
            double targetY = targetTileY * Game.TILE + Game.TILE / 2;
            double dx = targetX - x;
            double dy = targetY - y;
            if (Math.sqrt(dx * dx + dy * dy) < 1.0 || random.nextDouble() < 0.01) {
                Point p = game.getMaze().randomCorridorFarFrom(x, y, 2);
                if (p != null) {
                    setTargetTile(p.x, p.y);
                }
            }
            aiMove(game.getMaze(), game.getVisibleTiles());
        }

        if (driller) {
            destroyWalls(game);
        }

        if (swapper) {
            trySwapWithPlayer(game);
        }
    }

    private void trySwapWithPlayer(Game game) {

        if (random.nextDouble() > 0.003) {
            return;
        }

        if (!isVisible(game.getVisibleTiles())) {
            return;
        }

        Player player = game.getPlayer();

        double px = player.x;
        double py = player.y;

        game.monsterSpawner.triggerTeleport(player.x, player.y);
        game.monsterSpawner.triggerTeleport(this.x, this.y);

        player.x = this.x;
        player.y = this.y;

        this.x = px;
        this.y = py;

        targetTileX = (int) (x / Game.TILE);
        targetTileY = (int) (y / Game.TILE);
    }

    public void draw(Graphics2D g2, double cameraX, double cameraY) {
        double scaleX = getFlipScale();
        Graphics2D gFlip = (Graphics2D) g2.create();
        gFlip.translate(x - cameraX, y - cameraY);
        gFlip.scale(scaleX, 1);
        gFlip.drawImage(img, -img.getWidth() / 2, -img.getHeight() / 2, null);
        gFlip.dispose();
    }

    public void setTargetTile(int tx, int ty) {
        targetTileX = tx;
        targetTileY = ty;
    }

    public void triggerFlip() {
        if (!flipping) {
            flipping = true;
            flipProgress = 0;
        }
    }

    public boolean isActive() {
        return active;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean isDriller() {
        return driller;
    }

    private void updateFlip() {
        if (!flipping) {
            return;
        }
        flipProgress += flipSpeed;
        if (flipProgress >= 0.5 && flipProgress - flipSpeed < 0.5) {
            flipped = !flipped;
        }
        if (flipProgress >= 1) {
            flipProgress = 0;
            flipping = false;
        }
    }

    private double getFlipScale() {
        if (!flipping) {
            return flipped ? -1 : 1;
        }
        double scale = flipProgress < 0.5 ? 1 - flipProgress * 2 : (flipProgress - 0.5) * 2;
        return flipped ? -scale : scale;
    }

    private void manualMove(MazeGenerator maze, Set<Point> visibleTiles, double sx, double sy) {
        double len = Math.sqrt(sx * sx + sy * sy);
        if (len > 1) {
            sx /= len;
            sy /= len;
        }
        double moveSpeed = speed * 3;
        double nx = sx * moveSpeed, ny = sy * moveSpeed;

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

        targetTileX = (int) (x / Game.TILE);
        targetTileY = (int) (y / Game.TILE);

        if (moved && isVisible(visibleTiles) && settingsMenu.areFootstepsEnabled()) {
            checkFootstep();
        }
    }

    private void aiMove(MazeGenerator maze, Set<Point> visibleTiles) {
        double targetX = targetTileX * Game.TILE + Game.TILE / 2;
        double targetY = targetTileY * Game.TILE + Game.TILE / 2;
        double dx = targetX - x, dy = targetY - y;
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
        Point currentTile = new Point((int) (x / Game.TILE), (int) (y / Game.TILE));
        return visibleTiles.contains(currentTile);
    }

    private void checkFootstep() {
        double dx = x - lastFootstepX, dy = y - lastFootstepY;
        if (Math.sqrt(dx * dx + dy * dy) >= Game.TILE / 2.0) {
            FootstepSound.play();
            lastFootstepX = x;
            lastFootstepY = y;
        }
    }

    private void destroyWalls(Game game) {

        MazeGenerator maze = game.getMaze();
        int radius = 2;

        int centerX = (int) (x / Game.TILE);
        int centerY = (int) (y / Game.TILE);

        boolean brokeWall = false;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {

                int tx = centerX + dx;
                int ty = centerY + dy;

                if (maze.isWallTile(tx, ty)) {
                    maze.removeWall(tx, ty);
                    brokeWall = true;
                }
            }
        }

        if (brokeWall) {
            game.monsterSpawner.triggerDrillEffect(x, y);
        }
    }

    public double distance(double px, double py) {
        double dx = px - x, dy = py - y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return String.format("Monster[x=%.2f, y=%.2f]", x, y);
    }
}
