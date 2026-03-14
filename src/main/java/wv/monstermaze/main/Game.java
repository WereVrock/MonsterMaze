package wv.monstermaze.main;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.*;

public class Game extends JPanel implements Runnable {

    public static final int TILE = 64;
    public static final int WIDTH = 21;
    public static final int HEIGHT = 15;

    private Player player;
    private Monster monster;
    private MazeGenerator maze;

    private BufferedImage playerImg;
    private java.util.List<BufferedImage> monsterImages = new ArrayList<>();

    private ControllerInput controller;

    private Set<Point> visibleTiles = new HashSet<>();

    public Game() {

        setPreferredSize(new Dimension(WIDTH * TILE, HEIGHT * TILE));

        maze = new MazeGenerator();

        player = new Player(2 * TILE + TILE / 2, 2 * TILE + TILE / 2);

        loadImages();

        controller = new ControllerInput();

        new Thread(this).start();
    }

    private void loadImages() {

        try {

            playerImg = scaleImage(ImageIO.read(new File("player.png")), TILE, TILE);

            File folder = new File("monsters");

            for (File f : folder.listFiles()) {

                BufferedImage img = ImageIO.read(f);

                monsterImages.add(scaleImage(img, TILE, TILE));
            }

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

        if (monster != null) {
            if (player.distance(monster.x, monster.y) < 32) {
                monster = null;
            }
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

        int playerScreenX = (int) (player.x - cameraX - playerImg.getWidth() / 2);
        int playerScreenY = (int) (player.y - cameraY - playerImg.getHeight() / 2);

        g2.drawImage(playerImg, playerScreenX, playerScreenY, null);
    }

    public static void main(String[] args) {

        JFrame f = new JFrame("Labyrinth");

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        f.add(new Game());

        f.pack();

        f.setVisible(true);
    }
}