package wv.monstermaze.main;

public class SpeedBoost {

    public enum Type { NONE, GREEN, RED }

    private double multiplier = 1.0;
    private long endTime = 0;
    private Type currentType = Type.NONE;

    public void trigger(double mult, int seconds, Type type) {
        multiplier = mult;
        endTime = System.currentTimeMillis() + seconds * 1000L;
        currentType = type;
    }

    public void update() {
        if (multiplier == 1.0) return;

        if (System.currentTimeMillis() > endTime) {
            multiplier = 1.0;
            currentType = Type.NONE;
        }
    }

    public double getMultiplier() { return multiplier; }

    public Type getType() { return currentType; }

    public boolean isGreenBoost() { return currentType == Type.GREEN; }

    public boolean isRedBoost() { return currentType == Type.RED; }
}