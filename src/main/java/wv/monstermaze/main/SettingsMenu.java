package wv.monstermaze.main;

import java.awt.*;

public class SettingsMenu {

    private boolean active = false;
    private boolean footstepsEnabled = true;
    private int selection = 0; // 0 = footsteps toggle, 1 = exit game
    private long lastInputTime = 0;

    public boolean isActive() {
        return active;
    }

    public void toggleActive() {
        active = !active;
        lastInputTime = System.currentTimeMillis();
    }

    public boolean areFootstepsEnabled() {
        return footstepsEnabled;
    }

    public void update(double lx, double ly, boolean confirmPressed) {
        if (!active) return;

        long now = System.currentTimeMillis();
        if (now - lastInputTime < 200) return;

        if (ly > 0.5) {
            selection++;
            if (selection > 1) selection = 0;
            lastInputTime = now;
        } else if (ly < -0.5) {
            selection--;
            if (selection < 0) selection = 1;
            lastInputTime = now;
        }

        if (confirmPressed) {
            if (selection == 0) {
                footstepsEnabled = !footstepsEnabled;
            } else if (selection == 1) {
                System.exit(0);
            }
            lastInputTime = now;
        }
    }

    public void draw(Graphics2D g2, int screenWidth, int screenHeight) {
        if (!active) return;

        int w = 400;
        int h = 200;
        int x = screenWidth / 2 - w / 2;
        int y = screenHeight / 2 - h / 2;

        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(x, y, w, h);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.drawString("Settings", x + 140, y + 40);

        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        g2.setColor(selection == 0 ? Color.YELLOW : Color.WHITE);
        g2.drawString("Footsteps: " + (footstepsEnabled ? "ON" : "OFF"), x + 50, y + 90);

        g2.setColor(selection == 1 ? Color.YELLOW : Color.WHITE);
        g2.drawString("Exit Game", x + 50, y + 140);

        g2.setFont(new Font("Arial", Font.ITALIC, 14));
        g2.drawString("Use LT to open this menu", x + 50, y + 180);
    }
}