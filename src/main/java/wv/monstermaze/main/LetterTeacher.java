package wv.monstermaze.main;

import javax.sound.sampled.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LetterTeacher {

    private static String allowedLetters = "D";

    private static char currentLetter;
    private static boolean active = false;

    private static long startTime;
    private static final long DURATION = 2500;

    private static float alpha = 0f;
    private static float scale = 1f;

    private static final String SOUND_DIR =
            "C:\\Users\\SC\\Documents\\NetBeansProjects\\MonsterMaze\\src\\main\\resources\\letterSounds";

    private static final Locale TR = new Locale("tr", "TR");

    // === AUDIO CACHE ===
    private static final Map<Character, Clip> clipCache = new HashMap<>();

    // === FONT CACHE ===
    private static final Font baseFont = new Font("Arial", Font.BOLD, 100);
    private static Font cachedFont = baseFont;
    private static int lastFontSize = -1;

    // === TEXT CACHE ===
    private static String text = "";
    private static int textW, textH;

    public static void setAllowedLetters(String letters) {
        if (letters == null) return;
        allowedLetters = letters.toUpperCase(TR);
    }

    public static void trigger(String word) {
        if (word == null || word.isEmpty()) return;

        char first = word.toUpperCase(TR).charAt(0);

//        if (allowedLetters.indexOf(first) == -1) return;

        currentLetter = first;
        text = String.valueOf(first);

        startTime = System.currentTimeMillis();
        active = true;

        playSound(first);
    }

    // === CACHED AUDIO ===
    private static void playSound(char letter) {
        try {
            Clip clip = clipCache.get(letter);

            if (clip == null) {
                File file = new File(SOUND_DIR + "\\" + letter + ".wav");
                if (!file.exists()) return;

                AudioInputStream audio = AudioSystem.getAudioInputStream(file);
                clip = AudioSystem.getClip();
                clip.open(audio);

                clipCache.put(letter, clip);
            }

            if (clip.isRunning()) {
                clip.stop();
            }

            clip.setFramePosition(0);
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

        // === Fade ===
        alpha = (t < 0.2f) ? t / 0.2f :
                (t > 0.8f) ? (1f - t) / 0.2f : 1f;

        // === Scale (cheaper) ===
        scale = 1.0f + 0.2f * (float)Math.sin(t * Math.PI * 2);

        int baseSize = (int)(screenH * 0.5f * scale);

        // === Font derive instead of new ===
        if (baseSize != lastFontSize) {
            cachedFont = baseFont.deriveFont((float) baseSize);
            lastFontSize = baseSize;

            // Recalculate text metrics ONLY when font changes
            FontMetrics fm = g.getFontMetrics(cachedFont);
            textW = fm.stringWidth(text);
            textH = fm.getAscent();
        }

        int x = (screenW - textW) >> 1;
        int y = (screenH + textH) >> 1;

        // === Apply alpha WITHOUT creating new Graphics ===
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        g.setFont(cachedFont);

        // === ULTRA CHEAP GLOW (4 draws only) ===
        g.setColor(new Color(255, 255, 0, (int)(alpha * 80)));
        int glow = 6;
        g.drawString(text, x - glow, y);
        g.drawString(text, x + glow, y);
        g.drawString(text, x, y - glow);
        g.drawString(text, x, y + glow);

        // === CHEAP OUTLINE (8 draws max) ===
        g.setColor(Color.BLACK);
        int o = 2;
        g.drawString(text, x - o, y);
        g.drawString(text, x + o, y);
        g.drawString(text, x, y - o);
        g.drawString(text, x, y + o);
        g.drawString(text, x - o, y - o);
        g.drawString(text, x + o, y - o);
        g.drawString(text, x - o, y + o);
        g.drawString(text, x + o, y + o);

        // Main
        g.setColor(Color.WHITE);
        g.drawString(text, x, y);

        g.setComposite(old);
    }
}