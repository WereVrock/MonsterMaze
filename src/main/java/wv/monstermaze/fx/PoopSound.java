package wv.monstermaze.fx;

import java.io.InputStream;
import java.util.Random;
import javazoom.jl.player.advanced.AdvancedPlayer;

public class PoopSound {

    private static final Random RANDOM = new Random();

    public static void play() {

        new Thread(() -> {

            try {

                String fileName = RANDOM.nextBoolean() ? "poop.mp3" : "poop2.mp3";

                InputStream is = PoopSound.class
                        .getClassLoader()
                        .getResourceAsStream(fileName);

                if (is == null) {
                    System.out.println(fileName + " not found");
                    return;
                }

                BoostedAudioDevice device = new BoostedAudioDevice(5f);
                AdvancedPlayer player = new AdvancedPlayer(is, device);

                player.play();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();

    }
    public static void main (String... a){
        play();
    }
}