package wv.monstermaze.main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class SpeedFXSystem {

    private static class AfterImage {

        double x;
        double y;
        int life;

        AfterImage(double x,double y){
            this.x=x;
            this.y=y;
            this.life=20;
        }
    }

    private static class SpeedLine {

        double x;
        double y;
        double vx;
        double vy;
        int life;

        SpeedLine(double x,double y,double vx,double vy){
            this.x=x;
            this.y=y;
            this.vx=vx;
            this.vy=vy;
            this.life=20;
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
    private List<Shockwave> waves = new ArrayList<>();

    private Random rand = new Random();

    public void triggerBoost(double x,double y){

        Shockwave s = new Shockwave();
        s.x = x;
        s.y = y;

        waves.add(s);
    }

    public void spawnSpeedEffects(double x,double y,double speedMult){

        if(speedMult <= 1.0) return;

        ghosts.add(new AfterImage(x,y));

        double angle = rand.nextDouble()*Math.PI*2;

        double vx = Math.cos(angle) * -6;
        double vy = Math.sin(angle) * -6;

        lines.add(new SpeedLine(x,y,vx,vy));
    }

    public void update(){

        Iterator<AfterImage> gi = ghosts.iterator();

        while(gi.hasNext()){

            AfterImage g = gi.next();
            g.life--;

            if(g.life <= 0){
                gi.remove();
            }
        }

        Iterator<SpeedLine> li = lines.iterator();

        while(li.hasNext()){

            SpeedLine l = li.next();

            l.x += l.vx;
            l.y += l.vy;

            l.life--;

            if(l.life <= 0){
                li.remove();
            }
        }

        Iterator<Shockwave> wi = waves.iterator();

        while(wi.hasNext()){

            Shockwave s = wi.next();

            s.radius += 8;
            s.life--;

            if(s.life <= 0){
                wi.remove();
            }
        }
    }

    public void draw(Graphics2D g,double camX,double camY,BufferedImage playerImg,double px,double py,double speedMult){

        for(AfterImage a : ghosts){

            float alpha = a.life / 20f;

            Composite old = g.getComposite();

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha*0.5f));

            int sx = (int)(a.x - camX - playerImg.getWidth()/2);
            int sy = (int)(a.y - camY - playerImg.getHeight()/2);

            g.drawImage(playerImg,sx,sy,null);

            g.setComposite(old);
        }

        for(SpeedLine l : lines){

            int sx = (int)(l.x - camX);
            int sy = (int)(l.y - camY);

            int ex = (int)(sx + l.vx*4);
            int ey = (int)(sy + l.vy*4);

            float alpha = l.life / 20f;

            Composite old = g.getComposite();

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            g.setColor(Color.WHITE);
            g.drawLine(sx,sy,ex,ey);

            g.setComposite(old);
        }

        for(Shockwave s : waves){

            int sx = (int)(s.x - camX);
            int sy = (int)(s.y - camY);

            float alpha = s.life / 25f;

            Composite old = g.getComposite();

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            g.setColor(Color.WHITE);

            int r = (int)s.radius;

            g.drawOval(sx-r,sy-r,r*2,r*2);

            g.setComposite(old);
        }

        if(speedMult > 1.0){

            int sx = (int)(px - camX);
            int sy = (int)(py - camY);

            int r = 30;

            Color aura = speedMult > 1.5 ? Color.GREEN : Color.RED;

            Composite old = g.getComposite();

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.35f));

            g.setColor(aura);

            g.fillOval(sx-r,sy-r,r*2,r*2);

            g.setComposite(old);
        }
    }
}