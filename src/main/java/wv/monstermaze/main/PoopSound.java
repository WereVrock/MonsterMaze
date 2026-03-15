package wv.monstermaze.main;

import java.io.InputStream;
import java.util.Random;
import javazoom.jl.player.Player;

public class PoopSound {

    private static final Random RANDOM = new Random();

    public static void play() {

        new Thread(() -> {

            try {
                // 50% chance
                String fileName = RANDOM.nextBoolean() ? "poop.mp3" : "poop2.mp3";

                InputStream is = PoopSound.class
                        .getClassLoader()
                        .getResourceAsStream(fileName);

                if (is == null) {
                    System.out.println(fileName + " not found");
                    return;
                }

                Player mp3Player = new Player(is);
                mp3Player.play();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();

    }
}