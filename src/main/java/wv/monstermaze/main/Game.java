package wv.monstermaze.main;

import wv.monstermaze.toilet.ToiletManager;
import wv.monstermaze.toilet.PoopBar;
import wv.monstermaze.toilet.ToiletActionHandler;
import wv.monstermaze.fx.HappyBumpEffect;
import wv.monstermaze.fx.SpeedFXSystem;
import wv.monstermaze.fx.SpeedParticles;
import wv.monstermaze.images.MonsterImagePool;
import wv.monstermaze.images.ImageLoader;

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

    private MonsterImagePool monsterPool;
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
    private SpeedParticles speedParticles = new SpeedParticles();
    private SpeedFXSystem speedFXSystem = new SpeedFXSystem();
    private ToiletActionHandler toiletHandler;

    private GameContext context;

    Camera camera;

    public Game() {
        setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());

        maze = new MazeGenerator();
        player = new Player(2 * TILE + TILE / 2, 2 * TILE + TILE / 2);

        controller = new ControllerInput();
        happyFx = new HappyBumpEffect();

        ImageLoader loader = new ImageLoader();

        List<ImageLoader.LoadedImage> loadedPlayers = loader.loadImages("player", TILE);
        playerImages = new ArrayList<>();
        for (ImageLoader.LoadedImage li : loadedPlayers) playerImages.add(li.image);

        if (!playerImages.isEmpty()) playerImg = playerImages.get(0);

        monsterPool = loader.setupMonsterImages(TILE);
        monsterSpawner = new MonsterSpawner(this, monsterPool);

        selectionManager = new PlayerSelectionManager(playerImages, monsterPool);

        settingsMenu = new SettingsMenu();

        camera = new Camera(this, WIDTH, HEIGHT, TILE);

        toiletHandler = new ToiletActionHandler(poopBar, speedParticles, speedFXSystem, toilets);

        setupContext();

        new Thread(this).start();
    }

    private void setupContext() {
        context = new GameContext();
        context.controller = controller;
        context.maze = maze;
        context.settings = settingsMenu;

        player.setContext(context);
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
            speedParticles.update();
            speedFXSystem.update();
        }

        if (player.getSpeedMultiplier() > 1.0 && settingsMenu.isSpeedVfxEnabled()) {
            boolean high = player.getSpeedMultiplier() > 1.5;
            speedParticles.spawnBoostTrail(player.x, player.y, high);
            speedFXSystem.spawnSpeedEffects(player.x, player.y, player.getSpeedMultiplier());
        }

        if (settingsMenu.isToiletSystemEnabled()) poopBar.update();

        if (controller.getStartButton() > 0.7f) settingsMenu.toggleActive();
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
                playerImg = selectionManager.getSelectedImage();
                selectingPlayer = false;
            } else {
                lastInputTime = System.currentTimeMillis();
            }
            return;
        }

        maze.ensureArea(player.x, player.y);
        updateVisibleTiles();

        if (settingsMenu.isToiletSystemEnabled() && controller.isXPressed()) {
            toiletHandler.handleToiletAction(player);
        }

        monsterSpawner.updateMonsters(happyFx);
        happyFx.update();

        camera.update();
    }

    private void restartToSelection() {
        selectingPlayer = true;

        maze = new MazeGenerator();
        player = new Player(2 * TILE + TILE / 2, 2 * TILE + TILE / 2);
        visibleTiles.clear();
        happyFx = new HappyBumpEffect();

        toilets = new ToiletManager();
        poopBar = new PoopBar();
        speedParticles = new SpeedParticles();
        speedFXSystem = new SpeedFXSystem();
        toiletHandler = new ToiletActionHandler(poopBar, speedParticles, speedFXSystem, toilets);

        ImageLoader loader = new ImageLoader();
        monsterPool = loader.setupMonsterImages(TILE);
        monsterSpawner = new MonsterSpawner(this, monsterPool);

        selectionManager = new PlayerSelectionManager(playerImages, monsterPool);
        selectionManager.refreshRandomSlot();

        camera = new Camera(this, WIDTH, HEIGHT, TILE);

        setupContext();
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

        int camX = (int)Math.round(camera.getX());
        int camY = (int)Math.round(camera.getY());

        int screenW = getWidth();
        int screenH = getHeight();

        if (selectingPlayer) {
            selectionManager.drawSelection(g2, screenW, screenH, TILE);
            return;
        }

        int baseTileX = camX / TILE;
        int baseTileY = camY / TILE;

        for (int wy = baseTileY - VIEW_PADDING; wy < baseTileY + HEIGHT + VIEW_PADDING; wy++) {
            for (int wx = baseTileX - VIEW_PADDING; wx < baseTileX + WIDTH + VIEW_PADDING; wx++) {

                int sx = wx * TILE - camX;
                int sy = wy * TILE - camY;

                g2.setColor(maze.isWallTile(wx, wy) ? Color.DARK_GRAY : Color.GRAY);
                g2.fillRect(sx, sy, TILE + 1, TILE + 1);
            }
        }

        if (settingsMenu.isToiletSystemEnabled()) toilets.draw(g2, camX, camY);
        monsterSpawner.drawMonsters(g2, camX, camY);
        happyFx.draw(g2, camX, camY);

        if (settingsMenu.isSpeedVfxEnabled()) {
            speedFXSystem.draw(g2, camX, camY, playerImg, player.x, player.y);
            speedParticles.draw(g2, camX, camY);
        }

        double tilt = 0;
        if (settingsMenu.isSpeedVfxEnabled() && player.getSpeedMultiplier() > 1.0)
            tilt = controller.getLX() * 0.22;

        Graphics2D gPlayer = (Graphics2D) g2.create();
        gPlayer.translate(player.x - camX, player.y - camY);
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