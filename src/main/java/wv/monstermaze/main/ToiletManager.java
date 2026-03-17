package wv.monstermaze.main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class ToiletManager {

    private final Set<Point> toilets = new HashSet<>();
    private final Map<Point, BufferedImage> toiletImages = new HashMap<>();
    private final Set<Point> usedChunks = new HashSet<>();
    private final Random rand = new Random();
    private final List<BufferedImage> images;

    // --- Configurable constants ---
    private static final int MIN_DISTANCE_BETWEEN_TOILETS = 7; // tiles
    private static final int MAX_TOILETS_PER_CHUNK = 1;
    private static final int MIN_TOILETS_PER_CHUNK = 1;
    private static final int CHUNK_WIDTH = Game.WIDTH-2;  // tiles per screen width
    private static final int CHUNK_HEIGHT = Game.HEIGHT-2; // tiles per screen height
    private static final int MAX_ATTEMPTS_PER_TOILET = 50; // tries to find free spot in chunk
    private static final double PLAYER_TOILET_RADIUS = 0.75; // fraction of TILE

    public ToiletManager() {
        ImageLoader loader = new ImageLoader();
        List<ImageLoader.LoadedImage> loaded = loader.loadImages("toilets", Game.TILE);

        images = new ArrayList<>();
        for (ImageLoader.LoadedImage li : loaded) {
            images.add(li.image);
        }
    }

    public void onTileGenerated(int x, int y, MazeGenerator maze) {
        if (maze.isWallTile(x, y)) return;

        int chunkX = Math.floorDiv(x, CHUNK_WIDTH);
        int chunkY = Math.floorDiv(y, CHUNK_HEIGHT);
        Point chunk = new Point(chunkX, chunkY);

        // Already spawned toilets in this chunk? Skip
        if (usedChunks.contains(chunk)) return;

        // Decide number of toilets in this chunk
        int toiletsInChunk = MIN_TOILETS_PER_CHUNK + rand.nextInt(MAX_TOILETS_PER_CHUNK - MIN_TOILETS_PER_CHUNK + 1);

        for (int i = 0; i < toiletsInChunk; i++) {
            Point spawn = findValidSpotInChunk(chunkX, chunkY, maze);
            if (spawn != null) {
                toilets.add(spawn);
                if (!images.isEmpty()) {
                    BufferedImage img = images.get(rand.nextInt(images.size()));
                    toiletImages.put(spawn, img);
                }
            }
        }

        usedChunks.add(chunk);
    }

    private Point findValidSpotInChunk(int chunkX, int chunkY, MazeGenerator maze) {
        int startX = chunkX * CHUNK_WIDTH;
        int startY = chunkY * CHUNK_HEIGHT;

        for (int attempt = 0; attempt < MAX_ATTEMPTS_PER_TOILET; attempt++) {
            int rx = startX + rand.nextInt(CHUNK_WIDTH);
            int ry = startY + rand.nextInt(CHUNK_HEIGHT);
            Point candidate = new Point(rx, ry);

            if (maze.isWallTile(rx, ry)) continue;
            if (isTooCloseToOther(candidate)) continue;

            return candidate;
        }

        return null;
    }

    private boolean isTooCloseToOther(Point p) {
        for (Point t : toilets) {
            int dx = Math.abs(t.x - p.x);
            int dy = Math.abs(t.y - p.y);
            if (dx + dy < MIN_DISTANCE_BETWEEN_TOILETS) return true;
        }
        return false;
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
            if (Math.sqrt(dx * dx + dy * dy) < Game.TILE * PLAYER_TOILET_RADIUS) return true;
        }
        return false;
    }

    public void draw(Graphics2D g2, double camX, double camY) {
        for (Point t : toilets) {
            int sx = t.x * Game.TILE - (int) camX;
            int sy = t.y * Game.TILE - (int) camY;

            BufferedImage img = toiletImages.get(t);
            if (img != null) {
                g2.drawImage(
                        img,
                        sx + Game.TILE / 2 - img.getWidth() / 2,
                        sy + Game.TILE / 2 - img.getHeight() / 2,
                        null
                );
            } else {
                g2.setColor(Color.WHITE);
                g2.fillRect(sx + 20, sy + 20, Game.TILE - 40, Game.TILE - 40);
            }
        }
    }
}