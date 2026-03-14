package wv.monstermaze.main;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.sound.sampled.*;

public class HappyBumpEffect {

    private static class Particle {

        double x;
        double y;

        double vx;
        double vy;

        double life;

        int size;

        Color color;

        public Particle(double x,double y){

            this.x = x;
            this.y = y;

            double angle = Math.random() * Math.PI * 2;
            double speed = 1 + Math.random() * 3;

            vx = Math.cos(angle) * speed;
            vy = Math.sin(angle) * speed;

            life = 40 + Math.random() * 20;

            size = 6 + (int)(Math.random()*8);

            Color[] palette = {
                Color.YELLOW,
                Color.PINK,
                Color.CYAN,
                Color.GREEN,
                Color.ORANGE
            };

            color = palette[(int)(Math.random()*palette.length)];
        }

        public void update(){

            x += vx;
            y += vy;

            vy += 0.05;

            life--;
        }

        public boolean dead(){
            return life <= 0;
        }
    }

    private List<Particle> particles = new ArrayList<>();

    public void trigger(double x,double y){

        for(int i=0;i<20;i++){
            particles.add(new Particle(x,y));
        }

        playBoing();
    }

    public void update(){

        Iterator<Particle> it = particles.iterator();

        while(it.hasNext()){

            Particle p = it.next();

            p.update();

            if(p.dead()){
                it.remove();
            }
        }
    }

    public void draw(Graphics2D g2,double cameraX,double cameraY){

        for(Particle p : particles){

            int sx = (int)(p.x - cameraX);
            int sy = (int)(p.y - cameraY);

            g2.setColor(p.color);

            g2.fillOval(
                sx - p.size/2,
                sy - p.size/2,
                p.size,
                p.size
            );
        }
    }

    private void playBoing(){

        new Thread(() -> {

            try{

                float sampleRate = 44100;

                byte[] buf = new byte[(int)sampleRate];

                for(int i=0;i<buf.length;i++){

                    double t = i / sampleRate;

                    double freq = 500 - t * 200;

                    double v = Math.sin(2 * Math.PI * freq * t);

                    buf[i] = (byte)(v * 80);
                }

                AudioFormat af = new AudioFormat(sampleRate,8,1,true,false);

                SourceDataLine line = AudioSystem.getSourceDataLine(af);

                line.open(af);

                line.start();

                line.write(buf,0,buf.length);

                line.drain();

                line.close();

            }catch(Exception e){
                e.printStackTrace();
            }

        }).start();
    }
}