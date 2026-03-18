package wv.monstermaze.main;

import wv.monstermaze.fx.HappyBumpEffect;
import wv.monstermaze.fx.DrillWallEffect;
import wv.monstermaze.fx.TeleportSound;
import wv.monstermaze.fx.TeleportSwapFX;
import wv.monstermaze.images.MonsterImagePool;
import wv.monstermaze.images.ImageLoader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MonsterSpawner {

    final Game game;
    final MonsterImagePool pool;
    private final Random random = new Random();
    private final List<Monster> monsters = new ArrayList<>();
    private final double DESPAWN_DISTANCE;

    private final Set<Point> knownVisibleTiles = new java.util.HashSet<>();
    private final DrillWallEffect drillEffect = new DrillWallEffect();
    private final TeleportSwapFX teleportFX = new TeleportSwapFX();

    private final PassiveMonsterSpawner passiveSpawner;

    public MonsterSpawner(Game game, MonsterImagePool pool) {
        this.game = game;
        this.pool = pool;
        this.DESPAWN_DISTANCE = Game.TILE * Game.WIDTH * 1.5;

        this.passiveSpawner = new PassiveMonsterSpawner(this, game);
    }

    public List<Monster> getMonsters() { return monsters; }

    public void triggerDrillEffect(double x, double y) { drillEffect.trigger(x, y); }

    public void triggerTeleport(double x,double y){
        teleportFX.trigger(x,y);
        TeleportSound.play();
    }

    public void updateMonsters(HappyBumpEffect happyFx) {

        detectNewVisibleTiles();

        passiveSpawner.update();

        List<Monster> toRemove = new ArrayList<>();
        Player player = game.getPlayer();

        for (Monster m : monsters) {

            m.update(game);

            double distanceToPlayer = m.distance(player.x, player.y);

            if (distanceToPlayer < 32) {
                happyFx.trigger(m.getX(), m.getY());
                toRemove.add(m);
                continue;
            }

            if (distanceToPlayer > DESPAWN_DISTANCE) {
                toRemove.add(m);
            }
        }

        monsters.removeAll(toRemove);

        drillEffect.update();
        teleportFX.update();
    }

    public void drawMonsters(Graphics2D g2, double cameraX, double cameraY) {

        for (Monster m : monsters)
            m.draw(g2, cameraX, cameraY);

        drillEffect.draw(g2, cameraX, cameraY);
        teleportFX.draw(g2, cameraX, cameraY);
    }

    private void detectNewVisibleTiles() {

        Set<Point> currentVisible = game.getVisibleTiles();

        for (Point tile : currentVisible) {

            if (!knownVisibleTiles.contains(tile))
                trySpawnMonster(tile);
        }

        knownVisibleTiles.clear();
        knownVisibleTiles.addAll(currentVisible);
    }

    private void trySpawnMonster(Point tile) {

        if ((pool.normal.isEmpty() && pool.misc.isEmpty() && pool.vip.isEmpty()) || !monsters.isEmpty())
            return;

        if (random.nextDouble() > 0.05)
            return;

        if (game.getMaze().isWallTile(tile.x, tile.y))
            return;

        double tileCenterX = tile.x * Game.TILE + Game.TILE / 2.0;
        double tileCenterY = tile.y * Game.TILE + Game.TILE / 2.0;

        double playerX = game.getPlayer().x;
        double playerY = game.getPlayer().y;

        double dist = Math.hypot(playerX - tileCenterX, playerY - tileCenterY);

        if (dist < Game.TILE * 2)
            return;

        ImageLoader.LoadedImage loaded = pool.getRandom(random);
        if (loaded == null) return;

        Monster m = new Monster(tileCenterX, tileCenterY, loaded.image, loaded.vip, game.getSettingsMenu());
        monsters.add(m);
    }
}