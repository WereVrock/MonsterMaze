package wv.monstermaze.main;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.*;

public class Game extends JPanel implements Runnable {

    public static final int TILE = 96;

    public static final int WIDTH =
            Toolkit.getDefaultToolkit().getScreenSize().width / TILE;

    public static final int HEIGHT =
            Toolkit.getDefaultToolkit().getScreenSize().height / TILE;

    private Player player;
    private Monster monster;
    private MazeGenerator maze;

    private BufferedImage playerImg;
    private java.util.List<BufferedImage> playerImages = new ArrayList<>();

    private java.util.List<BufferedImage> monsterImages = new ArrayList<>();

    private int playerSelectionIndex = 0;

    private boolean selectingPlayer = true;

    private ControllerInput controller;

    private Set<Point> visibleTiles = new HashSet<>();

    private HappyBumpEffect happyFx = new HappyBumpEffect();

    private long lastInputTime = 0;

    public Game() {

        setPreferredSize(
                Toolkit.getDefaultToolkit().getScreenSize()
        );

        maze = new MazeGenerator();

        player = new Player(2 * TILE + TILE / 2, 2 * TILE + TILE / 2);

        loadImages();

        controller = new ControllerInput();

        new Thread(this).start();
    }

    private void loadImages() {

        try {

            loadPlayerImages();

            loadMonsterImages();

            if (!playerImages.isEmpty()) {
                playerImg = playerImages.get(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPlayerImages() {

        try {

            File folder = new File("player");

            if (!folder.exists()) {
                System.out.println("Player folder not found: " + folder.getAbsolutePath());
                return;
            }

            File[] files = folder.listFiles();

            if (files == null || files.length == 0) {
                System.out.println("Player folder empty.");
                return;
            }

            for (File f : files) {

                try {

                    BufferedImage img = ImageIO.read(f);

                    if (img != null) {

                        playerImages.add(scaleImage(img, TILE, TILE));

                        System.out.println("Loaded player image: " + f.getName());
                    }

                } catch (Exception e) {
                    System.out.println("Failed loading: " + f.getName());
                }
            }

            System.out.println("Total player images loaded: " + playerImages.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMonsterImages() {

        try {

            File folder = new File("monsters");

            if (!folder.exists()) {
                System.out.println("Monster folder not found: " + folder.getAbsolutePath());
                return;
            }

            File[] files = folder.listFiles();

            if (files == null || files.length == 0) {
                System.out.println("Monster folder empty.");
                return;
            }

            for (File f : files) {

                try {

                    BufferedImage img = ImageIO.read(f);

                    if (img != null) {

                        monsterImages.add(scaleImage(img, TILE, TILE));

                        System.out.println("Loaded monster image: " + f.getName());
                    }

                } catch (Exception e) {
                    System.out.println("Failed loading: " + f.getName());
                }
            }

            System.out.println("Total monster images loaded: " + monsterImages.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BufferedImage scaleImage(BufferedImage original, int maxWidth, int maxHeight) {

        double ratio = Math.min(
                (double) maxWidth / original.getWidth(),
                (double) maxHeight / original.getHeight());

        int newWidth = (int) (original.getWidth() * ratio);
        int newHeight = (int) (original.getHeight() * ratio);

        BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = scaled.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2.drawImage(original, 0, 0, newWidth, newHeight, null);

        g2.dispose();

        return scaled;
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

        if (controller.getRightTrigger() > 0.7f) {
            restartToSelection();
            return;
        }

        if (selectingPlayer) {
            updatePlayerSelection();
            return;
        }

        double lx = controller.getLX();
        double ly = controller.getLY();

        if (Math.abs(lx) < 0.15) lx = 0;
        if (Math.abs(ly) < 0.15) ly = 0;

        ly = -ly;

        double len = Math.sqrt(lx * lx + ly * ly);
        if (len > 1) {
            lx /= len;
            ly /= len;
        }

        double speed = 4;

        double dx = lx * speed;
        double dy = ly * speed;

        double newX = player.x + dx;
        double newY = player.y + dy;
        Rectangle nextPos = player.getBounds(newX, newY);

        if (!maze.isColliding(nextPos)) {
            player.x = newX;
            player.y = newY;
        } else {

            Rectangle nextX = player.getBounds(player.x + dx, player.y);
            if (!maze.isColliding(nextX)) {
                player.x += dx;
            }

            Rectangle nextY = player.getBounds(player.x, player.y + dy);
            if (!maze.isColliding(nextY)) {
                player.y += dy;
            }
        }

        maze.ensureArea(player.x, player.y);

        checkVisibleTiles();

        updateMonster();

        happyFx.update();

        if (monster != null) {
            if (player.distance(monster.x, monster.y) < 32) {

                happyFx.trigger(monster.x, monster.y);

                monster = null;
            }
        }
    }

    private void restartToSelection() {

        selectingPlayer = true;

        maze = new MazeGenerator();

        player = new Player(2 * TILE + TILE / 2, 2 * TILE + TILE / 2);

        monster = null;

        visibleTiles.clear();

        happyFx = new HappyBumpEffect();

        System.out.println("Restarting to player selection");
    }

    private void updatePlayerSelection() {

        double lx = controller.getLX();

        long now = System.currentTimeMillis();

        if (now - lastInputTime < 200) return;

        if (lx > 0.5) {
            playerSelectionIndex++;
            lastInputTime = now;
        }

        if (lx < -0.5) {
            playerSelectionIndex--;
            lastInputTime = now;
        }

        if (playerSelectionIndex < 0) playerSelectionIndex = playerImages.size() - 1;
        if (playerSelectionIndex >= playerImages.size()) playerSelectionIndex = 0;

        double ly = controller.getLY();

        if (Math.abs(ly) > 0.8) {

            playerImg = playerImages.get(playerSelectionIndex);

            selectingPlayer = false;

            System.out.println("Player selected: " + playerSelectionIndex);
        }
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

        BufferedImage img = monsterImages.get(new Random().nextInt(monsterImages.size()));

        double mx = tile.x * TILE + TILE / 2;
        double my = tile.y * TILE + TILE / 2;

        monster = new Monster(mx, my, img);
    }

    private void updateMonster() {

        if (monster == null) return;

        double maxDistance = TILE * 25;

        if (player.distance(monster.x, monster.y) > maxDistance) {
            monster = null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        g2.drawString("RT = Change Character", 20, 30);

        if (selectingPlayer) {

            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 40));

            g2.drawString("Choose Your Character", getWidth() / 2 - 220, 120);

            int spacing = TILE * 2;

            int startX = getWidth() / 2 - (playerImages.size() * spacing) / 2;

            for (int i = 0; i < playerImages.size(); i++) {

                BufferedImage img = playerImages.get(i);

                int x = startX + i * spacing;
                int y = getHeight() / 2;

                if (i == playerSelectionIndex) {

                    g2.setColor(Color.YELLOW);
                    g2.drawRect(x - 10, y - 10, TILE + 20, TILE + 20);
                }

                g2.drawImage(img, x, y, null);
            }

            return;
        }

        int screenCenterX = WIDTH * TILE / 2;
        int screenCenterY = HEIGHT * TILE / 2;

        double cameraX = player.x - screenCenterX;
        double cameraY = player.y - screenCenterY;

        int startX = (int) (cameraX / TILE) - 1;
        int startY = (int) (cameraY / TILE) - 1;

        int endX = startX + WIDTH + 2;
        int endY = startY + HEIGHT + 2;

        for (int wy = startY; wy < endY; wy++) {

            for (int wx = startX; wx < endX; wx++) {

                int worldX = wx * TILE;
                int worldY = wy * TILE;

                int sx = (int) (worldX - cameraX);
                int sy = (int) (worldY - cameraY);

                if (maze.isWallTile(wx, wy)) {
                    g2.setColor(Color.DARK_GRAY);
                } else {
                    g2.setColor(Color.GRAY);
                }

                g2.fillRect(sx, sy, TILE, TILE);
            }
        }

        if (monster != null) {

            int sx = (int) (monster.x - cameraX - monster.img.getWidth() / 2);
            int sy = (int) (monster.y - cameraY - monster.img.getHeight() / 2);

            g2.drawImage(monster.img, sx, sy, null);
        }

        happyFx.draw(g2, cameraX, cameraY);

        int playerScreenX = (int) (player.x - cameraX - playerImg.getWidth() / 2);
        int playerScreenY = (int) (player.y - cameraY - playerImg.getHeight() / 2);

        g2.drawImage(playerImg, playerScreenX, playerScreenY, null);
    }

    public static void main(String[] args) {

        JFrame f = new JFrame("Labyrinth");

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        f.setUndecorated(true);

        GraphicsDevice device =
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice();

        Game game = new Game();

        f.add(game);

        device.setFullScreenWindow(f);

        f.validate();
    }
}