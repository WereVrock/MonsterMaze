package wv.monstermaze.main;

import wv.monstermaze.images.MonsterImagePool;
import wv.monstermaze.images.ImageLoader;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PassiveMonsterSpawner {

    private final MonsterSpawner spawner;
    private final Game game;
    private final Random random = new Random();

    private long lastCheckTime = System.currentTimeMillis();

    private static final long INTERVAL_MS = 3000;
    private static final double SPAWN_CHANCE = 01.25;

    private static final int TARGET_DISTANCE = 8;
    private static final int DISTANCE_TOLERANCE = 1;

    public PassiveMonsterSpawner(MonsterSpawner spawner, Game game) {
        this.spawner = spawner;
        this.game = game;
    }

    public void update() {

        long now = System.currentTimeMillis();

        if (now - lastCheckTime < INTERVAL_MS)
            return;

        lastCheckTime = now;

        if (isAnyMonsterVisible())
            return;

        if (random.nextDouble() > SPAWN_CHANCE)
            return;

        spawnAtDistance();
    }

    private boolean isAnyMonsterVisible() {

        for (Monster m : spawner.getMonsters()) {

            double screenX = m.getX() - game.camera.getX();
            double screenY = m.getY() - game.camera.getY();

            if (screenX >= 0 && screenX <= game.getWidth() &&
                screenY >= 0 && screenY <= game.getHeight())
                return true;
        }

        return false;
    }

    private void spawnAtDistance() {

        MonsterImagePool pool = spawner.pool;

        if (pool.normal.isEmpty() && pool.misc.isEmpty() && pool.vip.isEmpty())
            return;

        Player player = game.getPlayer();

        int playerTileX = (int)(player.x / Game.TILE);
        int playerTileY = (int)(player.y / Game.TILE);

        List<Point> candidates = new ArrayList<>();

        int range = TARGET_DISTANCE + DISTANCE_TOLERANCE;

        for (int y = playerTileY - range; y <= playerTileY + range; y++) {
            for (int x = playerTileX - range; x <= playerTileX + range; x++) {

                int dist = Math.abs(x - playerTileX) + Math.abs(y - playerTileY);

                if (dist >= TARGET_DISTANCE - DISTANCE_TOLERANCE &&
                    dist <= TARGET_DISTANCE + DISTANCE_TOLERANCE) {

                    if (!game.getMaze().isWallTile(x, y)) {
                        candidates.add(new Point(x, y));
                    }
                }
            }
        }

        if (candidates.isEmpty())
            return;

        Point spawnTile = candidates.get(random.nextInt(candidates.size()));

        double x = spawnTile.x * Game.TILE + Game.TILE / 2.0;
        double y = spawnTile.y * Game.TILE + Game.TILE / 2.0;

        ImageLoader.LoadedImage loaded = pool.getRandom(random);
        if (loaded == null) return;

        Monster m = new Monster(x, y, loaded.image, loaded.vip, game.getSettingsMenu());
        spawner.getMonsters().add(m);
    }
}