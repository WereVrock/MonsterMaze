package wv.monstermaze.main;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

public class Game extends JPanel implements Runnable {

public static final int TILE = 96;
public static final int WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width / TILE;
public static final int HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height / TILE;

private Player player;
private Monster monster;
private MazeGenerator maze;

private BufferedImage playerImg;
private java.util.List<BufferedImage> playerImages;
private java.util.List<BufferedImage> monsterImages;

private boolean selectingPlayer = true;
private int playerSelectionIndex = 0;
private long lastInputTime = 0;

private ControllerInput controller;
private HappyBumpEffect happyFx;
private PlayerSelectionManager selectionManager;
private SettingsMenu settingsMenu;

private Set<Point> visibleTiles = new HashSet<>();

private ToiletManager toilets = new ToiletManager();
private PoopBar poopBar = new PoopBar();

private boolean wasOnToilet = false;

private SpeedEffectVFX speedFx = new SpeedEffectVFX();

public Game() {

    setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());

    maze = new MazeGenerator();
    player = new Player(2 * TILE + TILE / 2, 2 * TILE + TILE / 2);

    controller = new ControllerInput();
    happyFx = new HappyBumpEffect();

    ImageLoader loader = new ImageLoader();

    playerImages = loader.loadImages("player", TILE);
    monsterImages = loader.loadImages("monsters", TILE);

    if (!playerImages.isEmpty()) {
        playerImg = playerImages.get(0);
    }

    selectionManager = new PlayerSelectionManager(playerImages);
    settingsMenu = new SettingsMenu();

    new Thread(this).start();
}

@Override
public void run() {

    while (true) {

        update();
        repaint();

        try {
            Thread.sleep(16);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

private void update() {

    controller.poll();

    player.update();

    speedFx.update();

    if(player.getSpeedMultiplier() > 1.0){

        boolean high = player.getSpeedMultiplier() > 1.5;

        speedFx.spawnBoostTrail(player.x,player.y,high);
    }

    if (settingsMenu.isToiletSystemEnabled()) {
        poopBar.update();
    }

    if (controller.getLeftTrigger() > 0.7f) {
        settingsMenu.toggleActive();
    }

    if (settingsMenu.isActive()) {
        settingsMenu.update(controller.getLX(), controller.getLY(), controller.getRightTrigger() > 0.7f);
        return;
    }

    if (controller.getRightTrigger() > 0.7f) {
        restartToSelection();
        return;
    }

    if (selectingPlayer) {

        if (selectionManager.updateSelection(controller.getLX(), controller.getLY(), lastInputTime)) {

            playerSelectionIndex = selectionManager.getSelectionIndex();
            playerImg = playerImages.get(playerSelectionIndex);
            selectingPlayer = false;

        } else {
            lastInputTime = System.currentTimeMillis();
        }

        return;
    }

    updatePlayerMovement();

    maze.ensureArea(player.x, player.y);

    checkVisibleTiles();

    if (settingsMenu.isToiletSystemEnabled()) {

        boolean onToilet = toilets.isPlayerOnToilet(player);

        if (controller.isXPressed()) {

            if (onToilet) {

                player.freeze(2);

                speedFx.triggerToiletBurst(player.x,player.y);

                if (poopBar.isGreen()) {

                    PoopSound.play();
                    player.triggerSpeedBoost(1.8,15);
                    poopBar.reset();

                } else if (poopBar.isRed()) {

                    PoopSound.play();
                    player.triggerSpeedBoost(1.35,15);
                    poopBar.reset();
                }
            }
        }

        wasOnToilet = onToilet;
    }

    updateMonster();

    happyFx.update();

    if (monster != null && player.distance(monster.x, monster.y) < 32) {
        happyFx.trigger(monster.x, monster.y);
        monster = null;
    }
}

private void updatePlayerMovement() {

    if(player.isFrozen()) return;

    double lx = controller.getLX();
    double ly = -controller.getLY();

    if (Math.abs(lx) < 0.15) lx = 0;
    if (Math.abs(ly) < 0.15) ly = 0;

    double len = Math.sqrt(lx * lx + ly * ly);

    if (len > 1) {
        lx /= len;
        ly /= len;
    }

    double speed = 4 * player.getSpeedMultiplier();

    double dx = lx * speed;
    double dy = ly * speed;

    Rectangle nextPos = player.getBounds(player.x + dx, player.y + dy);

    if (!maze.isColliding(nextPos)) {

        player.x += dx;
        player.y += dy;

    } else {

        Rectangle nextX = player.getBounds(player.x + dx, player.y);
        Rectangle nextY = player.getBounds(player.x, player.y + dy);

        if (!maze.isColliding(nextX)) player.x += dx;
        if (!maze.isColliding(nextY)) player.y += dy;
    }

    if (settingsMenu.areFootstepsEnabled()) {
        player.checkFootstep();
    }
}

private void restartToSelection() {

    selectingPlayer = true;

    maze = new MazeGenerator();
    player = new Player(2 * TILE + TILE / 2, 2 * TILE + TILE / 2);
    monster = null;

    visibleTiles.clear();

    happyFx = new HappyBumpEffect();

    toilets = new ToiletManager();
    poopBar = new PoopBar();

    speedFx = new SpeedEffectVFX();
}

private void checkVisibleTiles() {

    int screenCenterX = WIDTH * TILE / 2;
    int screenCenterY = HEIGHT * TILE / 2;

    double cameraX = player.x - screenCenterX;
    double cameraY = player.y - screenCenterY;

    int startX = (int) (cameraX / TILE) - 1;
    int startY = (int) (cameraY / TILE) - 1;

    int endX = startX + WIDTH + 2;
    int endY = startY + HEIGHT + 2;

    Set<Point> newVisible = new HashSet<>();

    for (int y = startY; y < endY; y++) {
        for (int x = startX; x < endX; x++) {

            Point p = new Point(x, y);
            newVisible.add(p);

            if (!visibleTiles.contains(p)) {

                if (settingsMenu.isToiletSystemEnabled()) {
                    toilets.onTileGenerated(x, y, maze);
                }

                onTileEntered(p);
            }
        }
    }

    visibleTiles = newVisible;
}

private void onTileEntered(Point tile) {

    if (monster != null) return;
    if (maze.isWallTile(tile.x, tile.y)) return;
    if (Math.random() > 0.05) return;
    if (monsterImages.isEmpty()) return;

    BufferedImage img = monsterImages.get(new java.util.Random().nextInt(monsterImages.size()));

    double mx = tile.x * TILE + TILE / 2;
    double my = tile.y * TILE + TILE / 2;

    monster = new Monster(mx, my, img, settingsMenu);
}

private void updateMonster() {

    if (monster == null) return;

    if (controller.isXPressedController2()) {
        monster.triggerFlip();
    }

    if (Math.random() < 0.01) {

        Point p = maze.randomCorridorFarFrom(monster.x, monster.y, 2);

        if (p != null) {
            monster.setTargetTile(p.x, p.y);
        }
    }

    monster.update(maze, visibleTiles, controller.getLX2(), controller.getLY2());

    if (player.distance(monster.x, monster.y) < 32) {
        happyFx.trigger(monster.x, monster.y);
        monster = null;
        return;
    }

    if (player.distance(monster.x, monster.y) > TILE * 25) {
        monster = null;
    }
}

@Override
protected void paintComponent(Graphics g) {

    super.paintComponent(g);

    Graphics2D g2 = (Graphics2D) g;

    if (selectingPlayer) {
        selectionManager.drawSelection(g2, getWidth(), getHeight(), TILE);
        return;
    }

    int screenCenterX = WIDTH * TILE / 2;
    int screenCenterY = HEIGHT * TILE / 2;

    double cameraX = player.x - screenCenterX;
    double cameraY = player.y - screenCenterY;

    for (int wy = (int) (cameraY / TILE) - 1; wy < (int) (cameraY / TILE) + HEIGHT + 1; wy++) {
        for (int wx = (int) (cameraX / TILE) - 1; wx < (int) (cameraX / TILE) + WIDTH + 1; wx++) {

            int sx = wx * TILE - (int) cameraX;
            int sy = wy * TILE - (int) cameraY;

            g2.setColor(maze.isWallTile(wx, wy) ? Color.DARK_GRAY : Color.GRAY);
            g2.fillRect(sx, sy, TILE, TILE);
        }
    }

    if (settingsMenu.isToiletSystemEnabled()) {
        toilets.draw(g2, cameraX, cameraY);
    }

    if (monster != null) {

        double scaleX = monster.getFlipScale();

        Graphics2D gFlip = (Graphics2D) g2.create();

        gFlip.translate(monster.x - cameraX, monster.y - cameraY);
        gFlip.scale(scaleX, 1);

        gFlip.drawImage(
                monster.img,
                -monster.img.getWidth() / 2,
                -monster.img.getHeight() / 2,
                null
        );

        gFlip.dispose();
    }

    happyFx.draw(g2, cameraX, cameraY);

    speedFx.draw(g2,cameraX,cameraY);

    int playerScreenX = (int) (player.x - cameraX - playerImg.getWidth() / 2);
    int playerScreenY = (int) (player.y - cameraY - playerImg.getHeight() / 2);

    g2.drawImage(playerImg, playerScreenX, playerScreenY, null);

    if (settingsMenu.isToiletSystemEnabled()) {
        poopBar.draw(g2, getWidth(), toilets.isPlayerOnToilet(player));
    }

    if (settingsMenu.isActive()) {
        settingsMenu.draw(g2, getWidth(), getHeight());
    }
}

public static void main(String[] args) {

    JFrame f = new JFrame("Labyrinth");
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setUndecorated(true);

    GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

    Game game = new Game();

    f.add(game);

    device.setFullScreenWindow(f);

    f.validate();
}
}