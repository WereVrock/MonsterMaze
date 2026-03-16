package wv.monstermaze.main;

import javax.sound.sampled.*;
import java.util.Random;

public class FootstepSound {

    private static final Random rand = new Random();
    private static boolean leftFoot = true; // alternates left/right

    public static void play() {
        new Thread(() -> {
            try {
                float sampleRate = 44100;
                int length = (int) (sampleRate * 0.1); // 100ms footstep
                byte[] buf = new byte[length];

                // Lower volume
                double baseVolume = 0.3; // ~30% volume

                // Slight difference for left/right
                double footVolume = leftFoot ? baseVolume : baseVolume * 0.5;
                leftFoot = !leftFoot;

                for (int i = 0; i < length; i++) {
                    double t = i / sampleRate;
                    double noise = rand.nextDouble() * 2 - 1;        // white noise
                    double envelope = Math.exp(-15 * t);            // fast decay
                    buf[i] = (byte) (noise * envelope * 127 * footVolume);
                }

                AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);
                try (SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
                    line.open(af);
                    line.start();
                    line.write(buf, 0, buf.length);
                    line.drain();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}