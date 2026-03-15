package wv.monstermaze.main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class PoopBar {


private double value = 0;
private long lastTime = System.currentTimeMillis();
private boolean flash;

private BufferedImage poopImg;
private BufferedImage toiletImg;

public PoopBar() {

    try {

        poopImg = ImageIO.read(new File("ui/poop.png"));
        toiletImg = ImageIO.read(new File("ui/toilet.png"));

        poopImg = scale(poopImg, 32, 32);
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

    value += dt * 0.02;

    if (value > 1) value = 1;

    flash = !flash;
}

public boolean isGreen() {
    return value >= 0.30 && value <= 0.85;
}

public boolean isRed() {
    return value > 0.85;
}

public boolean isGray() {
    return value < 0.30;
}

public void reset() {
    value = 0;
}

public void draw(Graphics2D g2, int screenW) {

    int w = 400;
    int h = 30;

    int x = screenW / 2 - w / 2;
    int y = 40;

    int fill = (int)(w * value);

    g2.setColor(Color.GRAY);
    g2.fillRect(x, y, (int)(w * 0.30), h);

    g2.setColor(Color.GREEN);
    g2.fillRect(x + (int)(w * 0.30), y, (int)(w * 0.55), h);

    g2.setColor(Color.RED);
    g2.fillRect(x + (int)(w * 0.85), y, (int)(w * 0.15), h);

    g2.setColor(Color.WHITE);
    g2.drawRect(x, y, w, h);

    g2.setColor(new Color(255, 220, 0));
    g2.fillRect(x, y, fill, h);

    if (poopImg != null) {
        g2.drawImage(poopImg, x - 40, y - 2, null);
    }

    if (toiletImg != null) {

        if (isGreen()) {
            g2.drawImage(toiletImg, x + w + 10, y - 2, null);
        }

        if (isRed() && flash) {
            g2.drawImage(toiletImg, x + w + 10, y - 2, null);
        }
    }
}


}
