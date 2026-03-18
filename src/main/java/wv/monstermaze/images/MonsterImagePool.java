package wv.monstermaze.images;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

 public  class MonsterImagePool {
        public final List<ImageLoader.LoadedImage> normal = new ArrayList<>();
        public final List<ImageLoader.LoadedImage> vip = new ArrayList<>();
        public final List<ImageLoader.LoadedImage> misc = new ArrayList<>();

        public ImageLoader.LoadedImage getRandom(Random random) {

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
        
        public ImageLoader.LoadedImage getTrueRandom(Random random) {

    int totalSize = normal.size() + misc.size() + vip.size();

    if (totalSize == 0) {
        return null;
    }

    int index = random.nextInt(totalSize);

    if (index < normal.size()) {
        return normal.get(index);
    }

    index -= normal.size();

    if (index < misc.size()) {
        return misc.get(index);
    }

    index -= misc.size();

    return vip.get(index);
}
    }