package wv.monstermaze.main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageLoader {

    public java.util.List<BufferedImage> loadImages(String folderName, int tileSize) {
        java.util.List<BufferedImage> images = new ArrayList<>();

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
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) {
                        images.add(scaleImage(img, tileSize, tileSize));
                        System.out.println("Loaded " + folderName + " image: " + f.getName());
                    }
                } catch (Exception e) {
                    System.out.println("Failed loading: " + f.getName());
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