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
                                int mr = (m >> 16) & 255, mg = (m >> 8) & 255, mb = m & 255;
                                if (mg > mr + 8 && mg > mb + 8) r = m; //only the green moss pixels
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
                int w = base.getWidth();
                BufferedImage o = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
                for (int y = 0; y < base.getHeight(); y++)
                    for (int x = 0; x < base.getWidth(); x++) {
                        int c = base.getRGB(x, y);
                        if (a(c) == 0) continue;
                        int cr = (c >> 16) & 255, cg = (c >> 8) & 255, cb = c & 255;
                        if (cr > 170 && cr > cb + 50) { //keep flames / light (lanterns)
                            o.setRGB(x, y, c);
                            continue;
                        }
                        // same noise in every animation frame; rust grows in clumps
                        int fy = y % w;
                        double noise = (new Random(x * 31L + fy * 17L).nextDouble() + ((x * 7 + fy * 13) % 5) / 5.0) / 2;
                        if (noise < share) {
                            int n = pick(rust, rank(bSorted, c));
                            o.setRGB(x, y, (c & 0xFF000000) | (n & 0xFFFFFF));
                        } else o.setRGB(x, y, c);
                    }
                write(o, args[4]);
            }
            case "recolor_liquid" -> {
                // recolor_liquid TEMPLATE PALETTE OUT: like recolor, but only bright warm pixels (the honey in a bottle)
                BufferedImage t = read(args[1]);
                List<Integer> warm = new ArrayList<>();
                for (int y = 0; y < t.getHeight(); y++)
                    for (int x = 0; x < t.getWidth(); x++) {
                        int c = t.getRGB(x, y);
                        if (a(c) > 128 && ((c >> 16) & 255) > 150 && (c & 255) < 110) warm.add(c);
                    }
                warm.sort((p, q) -> Double.compare(lum(p), lum(q)));
                List<Integer> pal = sortedOpaque(read(args[2]));
                BufferedImage o = new BufferedImage(t.getWidth(), t.getHeight(), BufferedImage.TYPE_INT_ARGB);
                for (int y = 0; y < t.getHeight(); y++)
                    for (int x = 0; x < t.getWidth(); x++) {
                        int c = t.getRGB(x, y);
                        boolean isWarm = a(c) > 128 && ((c >> 16) & 255) > 150 && (c & 255) < 110;
                        o.setRGB(x, y, isWarm ? (c & 0xFF000000) | (pick(pal, rank(warm, c)) & 0xFFFFFF) : c);
                    }
                write(o, args[3]);
            }
            case "rot" -> {
                // rot BASE OUT: dark, damp, slightly green wood with darker rot spots
                BufferedImage base = read(args[1]);
                List<Integer> bSorted = sortedOpaque(base);
                int w = base.getWidth();
                int[] dark = {0x24, 0x1e, 0x13}, light = {0x6e, 0x5f, 0x41}, moss = {0x4e, 0x5a, 0x2c};
                BufferedImage o = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
                for (int y = 0; y < base.getHeight(); y++)
                    for (int x = 0; x < base.getWidth(); x++) {
                        int c = base.getRGB(x, y);
                        if (a(c) == 0) continue;
                        double q = rank(bSorted, c);
                        double n = new Random(x * 131L + (y % w) * 71L).nextDouble();
                        int[] hi = n > 0.82 ? moss : light;
                        if (n < 0.12) q *= 0.4; // rot spots
                        int r = (int) (dark[0] + (hi[0] - dark[0]) * q);
                        int g = (int) (dark[1] + (hi[1] - dark[1]) * q);
                        int b = (int) (dark[2] + (hi[2] - dark[2]) * q);
                        o.setRGB(x, y, (c & 0xFF000000) | (r << 16) | (g << 8) | b);
                    }
                write(o, args[2]);
            }
            case "preview" -> {
                // preview OUT IMG...: the first 16x16 frame of every image, 8x scaled, side by side
                int n = args.length - 2, s = 8;
                BufferedImage o = new BufferedImage(n * 16 * s + (n - 1) * 4, 16 * s, BufferedImage.TYPE_INT_ARGB);
                for (int k = 0; k < n; k++) {
                    BufferedImage im = read(args[k + 2]);
                    for (int y = 0; y < 16 * s; y++)
                        for (int x = 0; x < 16 * s; x++)
                            o.setRGB(k * (16 * s + 4) + x, y, im.getRGB(x / s * im.getWidth() / 16, y / s * im.getWidth() / 16));
                }
                write(o, args[1]);
            }
            default -> throw new IllegalArgumentException(args[0]);
        }
    }
}
