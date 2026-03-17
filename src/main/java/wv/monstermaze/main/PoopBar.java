package wv.monstermaze.main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class PoopBar {

    private double value = 0;
    private long lastTime = System.currentTimeMillis();

    private boolean flash;
    private long lastFlashTime = 0;
    private final long FLASH_INTERVAL = 400;

    private boolean greenTriggered = false;

    private BufferedImage poopImg;
    private BufferedImage toiletImg;

    private double wiggleTime = 0;

    // --- NEW: delay before bar starts refilling ---
    private double refillDelay=0,refillDelayDuration = 10;

    public PoopBar() {
        try {
            poopImg = ImageIO.read(new File("ui/poop.png"));
            toiletImg = ImageIO.read(new File("ui/toilet.png"));

            poopImg = scale(poopImg, 28, 28);
            toiletImg = scale(toiletImg, 32, 32);

        } catch (Exception e) {
            System.out.println("UI icons missing in /ui folder");
        }
    }

    private BufferedImage scale(BufferedImage img, int w, int h) {
        BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, w, h, null);
        g.dispose();
        return scaled;
    }

    public void update() {
        long now = System.currentTimeMillis();
        double dt = (now - lastTime) / 1000.0;
        lastTime = now;

        // Only refill if delay has expired
        if (refillDelay > 0) {
            refillDelay -= dt;
            if (refillDelay < 0) refillDelay = 0;
        } else {
            value += dt * 0.02;
            if (value > 1) value = 1;
        }

        wiggleTime += dt * 6;

        if (now - lastFlashTime > FLASH_INTERVAL) {
            flash = !flash;
            lastFlashTime = now;

            if (isRed() && flash) {
                ToiletAlertSound.play();
            }
        }

        if (isGreen() && !greenTriggered) {
            greenTriggered = true;
            ToiletAlertSound.play();
        }

        if (isGray()) {
            greenTriggered = false;
        }
    }

    public boolean isGreen() {
        return value >= 0.30 && value <= 0.80;
    }

    public boolean isRed() {
        return value > 0.80;
    }

    public boolean isGray() {
        return value < 0.30;
    }

    public void reset() {
        value = 0;
        refillDelay=refillDelayDuration;
        greenTriggered = false;
    }

   

    public void draw(Graphics2D g2, int screenW, boolean playerOnToilet) {
        int w = 400;
        int h = 34;

        int x = screenW / 2 - w / 2;
        int y = 40;

        int grayEnd = (int)(w * 0.30);
        int greenEnd = (int)(w * 0.80);

        int fillWidth = (int)(w * value);

        Color darkGray = new Color(40,40,40);
        Color lightGray = new Color(120,120,120);
        Color softGreen = new Color(70,140,90);
        Color softRed = new Color(170,70,70);

        g2.setColor(darkGray);
        g2.fillRect(x, y, grayEnd, h);

        g2.setColor(softGreen);
        g2.fillRect(x + grayEnd, y, greenEnd - grayEnd, h);

        g2.setColor(softRed);
        g2.fillRect(x + greenEnd, y, w - greenEnd, h);

        if (fillWidth > 0) {
            if (fillWidth <= grayEnd) {
                g2.setColor(lightGray);
                g2.fillRect(x, y, fillWidth, h);
            } else {
                g2.setColor(lightGray);
                g2.fillRect(x, y, grayEnd, h);
            }
        }

        g2.setColor(new Color(220,220,220));
        g2.drawRect(x, y, w, h);

        if (poopImg != null && value >= 0.30) {
            int spacing = poopImg.getWidth() + 12;
            for (int px = x + grayEnd; px < x + fillWidth; px += spacing) {
                double offset = Math.sin(wiggleTime + px * 0.05) * 3;
                g2.drawImage(
                        poopImg,
                        px,
                        (int)(y + h/2 - poopImg.getHeight()/2 + offset),
                        null
                );
            }
        }

        if (toiletImg != null) {
            if (isGreen()) g2.drawImage(toiletImg, x + w + 10, y, null);
            if (isRed() && flash) g2.drawImage(toiletImg, x + w + 10, y, null);
        }

        if (playerOnToilet && (isGreen() || isRed())) {
            int bx = screenW / 2 - 30;
            int by = y + 60;
            g2.setColor(new Color(40,120,255));
            g2.fillRoundRect(bx, by, 60, 60, 16, 16);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(5));
            g2.drawLine(bx + 16, by + 16, bx + 44, by + 44);
            g2.drawLine(bx + 44, by + 16, bx + 16, by + 44);
        }
    }
}