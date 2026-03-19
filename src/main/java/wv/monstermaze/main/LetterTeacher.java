package wv.monstermaze.main;

import javax.sound.sampled.*;
import java.awt.*;
import java.io.File;
import java.util.Locale;

public class LetterTeacher {

    private static String allowedLetters = "D"; // Editable single string

    private static char currentLetter;
    private static boolean active = false;

    private static long startTime;
    private static final long DURATION = 2500;

    private static float alpha = 0f;
    private static float scale = 0f;

    private static final String SOUND_DIR =
            "C:\\Users\\SC\\Documents\\NetBeansProjects\\MonsterMaze\\src\\main\\resources\\letterSounds";

    private static final Locale TR = new Locale("tr", "TR");

    // Cached font (reused)
    private static Font cachedFont = new Font("Arial", Font.BOLD, 100);
    private static int lastFontSize = -1;

    public static void setAllowedLetters(String letters) {
        if (letters == null) return;
        allowedLetters = letters.toUpperCase(TR);
    }

    public static void trigger(String word) {
        if (word == null || word.isEmpty()) return;

        String upper = word.toUpperCase(TR);
        char first = upper.charAt(0);

//        if (allowedLetters.indexOf(first) == -1) return;

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

        // Fade
        if (t < 0.2f) alpha = t / 0.2f;
        else if (t > 0.8f) alpha = (1f - t) / 0.2f;
        else alpha = 1f;

        // Scale (pop)
        scale = (float)(1.0 + Math.sin(t * Math.PI * 2) * 0.2);

        int baseSize = (int)(screenH * 0.5 * scale);

        // Font caching
        if (baseSize != lastFontSize) {
            cachedFont = new Font("Arial", Font.BOLD, baseSize);
            lastFontSize = baseSize;
        }

        String text = String.valueOf(currentLetter);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.setFont(cachedFont);

        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(text);
        int textH = fm.getAscent();

        int x = (screenW - textW) / 2;
        int y = (screenH + textH) / 2;

        // === Optimized Glow (reduced from ~80 draws to 8) ===
        g2.setColor(new Color(255, 255, 0, (int)(alpha * 80)));
        int glow = 8;
        g2.drawString(text, x - glow, y);
        g2.drawString(text, x + glow, y);
        g2.drawString(text, x, y - glow);
        g2.drawString(text, x, y + glow);
        g2.drawString(text, x - glow, y - glow);
        g2.drawString(text, x + glow, y - glow);
        g2.drawString(text, x - glow, y + glow);
        g2.drawString(text, x + glow, y + glow);

        // === Optimized Outline (diamond instead of circle) ===
        g2.setColor(Color.BLACK);
        int outline = 4;
        for (int dx = -outline; dx <= outline; dx++) {
            for (int dy = -outline; dy <= outline; dy++) {
                if (Math.abs(dx) + Math.abs(dy) <= outline) {
                    g2.drawString(text, x + dx, y + dy);
                }
            }
        }

        // Main
        g2.setColor(Color.WHITE);
        g2.drawString(text, x, y);

        g2.dispose();
    }
}