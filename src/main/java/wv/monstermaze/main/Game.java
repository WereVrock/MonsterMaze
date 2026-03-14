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

    public Game() {
        setPreferredSize(new Dimension(WIDTH * TILE, HEIGHT * TILE));
        maze = new MazeGenerator();
        player = new Player(0, 0);
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

    // Scale image while maintaining aspect ratio
    private BufferedImage scaleImage(BufferedImage original, int maxWidth, int maxHeight) {
        double ratio = Math.min((double) maxWidth / original.getWidth(), (double) maxHeight / original.getHeight());
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
        double dx = controller.getLX() * 4;
        double dy = controller.getLY() * 4;
        double newX = player.x + dx;
        double newY = player.y + dy;

        if (!maze.isWall(newX, newY)) {
            player.x = newX;
            player.y = newY;
        }

        maze.ensureArea(player.x, player.y);
        spawnMonster();

        if (monster != null) {
            if (player.distance(monster.x, monster.y) < 32) {
                monster = null;
            }
        }
    }

    private void spawnMonster() {
        if (monster != null) return;
        if (Math.random() > 0.1) return;

        Point p = maze.randomCorridorFarFrom(player.x, player.y, 8);
        if (p == null) return;

        BufferedImage img = monsterImages.get(new Random().nextInt(monsterImages.size()));
        monster = new Monster(p.x, p.y, img);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        int px = (int) player.x;
        int py = (int) player.y;

        int startX = px / TILE - WIDTH / 2;
        int startY = py / TILE - HEIGHT / 2;

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int wx = startX + x;
                int wy = startY + y;
                int sx = x * TILE;
                int sy = y * TILE;

                if (maze.isWallTile(wx, wy)) {
                    g2.setColor(Color.DARK_GRAY);
                } else {
                    g2.setColor(Color.GRAY);
                }
                g2.fillRect(sx, sy, TILE, TILE);
            }
        }

        if (monster != null) {
            int sx = (int) (monster.x - px + WIDTH * TILE / 2 - monster.img.getWidth() / 2);
            int sy = (int) (monster.y - py + HEIGHT * TILE / 2 - monster.img.getHeight() / 2);
            g2.drawImage(monster.img, sx, sy, null);
        }

        g2.drawImage(playerImg, WIDTH * TILE / 2 - playerImg.getWidth() / 2, HEIGHT * TILE / 2 - playerImg.getHeight() / 2, null);
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Labyrinth");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new Game());
        f.pack();
        f.setVisible(true);
    }
}