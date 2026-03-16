package wv.monstermaze.main;

import javax.sound.sampled.*;

public class TeleportSound {

    public static void play() {

        new Thread(() -> {

            try {

                int sampleRate = 44100;
                int durationMs = 200;
                int samples = (sampleRate * durationMs) / 1000;

                byte[] buffer = new byte[samples * 2];

                double startFreq = 220;
                double endFreq = 1400;

                for (int i = 0; i < samples; i++) {

                    double t = (double) i / samples;

                    double freq = startFreq + (endFreq - startFreq) * t;

                    double phase = 2 * Math.PI * freq * i / sampleRate;

                    double envelope = 1.0 - t;

                    short value = (short) (Math.sin(phase) * 32767 * envelope);

                    buffer[i * 2] = (byte) (value & 0xff);
                    buffer[i * 2 + 1] = (byte) ((value >> 8) & 0xff);
                }

                AudioFormat format = new AudioFormat(
                        sampleRate,
                        16,
                        1,
                        true,
                        false
                );

                SourceDataLine line = AudioSystem.getSourceDataLine(format);

                line.open(format);
                line.start();

                line.write(buffer, 0, buffer.length);

                line.drain();
                line.stop();
                line.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }
}