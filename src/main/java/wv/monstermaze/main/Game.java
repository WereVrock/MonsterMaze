package wv.monstermaze.main;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.Set;

public class Game extends JPanel implements Runnable {

    public static final int TILE = 96;
    public static final int WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width / TILE + 2;
    public static final int HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height / TILE + 2;
    private static final int VIEW_PADDING = 4;

    private Player player;
    private MazeGenerator maze;

    private BufferedImage playerImg;
    private List<BufferedImage> playerImages;
    MonsterSpawner monsterSpawner;

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
    private SpeedParticles speedFx = new SpeedParticles();
    private SpeedFXSystem speedFXSystem = new SpeedFXSystem();
    private ToiletActionHandler toiletHandler;

    Camera camera;

    public Game() {
        setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
        maze = new MazeGenerator();
        player = new Player(2 * TILE + TILE / 2, 2 * TILE + TILE / 2);

        controller = new ControllerInput();
        happyFx = new HappyBumpEffect();

        ImageLoader loader = new ImageLoader();

        // --- load player images ---
        List<ImageLoader.LoadedImage> loadedPlayers = loader.loadImages("player", TILE);
        playerImages = new ArrayList<>();
        for (ImageLoader.LoadedImage li : loadedPlayers) playerImages.add(li.image);

        if (!playerImages.isEmpty()) playerImg = playerImages.get(0);

        // --- load monster images through centralized loader ---
        ImageLoader.MonsterImagePool monsterPool = loader.setupMonsterImages(TILE);
        monsterSpawner = new MonsterSpawner(this, monsterPool);

        selectionManager = new PlayerSelectionManager(playerImages);
        settingsMenu = new SettingsMenu();

        camera = new Camera(this, WIDTH, HEIGHT, TILE);

        // --- initialize modular toilet handler ---
        toiletHandler = new ToiletActionHandler(poopBar, speedFx, speedFXSystem, toilets);

        new Thread(this).start();
    }

    @Override
    public void run() {
        while (true) {
            update();
            repaint();
            try { Thread.sleep(16); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void update() {
        controller.poll();
        player.update();

        if (settingsMenu.isSpeedVfxEnabled()) {
            speedFx.update();
            speedFXSystem.update();
        }

        if (player.getSpeedMultiplier() > 1.0 && settingsMenu.isSpeedVfxEnabled()) {
            boolean high = player.getSpeedMultiplier() > 1.5;
            speedFx.spawnBoostTrail(player.x, player.y, high);
            speedFXSystem.spawnSpeedEffects(player.x, player.y, player.getSpeedMultiplier());
        }

        if (settingsMenu.isToiletSystemEnabled()) poopBar.update();

        if (controller.getStartButton() > 0.7f) settingsMenu.toggleActive();
        if (settingsMenu.isActive()) {
            settingsMenu.update(controller.getLX(), controller.getLY(), controller.getRightTrigger() > 0.7f);
            return;
        }

        if (controller.getRightTrigger() > 0.7f) { restartToSelection(); return; }

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
        updateVisibleTiles();

        // --- modular toilet action ---
        if (settingsMenu.isToiletSystemEnabled() && controller.isXPressed()) {
            toiletHandler.handleToiletAction(player);
        }

        monsterSpawner.updateMonsters(happyFx);
        happyFx.update();

        camera.update();
    }

    private void updatePlayerMovement() {
        if (player.isFrozen()) return;

        double lx = controller.getLX();
        double ly = -controller.getLY();

        if (Math.abs(lx) < 0.15) lx = 0;
        if (Math.abs(ly) < 0.15) ly = 0;

        double len = Math.sqrt(lx * lx + ly * ly);
        if (len > 1) { lx /= len; ly /= len; }

        double speed = 4 * player.getSpeedMultiplier();
        double dx = lx * speed;
        double dy = ly * speed;

        Rectangle nextPos = player.getBounds(player.x + dx, player.y + dy);
        if (!maze.isColliding(nextPos)) { player.x += dx; player.y += dy; }
        else {
            Rectangle nextX = player.getBounds(player.x + dx, player.y);
            Rectangle nextY = player.getBounds(player.x, player.y + dy);
            if (!maze.isColliding(nextX)) player.x += dx;
            if (!maze.isColliding(nextY)) player.y += dy;
        }

        if (settingsMenu.areFootstepsEnabled()) player.checkFootstep();
    }

    private void restartToSelection() {
        selectingPlayer = true;
        maze = new MazeGenerator();
        player = new Player(2 * TILE + TILE / 2, 2 * TILE + TILE / 2);
        visibleTiles.clear();
        happyFx = new HappyBumpEffect();
        toilets = new ToiletManager();
        poopBar = new PoopBar();
        speedFx = new SpeedParticles();
        speedFXSystem = new SpeedFXSystem();
        toiletHandler = new ToiletActionHandler(poopBar, speedFx, speedFXSystem, toilets);

        ImageLoader loader = new ImageLoader();
        ImageLoader.MonsterImagePool monsterPool = loader.setupMonsterImages(TILE);
        monsterSpawner = new MonsterSpawner(this, monsterPool);

        camera = new Camera(this, WIDTH, HEIGHT, TILE);
    }

    private void updateVisibleTiles() {
        int baseTileX = (int)(camera.getX() / TILE);
        int baseTileY = (int)(camera.getY() / TILE);

        int startX = baseTileX - VIEW_PADDING;
        int startY = baseTileY - VIEW_PADDING;
        int endX = baseTileX + WIDTH + VIEW_PADDING;
        int endY = baseTileY + HEIGHT + VIEW_PADDING;

        Set<Point> newVisible = new HashSet<>();
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                Point p = new Point(x, y);
                newVisible.add(p);
                if (!visibleTiles.contains(p) && settingsMenu.isToiletSystemEnabled())
                    toilets.onTileGenerated(x, y, maze);
            }
        }

        visibleTiles = newVisible;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        camera.applyTransform(g2);

        int screenW = getWidth();
        int screenH = getHeight();

        if (selectingPlayer) {
            selectionManager.drawSelection(g2, screenW, screenH, TILE);
            return;
        }

        int baseTileX = (int)(camera.getX() / TILE);
        int baseTileY = (int)(camera.getY() / TILE);

        for (int wy = baseTileY - VIEW_PADDING; wy < baseTileY + HEIGHT + VIEW_PADDING; wy++) {
            for (int wx = baseTileX - VIEW_PADDING; wx < baseTileX + WIDTH + VIEW_PADDING; wx++) {
                int sx = wx * TILE - (int) camera.getX();
                int sy = wy * TILE - (int) camera.getY();
                g2.setColor(maze.isWallTile(wx, wy) ? Color.DARK_GRAY : Color.GRAY);
                g2.fillRect(sx, sy, TILE, TILE);
            }
        }

        if (settingsMenu.isToiletSystemEnabled()) toilets.draw(g2, camera.getX(), camera.getY());
        monsterSpawner.drawMonsters(g2, camera.getX(), camera.getY());
        happyFx.draw(g2, camera.getX(), camera.getY());

        if (settingsMenu.isSpeedVfxEnabled()) {
            speedFx.draw(g2, camera.getX(), camera.getY());
            speedFXSystem.draw(g2, camera.getX(), camera.getY(), playerImg, player.x, player.y);
        }

        double tilt = 0;
        if (settingsMenu.isSpeedVfxEnabled() && player.getSpeedMultiplier() > 1.0) tilt = controller.getLX() * 0.22;

        Graphics2D gPlayer = (Graphics2D) g2.create();
        gPlayer.translate(player.x - camera.getX(), player.y - camera.getY());
        gPlayer.rotate(tilt);
        gPlayer.drawImage(playerImg, -playerImg.getWidth()/2, -playerImg.getHeight()/2, null);
        gPlayer.dispose();

        if (settingsMenu.isToiletSystemEnabled())
            poopBar.draw(g2, screenW, toilets.isPlayerOnToilet(player));

        if (settingsMenu.isActive())
            settingsMenu.draw(g2, screenW, screenH);
    }

    public Player getPlayer() { return player; }
    public MazeGenerator getMaze() { return maze; }
    public Set<Point> getVisibleTiles() { return visibleTiles; }
    public ControllerInput getController() { return controller; }
    public SettingsMenu getSettingsMenu() { return settingsMenu; }

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