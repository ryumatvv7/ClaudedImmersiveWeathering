import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

/** Generates the ash layer block texture (soft gray noise). java tools/AshTexture.java */
public class AshTexture {
    public static void main(String[] a) throws Exception {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Random r = new Random(42);
        int[] palette = {0x5c5a58, 0x6b6966, 0x777471, 0x83807c, 0x8f8b87, 0x4e4c4a, 0x9a9691};
        int[] weights = {3, 5, 6, 5, 3, 2, 1};
        int total = 0;
        for (int w : weights) total += w;
        for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) {
            int n = r.nextInt(total), i = 0;
            while (n >= weights[i]) n -= weights[i++];
            int c = palette[i];
            //few glowing embers
            if (r.nextInt(90) == 0) c = 0xb0703a;
            img.setRGB(x, y, 0xFF000000 | c);
        }
        ImageIO.write(img, "png", new File("src/main/resources/assets/immersive_weathering/textures/block/ash.png"));
    }
}
