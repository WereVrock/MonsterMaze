package wv.monstermaze.main;

public class Player {

    public double x;
    public double y;

    public Player(double x,double y){
        this.x=x;
        this.y=y;
    }

    public double distance(double ox,double oy){
        double dx=x-ox;
        double dy=y-oy;
        return Math.sqrt(dx*dx+dy*dy);
    }
}