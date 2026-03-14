package wv.monstermaze.main;

import java.awt.image.BufferedImage;

public class Monster {

    public double x;
    public double y;

    public BufferedImage img;

    public Monster(double x,double y,BufferedImage img){
        this.x=x;
        this.y=y;
        this.img=img;
    }
}