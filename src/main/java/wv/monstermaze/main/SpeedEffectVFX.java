package wv.monstermaze.main;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class SpeedEffectVFX {

    private static class Particle {

        double x;
        double y;
        double vx;
        double vy;
        int life;
        Color color;

        Particle(double x,double y,double vx,double vy,int life,Color c){
            this.x=x;
            this.y=y;
            this.vx=vx;
            this.vy=vy;
            this.life=life;
            this.color=c;
        }
    }

    private List<Particle> particles = new ArrayList<>();
    private Random rand = new Random();

    public void triggerToiletBurst(double x,double y){

        for(int i=0;i<40;i++){

            double angle = rand.nextDouble()*Math.PI*2;
            double speed = 2 + rand.nextDouble()*3;

            double vx = Math.cos(angle)*speed;
            double vy = Math.sin(angle)*speed;

            Color c = rand.nextBoolean() ? Color.YELLOW : Color.WHITE;

            particles.add(new Particle(x,y,vx,vy,40+rand.nextInt(20),c));
        }
    }

    public void spawnBoostTrail(double x,double y,boolean high){

        Color c = high ? Color.GREEN : Color.RED;

        for(int i=0;i<3;i++){

            double vx = (rand.nextDouble()-0.5)*1.5;
            double vy = (rand.nextDouble()-0.5)*1.5;

            particles.add(new Particle(x,y,vx,vy,25,c));
        }
    }

    public void update(){

        Iterator<Particle> it = particles.iterator();

        while(it.hasNext()){

            Particle p = it.next();

            p.x += p.vx;
            p.y += p.vy;

            p.vx *= 0.95;
            p.vy *= 0.95;

            p.life--;

            if(p.life<=0){
                it.remove();
            }
        }
    }

    public void draw(Graphics2D g,double camX,double camY){

        for(Particle p:particles){

            int sx = (int)(p.x - camX);
            int sy = (int)(p.y - camY);

            g.setColor(p.color);

            int size = 4 + p.life/10;

            g.fillOval(sx-size/2,sy-size/2,size,size);
        }
    }
}