package wv.monstermaze.main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ImageLoader {

    public static class LoadedImage {
        public final BufferedImage image;
        public final boolean vip;

        public LoadedImage(BufferedImage image, boolean vip) {
            this.image = image;
            this.vip = vip;
        }
    }

    public static class MonsterImagePool {
        public final List<LoadedImage> normal = new ArrayList<>();
        public final List<LoadedImage> vip = new ArrayList<>();
        public final List<LoadedImage> misc = new ArrayList<>();

        public LoadedImage getRandom(Random random) {

            if (normal.isEmpty() && misc.isEmpty() && vip.isEmpty()) {
                return null;
            }

            // --- VIP rare override ---
            if (!vip.isEmpty() && random.nextDouble() < 0.1) {
                return vip.get(random.nextInt(vip.size()));
            }

            // --- Misc flavor ---
            if (!misc.isEmpty() && random.nextDouble() < 0.2) {
                return misc.get(random.nextInt(misc.size()));
            }

            // --- Fallback normal ---
            if (!normal.isEmpty()) {
                return normal.get(random.nextInt(normal.size()));
            }

            // --- Absolute fallback ---
            if (!misc.isEmpty()) {
                return misc.get(random.nextInt(misc.size()));
            }

            return vip.get(random.nextInt(vip.size()));
        }
    }

    private BufferedImage scaleImage(BufferedImage original, int maxWidth, int maxHeight) {
        double ratio = Math.min((double) maxWidth / original.getWidth(),
                                (double) maxHeight / original.getHeight());
        int newWidth = (int)(original.getWidth() * ratio);
        int newHeight = (int)(original.getHeight() * ratio);

        BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2.dispose();
        return scaled;
    }

    private void loadSubfolder(File parent, String subfolderName, List<LoadedImage> targetList, int tileSize, boolean vip) {
        File folder = new File(parent, subfolderName);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    try {
                        if (!f.isFile()) continue;
                        BufferedImage img = ImageIO.read(f);
                        if (img != null) {
                            targetList.add(new LoadedImage(scaleImage(img, tileSize, tileSize), vip));
                            System.out.println("Loaded " + subfolderName + " image: " + f.getName() + (vip ? " [VIP]" : ""));
                        }
                    } catch (Exception e) {
                        System.out.println("Failed loading " + subfolderName + " image: " + f.getName());
                    }
                }
            }
        }
    }

    private void loadFolderImages(File folder, List<LoadedImage> targetList, int tileSize, boolean vip) {
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    try {
                        if (!f.isFile()) continue;
                        BufferedImage img = ImageIO.read(f);
                        if (img != null) {
                            targetList.add(new LoadedImage(scaleImage(img, tileSize, tileSize), vip));
                            System.out.println("Loaded folder image: " + f.getName() + (vip ? " [VIP]" : ""));
                        }
                    } catch (Exception e) {
                        System.out.println("Failed loading image: " + f.getName());
                    }
                }
            }
        }
    }

    public MonsterImagePool setupMonsterImages(int tileSize) {
        MonsterImagePool pool = new MonsterImagePool();
        File monstersFolder = new File("monsters");

        loadFolderImages(monstersFolder, pool.normal, tileSize, false);
        loadSubfolder(monstersFolder, "vip", pool.vip, tileSize, true);
        loadSubfolder(monstersFolder, "misc", pool.misc, tileSize, false);

        System.out.println("Monster setup completed: normal=" + pool.normal.size()
                + ", vip=" + pool.vip.size() + ", misc=" + pool.misc.size());
        return pool;
    }

    public List<LoadedImage> loadImages(String folderName, int tileSize) {
        List<LoadedImage> images = new ArrayList<>();
        File folder = new File(folderName);
        loadFolderImages(folder, images, tileSize, folderName.toLowerCase().contains("vip"));
        loadSubfolder(folder, "vip", images, tileSize, true);
        return images;
    }
}