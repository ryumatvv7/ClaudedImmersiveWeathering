import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Small texture generator for the port's new blocks / items (run with: java tools/TexGen.java <op> ...).
 * <ul>
 *   <li>recolor TEMPLATE PALETTE OUT: keeps the template's shape and shading, takes colors from PALETTE
 *       (pixels are matched by brightness rank).</li>
 *   <li>moss BASE PLAIN MOSSY OUT: copies the moss pixels (where MOSSY differs from PLAIN) onto BASE.</li>
 *   <li>crack BASE PLAIN CRACKED OUT: darkens BASE where CRACKED differs from PLAIN.</li>
 *   <li>rust BASE RUST_PALETTE LEVEL OUT: turns a share of BASE's opaque pixels (LEVEL 1..3) into rust colors.</li>
 * </ul>
 * Only the first 16 rows are used for animated (strip) textures.
 */
public class TexGen {

    static BufferedImage read(String p) throws Exception {
        BufferedImage i = ImageIO.read(new File(p));
        BufferedImage o = new BufferedImage(i.getWidth(), i.getHeight(), BufferedImage.TYPE_INT_ARGB);
        o.getGraphics().drawImage(i, 0, 0, null);
        return o;
    }

    static void write(BufferedImage i, String p) throws Exception {
        File f = new File(p);
        f.getParentFile().mkdirs();
        ImageIO.write(i, "png", f);
    }

    static int a(int c) { return c >>> 24; }

    static double lum(int c) {
        return 0.299 * ((c >> 16) & 255) + 0.587 * ((c >> 8) & 255) + 0.114 * (c & 255);
    }

    static List<Integer> sortedOpaque(BufferedImage img) {
        List<Integer> l = new ArrayList<>();
        for (int y = 0; y < Math.min(img.getHeight(), img.getWidth()); y++)
            for (int x = 0; x < img.getWidth(); x++) {
                int c = img.getRGB(x, y);
                if (a(c) > 128) l.add(c);
            }
        l.sort((p, q) -> Double.compare(lum(p), lum(q)));
        return l;
    }

    static int pick(List<Integer> sorted, double q) {
        int i = (int) Math.round(q * (sorted.size() - 1));
        return sorted.get(Math.max(0, Math.min(sorted.size() - 1, i)));
    }

    static double rank(List<Integer> sorted, int c) {
        double l = lum(c);
        int n = 0;
        for (int s : sorted) if (lum(s) < l) n++;
        return sorted.size() <= 1 ? 0.5 : (double) n / (sorted.size() - 1);
    }

    public static void main(String[] args) throws Exception {
        switch (args[0]) {
            case "recolor" -> {
                BufferedImage t = read(args[1]);
                List<Integer> tSorted = sortedOpaque(t);
                List<Integer> pal = sortedOpaque(read(args[2]));
                BufferedImage o = new BufferedImage(t.getWidth(), t.getHeight(), BufferedImage.TYPE_INT_ARGB);
                for (int y = 0; y < t.getHeight(); y++)
                    for (int x = 0; x < t.getWidth(); x++) {
                        int c = t.getRGB(x, y);
                        if (a(c) == 0) continue;
                        int n = pick(pal, rank(tSorted, c));
                        o.setRGB(x, y, (c & 0xFF000000) | (n & 0xFFFFFF));
                    }
                write(o, args[3]);
            }
            case "moss", "crack" -> {
                BufferedImage base = read(args[1]), plain = read(args[2]), changed = read(args[3]);
                BufferedImage o = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                for (int y = 0; y < 16; y++)
                    for (int x = 0; x < 16; x++) {
                        int b = base.getRGB(x, y), p = plain.getRGB(x, y), m = changed.getRGB(x, y);
                        boolean diff = Math.abs(lum(p) - lum(m)) > 6 || Math.abs(((p >> 8) & 255) - ((m >> 8) & 255)) > 10;
                        int r = b;
                        if (diff) {
                            if (args[0].equals("moss")) {
                                r = m;
                            } else if (lum(m) < lum(p)) {
                                double k = Math.max(0.35, lum(m) / Math.max(1, lum(p)));
                                r = (b & 0xFF000000) | ((int) (((b >> 16) & 255) * k) << 16)
                                        | ((int) (((b >> 8) & 255) * k) << 8) | (int) ((b & 255) * k);
                            }
                        }
                        o.setRGB(x, y, r);
                    }
                write(o, args[4]);
            }
            case "rust" -> {
                BufferedImage base = read(args[1]);
                List<Integer> rust = sortedOpaque(read(args[2]));
                List<Integer> bSorted = sortedOpaque(base);
                int level = Integer.parseInt(args[3]);
                double share = new double[]{0, 0.3, 0.6, 0.95}[level];
                Random r = new Random(1234 + level);
                BufferedImage o = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
                for (int y = 0; y < base.getHeight(); y++)
                    for (int x = 0; x < base.getWidth(); x++) {
                        int c = base.getRGB(x, y);
                        if (a(c) == 0) continue;
                        // rust grows in clumps: neighbours share part of the noise
                        double noise = (r.nextDouble() + ((x * 7 + y * 13) % 5) / 5.0) / 2;
                        if (noise < share) {
                            int n = pick(rust, rank(bSorted, c));
                            o.setRGB(x, y, (c & 0xFF000000) | (n & 0xFFFFFF));
                        } else o.setRGB(x, y, c);
                    }
                write(o, args[4]);
            }
            default -> throw new IllegalArgumentException(args[0]);
        }
    }
}
