package wv.monstermaze.fx;

import java.awt.*;
import java.util.*;
import java.util.List;

public class TeleportSwapFX {

    private static class Particle {

        double x;
        double y;
        double vx;
        double vy;
        int life;

        Particle(double x,double y,double vx,double vy){
            this.x=x;
            this.y=y;
            this.vx=vx;
            this.vy=vy;
            this.life=28;
        }
    }

    private static class Ring {

        double x;
        double y;
        double radius = 8;
        int life = 22;
    }

    private static class Spark {

        double x;
        double y;
        double vx;
        double vy;
        int life;

        Spark(double x,double y,double vx,double vy){
            this.x=x;
            this.y=y;
            this.vx=vx;
            this.vy=vy;
            this.life=16;
        }
    }

    private final List<Particle> particles = new ArrayList<>();
    private final List<Ring> rings = new ArrayList<>();
    private final List<Spark> sparks = new ArrayList<>();

    private final Random rand = new Random();

    public void trigger(double x,double y){

        Ring r = new Ring();
        r.x = x;
        r.y = y;
        rings.add(r);

        for(int i=0;i<18;i++){

            double angle = rand.nextDouble()*Math.PI*2;
            double speed = 2 + rand.nextDouble()*3;

            double vx = Math.cos(angle)*speed;
            double vy = Math.sin(angle)*speed;

            particles.add(new Particle(x,y,vx,vy));
        }

        for(int i=0;i<10;i++){

            double angle = rand.nextDouble()*Math.PI*2;
            double speed = 3 + rand.nextDouble()*4;

            double vx = Math.cos(angle)*speed;
            double vy = Math.sin(angle)*speed;

            sparks.add(new Spark(x,y,vx,vy));
        }
    }

    public void update(){

        Iterator<Particle> pi = particles.iterator();

        while(pi.hasNext()){

            Particle p = pi.next();

            p.x += p.vx;
            p.y += p.vy;

            p.vx *= 0.92;
            p.vy *= 0.92;

            p.life--;

            if(p.life <= 0){
                pi.remove();
            }
        }

        Iterator<Spark> si = sparks.iterator();

        while(si.hasNext()){

            Spark s = si.next();

            s.x += s.vx;
            s.y += s.vy;

            s.vx *= 0.88;
            s.vy *= 0.88;

            s.life--;

            if(s.life <= 0){
                si.remove();
            }
        }

        Iterator<Ring> ri = rings.iterator();

        while(ri.hasNext()){

            Ring r = ri.next();

            r.radius += 9;
            r.life--;

            if(r.life <= 0){
                ri.remove();
            }
        }
    }

    public void draw(Graphics2D g,double camX,double camY){

        g.setColor(new Color(180,120,255));

        for(Particle p : particles){

            int sx = (int)(p.x - camX);
            int sy = (int)(p.y - camY);

            g.fillOval(sx-3,sy-3,6,6);
        }

        g.setColor(new Color(255,220,255));

        for(Spark s : sparks){

            int sx = (int)(s.x - camX);
            int sy = (int)(s.y - camY);

            g.drawLine(sx,sy,(int)(sx - s.vx*2),(int)(sy - s.vy*2));
        }

        g.setColor(new Color(220,160,255));

        for(Ring r : rings){

            int sx = (int)(r.x - camX);
            int sy = (int)(r.y - camY);

            int rad = (int)r.radius;

            g.drawOval(sx-rad,sy-rad,rad*2,rad*2);
        }
    }
}