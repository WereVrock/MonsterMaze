package wv.monstermaze.main;

import javax.sound.sampled.*;
import java.awt.*;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class LetterTeacher {

    private static final Set<Character> allowedLetters = new HashSet<>();

    private static char currentLetter;
    private static boolean active = false;

    private static long startTime;
    private static final long DURATION = 2500; // ms

    private static float alpha = 0f;
    private static float scale = 0f;

    private static final String SOUND_DIR =
            "C:\\Users\\SC\\Documents\\NetBeansProjects\\MonsterMaze\\src\\main\\resources\\letterSounds";

    static {
        // Allowed letters (edit freely)
        for (char c = 'A'; c <= 'Z'; c++) {
            allowedLetters.add(c);
        }
    }

    public static void trigger(String word) {
        if (word == null || word.isEmpty()) return;

        char first = Character.toUpperCase(word.charAt(0));

        if (!allowedLetters.contains(first)) return;

        currentLetter = first;
        startTime = System.currentTimeMillis();
        active = true;

        playSound(first);
    }

    private static void playSound(char letter) {
        try {
            File file = new File(SOUND_DIR + "\\" + letter + ".wav");
            if (!file.exists()) return;

            AudioInputStream audio = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void draw(Graphics2D g, int screenW, int screenH) {
        if (!active) return;

        long now = System.currentTimeMillis();
        float t = (now - startTime) / (float) DURATION;

        if (t >= 1f) {
            active = false;
            return;
        }

        // === Animation curves ===

        // Fade in/out
        if (t < 0.2f) alpha = t / 0.2f;
        else if (t > 0.8f) alpha = (1f - t) / 0.2f;
        else alpha = 1f;

        // Punchy scale (pop effect)
        scale = (float)(1.0 + Math.sin(t * Math.PI * 2) * 0.2);

        // Base size
        int baseSize = (int)(screenH * 0.5 * scale);

        String text = String.valueOf(currentLetter);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // Center
        int cx = screenW / 2;
        int cy = screenH / 2;

        // Font
        Font font = new Font("Arial", Font.BOLD, baseSize);
        g2.setFont(font);

        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(text);
        int textH = fm.getAscent();

        int x = cx - textW / 2;
        int y = cy + textH / 2;

        // === Glow effect ===
        g2.setColor(new Color(255, 255, 0, (int)(alpha * 80)));
        for (int i = 0; i < 20; i++) {
            int spread = i * 2;
            g2.drawString(text, x - spread, y - spread);
            g2.drawString(text, x + spread, y + spread);
            g2.drawString(text, x - spread, y + spread);
            g2.drawString(text, x + spread, y - spread);
        }

        // === Outline ===
        g2.setColor(Color.BLACK);
        for (int dx = -6; dx <= 6; dx++) {
            for (int dy = -6; dy <= 6; dy++) {
                if (dx * dx + dy * dy < 36) {
                    g2.drawString(text, x + dx, y + dy);
                }
            }
        }

        // === Main Letter ===
        g2.setColor(Color.WHITE);
        g2.drawString(text, x, y);

        g2.dispose();
    }
}