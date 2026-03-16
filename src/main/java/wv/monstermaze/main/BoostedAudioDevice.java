package wv.monstermaze.main;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.JavaSoundAudioDevice;

public class BoostedAudioDevice extends JavaSoundAudioDevice {

    private final float gain;

    public BoostedAudioDevice(float gain) {
        this.gain = gain;
    }

    @Override
    protected void writeImpl(short[] samples, int offs, int len) {

        for (int i = offs; i < offs + len; i++) {

            int boosted = (int) (samples[i] * gain);

            if (boosted > Short.MAX_VALUE) boosted = Short.MAX_VALUE;
            if (boosted < Short.MIN_VALUE) boosted = Short.MIN_VALUE;

            samples[i] = (short) boosted;
        }

        try {
            super.writeImpl(samples, offs, len);
        } catch (JavaLayerException ex) {
            System.getLogger(BoostedAudioDevice.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
}