package wv.monstermaze.main;

public class PlayerFreeze {

    private long freezeEnd = 0;

    public void trigger(double seconds) {
        freezeEnd = System.currentTimeMillis() + (long)(seconds * 1000);
    }

    public boolean isFrozen() {
        return System.currentTimeMillis() < freezeEnd;
    }
}