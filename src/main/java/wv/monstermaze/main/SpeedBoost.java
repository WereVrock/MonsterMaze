package wv.monstermaze.main;

public class SpeedBoost {

    private double multiplier = 1.0;
    private long endTime = 0;

    public void trigger(double mult, int seconds) {

        multiplier = mult;
        endTime = System.currentTimeMillis() + seconds * 1000L;
    }

    public void update() {

        if(multiplier == 1.0) return;

        if(System.currentTimeMillis() > endTime){
            multiplier = 1.0;
        }
    }

    public double getMultiplier(){
        return multiplier;
    }

}