package wv.monstermaze.fx;

import javax.sound.sampled.*;

public class ToiletAlertSound {

    public static void play() {

        new Thread(() -> {

            try {

                float sampleRate = 44100f;
                int durationMs = 120;

                int samples = (int)((durationMs / 1000f) * sampleRate);

                byte[] buffer = new byte[samples];

                for (int i = 0; i < samples; i++) {

                    double t = i / sampleRate;

                    // descending tone
                    double freq = 900 - (t * 600);

                    double wave = Math.sin(2 * Math.PI * freq * t);

                    // short decay envelope
                    double env = 1.0 - (double)i / samples;

                    buffer[i] = (byte)(wave * env * 127);
                }

                AudioFormat format = new AudioFormat(
                        sampleRate,
                        8,
                        1,
                        true,
                        false
                );

                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format, samples);
                line.start();

                line.write(buffer, 0, buffer.length);

                line.drain();
                line.close();

            } catch (Exception ignored) {}
        }).start();

    }

}