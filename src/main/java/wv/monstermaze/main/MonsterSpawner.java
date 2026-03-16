package wv.monstermaze.main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MonsterSpawner {

    private final Game game;
    private final List<BufferedImage> monsterImages;
    private final Random random = new Random();
    private final List<Monster> monsters = new ArrayList<>();
    private final double DESPAWN_DISTANCE;

    private final Set<Point> knownVisibleTiles = new HashSet<>();

    public MonsterSpawner(Game game, List<BufferedImage> monsterImages) {
        this.game = game;
        this.monsterImages = monsterImages;
        this.DESPAWN_DISTANCE = Game.TILE * Game.WIDTH * 1.5; // 1.5 screen width
    }

    public List<Monster> getMonsters() {
        return monsters;
    }

    /**
     * Updates all monsters and handles:
     * - spawning on newly visible tiles
     * - despawning
     * - player collision
     */
    public void updateMonsters(HappyBumpEffect happyFx) {
        detectNewVisibleTiles();

        List<Monster> toRemove = new ArrayList<>();
        Player player = game.getPlayer();

        for (Monster m : monsters) {
            m.update(game);

            double distanceToPlayer = m.distance(player.x, player.y);

            // Collision with player
            if (distanceToPlayer < 32) {
                happyFx.trigger(m.getX(), m.getY());
                toRemove.add(m);
                continue;
            }

            // Despawn if too far
            if (distanceToPlayer > DESPAWN_DISTANCE) {
                toRemove.add(m);
            }
        }

        monsters.removeAll(toRemove);
    }

    /**
     * Draw all monsters
     */
    public void drawMonsters(Graphics2D g2, double cameraX, double cameraY) {
        for (Monster m : monsters) {
            m.draw(g2, cameraX, cameraY);
        }
    }

    /**
     * Detects newly visible tiles and attempts to spawn monsters on them
     */
    private void detectNewVisibleTiles() {
        Set<Point> currentVisible = game.getVisibleTiles();
        for (Point tile : currentVisible) {
            if (!knownVisibleTiles.contains(tile)) {
                // New tile detected
                trySpawnMonster(tile);
            }
        }
        knownVisibleTiles.clear();
        knownVisibleTiles.addAll(currentVisible);
    }

    /**
     * Attempts to spawn a monster on a tile if no visible monsters exist
     */
    private void trySpawnMonster(Point tile) {
        if (monsterImages.isEmpty() || !monsters.isEmpty()) return;
        if (random.nextDouble() > 0.05) return;

        if (game.getMaze().isWallTile(tile.x, tile.y)) return;

        double tileCenterX = tile.x * Game.TILE + Game.TILE / 2.0;
        double tileCenterY = tile.y * Game.TILE + Game.TILE / 2.0;

        double playerX = game.getPlayer().x;
        double playerY = game.getPlayer().y;
        double dist = Math.hypot(playerX - tileCenterX, playerY - tileCenterY);

        if (dist < Game.TILE * 2) return;

        BufferedImage img = monsterImages.get(random.nextInt(monsterImages.size()));
        Monster m = new Monster(tileCenterX, tileCenterY, img, game.getSettingsMenu());
        monsters.add(m);
    }
}