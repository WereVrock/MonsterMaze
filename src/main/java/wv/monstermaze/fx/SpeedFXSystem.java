// ===== SpeedFXSystem.java =====
package wv.monstermaze.fx;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class SpeedFXSystem {

    private static class AfterImage {

        double x;
        double y;
        int life;

        AfterImage(double x, double y) {
            this.x = x;
            this.y = y;
            this.life = 20;
        }
    }

    private static class SpeedLine {

        double x;
        double y;
        double vx;
        double vy;
        int life;

        SpeedLine(double x, double y, double vx, double vy) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = 20;
        }
    }

    private static class Dust {

        double x;
        double y;
        double vx;
        double vy;
        int life;

        Dust(double x, double y, double vx, double vy) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = 18;
        }
    }

    private static class Shockwave {

        double x;
        double y;
        double radius = 10;
        int life = 25;
    }

    private List<AfterImage> ghosts = new ArrayList<>();
    private List<SpeedLine> lines = new ArrayList<>();
    private List<Dust> dust = new ArrayList<>();
    private List<Shockwave> waves = new ArrayList<>();

    private Random rand = new Random();

    public void triggerBoost(double x, double y) {

        Shockwave s = new Shockwave();
        s.x = x;
        s.y = y;

        waves.add(s);

        for (int i = 0; i < 12; i++) {

            double angle = rand.nextDouble() * Math.PI * 2;
            double speed = 2 + rand.nextDouble() * 2;

            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;

            dust.add(new Dust(x, y, vx, vy));
        }
    }

    public void spawnSpeedEffects(double x, double y, double speedMult) {

        if (speedMult <= 1.0) {
            return;
        }

        ghosts.add(new AfterImage(x, y));

        double angle = rand.nextDouble() * Math.PI * 2;

        double vx = Math.cos(angle) * -6;
        double vy = Math.sin(angle) * -6;

        lines.add(new SpeedLine(x, y, vx, vy));

        if (rand.nextDouble() < 0.4) {

            double dvx = (rand.nextDouble() - 0.5) * 2;
            double dvy = (rand.nextDouble() - 0.5) * 2;

            dust.add(new Dust(x, y, dvx, dvy));
        }
    }

    public void update() {

        Iterator<AfterImage> gi = ghosts.iterator();

        while (gi.hasNext()) {

            AfterImage g = gi.next();
            g.life--;

            if (g.life <= 0) {
                gi.remove();
            }
        }

        Iterator<SpeedLine> li = lines.iterator();

        while (li.hasNext()) {

            SpeedLine l = li.next();

            l.x += l.vx;
            l.y += l.vy;

            l.life--;

            if (l.life <= 0) {
                li.remove();
            }
        }

        Iterator<Dust> di = dust.iterator();

        while (di.hasNext()) {

            Dust d = di.next();

            d.x += d.vx;
            d.y += d.vy;

            d.vx *= 0.9;
            d.vy *= 0.9;

            d.life--;

            if (d.life <= 0) {
                di.remove();
            }
        }

        Iterator<Shockwave> wi = waves.iterator();

        while (wi.hasNext()) {

            Shockwave s = wi.next();

            s.radius += 8;
            s.life--;

            if (s.life <= 0) {
                wi.remove();
            }
        }
    }

    public void draw(Graphics2D g, double camX, double camY, BufferedImage playerImg, double px, double py) {

        for (int i = 0; i < ghosts.size(); i++) {
            AfterImage a = ghosts.get(i);

            float alpha = a.life / 20f;

            Composite old = g.getComposite();

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.5f));

            int sx = (int) (a.x - camX - playerImg.getWidth() / 2);
            int sy = (int) (a.y - camY - playerImg.getHeight() / 2);

            g.drawImage(playerImg, sx, sy, null);

            g.setComposite(old);
        }

        g.setColor(Color.WHITE);

        for (SpeedLine l : lines) {

            int sx = (int) (l.x - camX);
            int sy = (int) (l.y - camY);

            int ex = (int) (sx + l.vx * 4);
            int ey = (int) (sy + l.vy * 4);

            g.drawLine(sx, sy, ex, ey);
        }

        g.setColor(new Color(200, 200, 200));

        for (int i = 0; i < dust.size(); i++) {
            Dust d = dust.get(i);

            int sx = (int) (d.x - camX);
            int sy = (int) (d.y - camY);

            g.fillOval(sx - 2, sy - 2, 4, 4);
        }

        g.setColor(Color.WHITE);

        for (Shockwave s : waves) {

            int sx = (int) (s.x - camX);
            int sy = (int) (s.y - camY);

            int r = (int) s.radius;

            g.drawOval(sx - r, sy - r, r * 2, r * 2);
        }
    }
}
