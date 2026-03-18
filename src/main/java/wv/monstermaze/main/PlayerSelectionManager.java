package wv.monstermaze.main;

import wv.monstermaze.images.MonsterImagePool;
import wv.monstermaze.images.ImageLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayerSelectionManager {

    private List<BufferedImage> basePlayerImages;
    private List<BufferedImage> displayImages;

    private final MonsterImagePool monsterPool;
    private final Random random = new Random();

    private int selectionIndex = 0;
    private long lastInputTime = 0;

    public PlayerSelectionManager(List<BufferedImage> playerImages, MonsterImagePool monsterPool) {
        this.basePlayerImages = playerImages;
        this.monsterPool = monsterPool;
        this.displayImages = new ArrayList<>();
        refreshRandomSlot();
    }

    public void refreshRandomSlot() {
        displayImages.clear();
        displayImages.addAll(basePlayerImages);

        ImageLoader.LoadedImage li = monsterPool.getTrueRandom(random);
        if (li != null) {
            displayImages.add(li.image); // extra random slot
        }
    }

    public boolean isRandomSlotSelected() {
        return selectionIndex == displayImages.size() - 1;
    }

    public BufferedImage getSelectedImage() {
        return displayImages.get(selectionIndex);
    }

    public boolean updateSelection(double lx, double ly, long lastInputTime) {
        long now = System.currentTimeMillis();
        if (now - this.lastInputTime < 200) return false;

        if (lx > 0.5) selectionIndex++;
        if (lx < -0.5) selectionIndex--;

        if (selectionIndex < 0) selectionIndex = displayImages.size() - 1;
        if (selectionIndex >= displayImages.size()) selectionIndex = 0;

        if (Math.abs(ly) > 0.8) {
            this.lastInputTime = now;
            return true;
        }

        this.lastInputTime = now;
        return false;
    }

    public int getSelectionIndex() {
        return selectionIndex;
    }

    public void drawSelection(Graphics2D g2, int screenWidth, int screenHeight, int tileSize) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        g2.drawString("Choose Your Character", screenWidth / 2 - 220, 120);

        int spacing = tileSize * 2;
        int startX = screenWidth / 2 - (displayImages.size() * spacing) / 2;

        for (int i = 0; i < displayImages.size(); i++) {
            BufferedImage img = displayImages.get(i);
            int x = startX + i * spacing;
            int y = screenHeight / 2;

            if (i == selectionIndex) {
                g2.setColor(Color.YELLOW);
                g2.drawRect(x - 10, y - 10, tileSize + 20, tileSize + 20);
            }

            g2.drawImage(img, x, y, null);

            // mark random slot visually
            if (i == displayImages.size() - 1) {
                g2.setColor(Color.CYAN);
                g2.setFont(new Font("Arial", Font.BOLD, 20));
                g2.drawString("?", x + tileSize / 2 - 5, y - 15);
            }
        }
    }
}