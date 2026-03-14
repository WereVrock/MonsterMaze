package wv.monstermaze.main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class PlayerSelectionManager {

    private List<BufferedImage> playerImages;
    private int selectionIndex = 0;
    private long lastInputTime = 0;

    public PlayerSelectionManager(List<BufferedImage> playerImages) {
        this.playerImages = playerImages;
    }

    public boolean updateSelection(double lx, double ly, long lastInputTime) {
        long now = System.currentTimeMillis();
        if (now - this.lastInputTime < 200) return false;

        if (lx > 0.5) selectionIndex++;
        if (lx < -0.5) selectionIndex--;

        if (selectionIndex < 0) selectionIndex = playerImages.size() - 1;
        if (selectionIndex >= playerImages.size()) selectionIndex = 0;

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
        int startX = screenWidth / 2 - (playerImages.size() * spacing) / 2;

        for (int i = 0; i < playerImages.size(); i++) {
            BufferedImage img = playerImages.get(i);
            int x = startX + i * spacing;
            int y = screenHeight / 2;

            if (i == selectionIndex) {
                g2.setColor(Color.YELLOW);
                g2.drawRect(x - 10, y - 10, tileSize + 20, tileSize + 20);
            }

            g2.drawImage(img, x, y, null);
        }
    }
}