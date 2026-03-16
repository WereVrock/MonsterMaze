package wv.monstermaze.main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageLoader {

    public static class LoadedImage {
        public final BufferedImage image;
        public final boolean vip;

        public LoadedImage(BufferedImage image, boolean vip) {
            this.image = image;
            this.vip = vip;
        }
    }

    public List<LoadedImage> loadImages(String folderName, int tileSize) {
        List<LoadedImage> images = new ArrayList<>();

        try {
            File folder = new File(folderName);
            if (!folder.exists()) {
                System.out.println(folderName + " folder not found: " + folder.getAbsolutePath());
                return images;
            }

            File[] files = folder.listFiles();
            if (files == null || files.length == 0) {
                System.out.println(folderName + " folder empty.");
                return images;
            }

            for (File f : files) {
                try {
                    if (!f.isFile()) continue;
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) {
                        boolean vip = folderName.toLowerCase().contains("vip");
                        images.add(new LoadedImage(scaleImage(img, tileSize, tileSize), vip));
                        System.out.println("Loaded " + folderName + " image: " + f.getName() + (vip ? " [VIP]" : ""));
                    }
                } catch (Exception e) {
                    System.out.println("Failed loading: " + f.getName());
                }
            }

            // also load VIP subfolder if exists
            File vipFolder = new File(folder, "vip");
            if (vipFolder.exists() && vipFolder.isDirectory()) {
                File[] vipFiles = vipFolder.listFiles();
                if (vipFiles != null) {
                    for (File f : vipFiles) {
                        try {
                            if (!f.isFile()) continue;
                            BufferedImage img = ImageIO.read(f);
                            if (img != null) {
                                images.add(new LoadedImage(scaleImage(img, tileSize, tileSize), true));
                                System.out.println("Loaded VIP image: " + f.getName());
                            }
                        } catch (Exception e) {
                            System.out.println("Failed loading VIP image: " + f.getName());
                        }
                    }
                }
            }

            System.out.println("Total " + folderName + " images loaded: " + images.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return images;
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
}