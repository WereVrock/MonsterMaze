package wv.monstermaze.main;

import javax.sound.sampled.*;
import java.util.Random;

public class SpeedWooshSound {

    private static volatile boolean running = false;
    private static volatile double targetStrength = 0;

    private static Thread audioThread;

    public static void start(double strength) {

        targetStrength = strength;

        if (running) return;

        running = true;

        audioThread = new Thread(SpeedWooshSound::audioLoop);
        audioThread.setDaemon(true);
        audioThread.start();
    }

    public static void update(double strength) {
        targetStrength = strength;
    }

    public static void stop() {
        targetStrength = 0;
    }

    private static void audioLoop() {

        try {

            float sampleRate = 44100;

            AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);

            SourceDataLine line = AudioSystem.getSourceDataLine(af);

            line.open(af, 2048);
            line.start();

            byte[] buffer = new byte[1024];

            Random rand = new Random();

            double low = 0;
            double high;

            double strength = 0;

            while (running) {

                strength += (targetStrength - strength) * 0.05;

                if (strength < 0.01 && targetStrength == 0) {
                    running = false;
                    break;
                }

                for (int i = 0; i < buffer.length; i++) {

                    double noise = rand.nextDouble() * 2 - 1;

                    double cutoff = 0.05 + strength * 0.08;

                    low += cutoff * (noise - low);

                    high = noise - low;

                    buffer[i] = (byte)(high * 20 * strength);
                }

                line.write(buffer, 0, buffer.length);
            }

            line.drain();
            line.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}