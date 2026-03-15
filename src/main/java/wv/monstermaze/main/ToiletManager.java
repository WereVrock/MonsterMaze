package wv.monstermaze.main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

public class ToiletManager {

    private Set<Point> toilets = new HashSet<>();
    private Map<Point, BufferedImage> toiletImages = new HashMap<>();

    private Random rand = new Random();

    private java.util.List<BufferedImage> images;

    public ToiletManager() {

        ImageLoader loader = new ImageLoader();
        images = loader.loadImages("toilets", Game.TILE);
    }

    public void onTileGenerated(int x, int y, MazeGenerator maze) {

        if (maze.isWallTile(x, y)) return;

        if (rand.nextDouble() < 0.02) {

            Point p = new Point(x, y);

            toilets.add(p);

            if (!images.isEmpty()) {

                BufferedImage img = images.get(rand.nextInt(images.size()));
                toiletImages.put(p, img);
            }
        }
    }

    public boolean isToilet(int x, int y) {
        return toilets.contains(new Point(x, y));
    }

    public boolean isPlayerOnToilet(Player p) {

        for (Point t : toilets) {

            double cx = t.x * Game.TILE + Game.TILE / 2.0;
            double cy = t.y * Game.TILE + Game.TILE / 2.0;

            double dx = p.x - cx;
            double dy = p.y - cy;

            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist < Game.TILE * 0.75) {
                return true;
            }
        }

        return false;
    }

    public void draw(Graphics2D g2, double camX, double camY) {

        for (Point t : toilets) {

            int sx = t.x * Game.TILE - (int)camX;
            int sy = t.y * Game.TILE - (int)camY;

            BufferedImage img = toiletImages.get(t);

            if (img != null) {

                g2.drawImage(
                        img,
                        sx + Game.TILE/2 - img.getWidth()/2,
                        sy + Game.TILE/2 - img.getHeight()/2,
                        null
                );

            } else {

                g2.setColor(Color.WHITE);
                g2.fillRect(sx + 20, sy + 20, Game.TILE - 40, Game.TILE - 40);
            }
        }
    }
}