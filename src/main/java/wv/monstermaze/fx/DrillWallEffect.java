package wv.monstermaze.fx;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.sound.sampled.*;

public class DrillWallEffect {

    private static class Particle {

        double x,y;
        double vx,vy;
        double life;
        int size;

        Particle(double x,double y){

            this.x=x;
            this.y=y;

            double a=Math.random()*Math.PI*2;
            double s=1+Math.random()*4;

            vx=Math.cos(a)*s;
            vy=Math.sin(a)*s;

            life=30+Math.random()*20;
            size=6+(int)(Math.random()*6);
        }

        void update(){

            x+=vx;
            y+=vy;

            vy+=0.1;

            life--;
        }

        boolean dead(){
            return life<=0;
        }
    }

    private List<Particle> particles=new ArrayList<>();

    public void trigger(double x,double y){

        for(int i=0;i<35;i++){
            particles.add(new Particle(x,y));
        }

        playDrillSound();
    }

    public void update(){

        Iterator<Particle> it=particles.iterator();

        while(it.hasNext()){

            Particle p=it.next();
            p.update();

            if(p.dead()) it.remove();
        }
    }

    public void draw(Graphics2D g2,double cameraX,double cameraY){

        g2.setColor(new Color(120,120,120));

        for(Particle p:particles){

            int sx=(int)(p.x-cameraX);
            int sy=(int)(p.y-cameraY);

            g2.fillOval(sx-p.size/2,sy-p.size/2,p.size,p.size);
        }
    }

    private void playDrillSound(){

        new Thread(()->{

            try{

                float sampleRate=44100;
                byte[] buf=new byte[(int)sampleRate/4];

                for(int i=0;i<buf.length;i++){

                    double t=i/sampleRate;

                    double freq=90+Math.sin(t*50)*30;

                    double v=Math.sin(2*Math.PI*freq*t);

                    buf[i]=(byte)(v*90);
                }

                AudioFormat af=new AudioFormat(sampleRate,8,1,true,false);
                SourceDataLine line=AudioSystem.getSourceDataLine(af);

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