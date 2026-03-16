package wv.monstermaze.main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

public class MonsterSpawner {

    private final Game game;
    private final List<BufferedImage> monsterImages;
    private final Random random = new Random();

    public MonsterSpawner(Game game, List<BufferedImage> monsterImages){
        this.game = game;
        this.monsterImages = monsterImages;
    }

    /**
     * Attempt to spawn a monster in the visible area.
     * Returns a new Monster if spawned, or null otherwise.
     */
    public Monster trySpawnMonster(){
        for (Point tile : game.getVisibleTiles()){
            // Skip walls
            if (game.getMaze().isWallTile(tile.x, tile.y)) continue;

            // Random chance
            if (random.nextDouble() > 0.05) continue;

            // Skip if no monster images loaded
            if (monsterImages.isEmpty()) continue;

            // Ensure the spawn tile is far enough from the player
            double tileCenterX = tile.x * Game.TILE + Game.TILE / 2.0;
            double tileCenterY = tile.y * Game.TILE + Game.TILE / 2.0;
            double playerX = game.getPlayer().x;
            double playerY = game.getPlayer().y;
            double dist = Math.hypot(playerX - tileCenterX, playerY - tileCenterY);
            if (dist < Game.TILE * 2) continue; // at least 2 tiles away

            // Pick random monster image
            BufferedImage img = monsterImages.get(random.nextInt(monsterImages.size()));

            return new Monster(tileCenterX, tileCenterY, img, game.getSettingsMenu());
        }
        return null;
    }

    /**
     * Called when a new tile becomes visible.
     * Optional: can spawn monsters here too.
     */
    public void onTileEntered(Point tile){
        // For now, spawning is handled in trySpawnMonster() to avoid multiple spawns
    }
}