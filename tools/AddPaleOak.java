import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;

/**
 * One-off: creates static resources for the pale oak leaf pile and bark (wood type added after 1.20.1)
 * from the cherry (untinted leaves) and dark oak templates. Run from the project root.
 */
public class AddPaleOak {
    static final Path R = Path.of("src/main/resources");
    static final String A = "assets/immersive_weathering/";
    static final String D = "data/immersive_weathering/";

    public static void main(String[] args) throws IOException {
        // leaf pile: cherry is the untinted template; pale oak uses the vanilla leaves texture
        List<Path> cherry;
        try (Stream<Path> s = Files.walk(R)) {
            cherry = s.filter(Files::isRegularFile).filter(p -> p.getFileName().toString().contains("cherry_leaf_pile")
                    || p.getFileName().toString().equals("cherry_leaves.json")).toList();
        }
        for (Path p : cherry) copyJson(p, "cherry", "pale_oak", true);

        // bark: dark oak templates
        for (String f : List.of(A + "items/dark_oak_bark.json", A + "models/item/dark_oak_bark.json",
                D + "advancement/recipes/dark_oak_bark.json", D + "recipe/dark_oak_log_unstrip.json",
                D + "recipe/dark_oak_wood_from_bark.json", D + "recipe/dark_oak_wood_unstrip.json")) {
            copyJson(R.resolve(f), "dark_oak", "pale_oak", false);
        }

        // bark texture: grey-white recolor of the oak bark
        BufferedImage img = ImageIO.read(R.resolve(A + "textures/item/oak_bark.png").toFile());
        BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int min = 255, max = 0;
        for (int y = 0; y < img.getHeight(); y++) for (int x = 0; x < img.getWidth(); x++) {
            int c = img.getRGB(x, y);
            if ((c >>> 24) == 0) continue;
            int l = lum(c); min = Math.min(min, l); max = Math.max(max, l);
        }
        for (int y = 0; y < img.getHeight(); y++) for (int x = 0; x < img.getWidth(); x++) {
            int c = img.getRGB(x, y);
            int a = c >>> 24;
            if (a == 0) { out.setRGB(x, y, 0); continue; }
            float t = max == min ? 0.5f : (lum(c) - min) / (float) (max - min);
            // pale oak log palette: from dark grey-brown to almost white
            int r = (int) (86 + t * (232 - 86)), g = (int) (80 + t * (226 - 80)), b = (int) (78 + t * (222 - 78));
            out.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
        }
        ImageIO.write(out, "png", R.resolve(A + "textures/item/pale_oak_bark.png").toFile());

        // tags: wherever dark oak is listed, pale oak goes too
        try (Stream<Path> s = Files.walk(R.resolve(D + "tags"))) {
            for (Path p : (Iterable<Path>) s.filter(Files::isRegularFile)::iterator) {
                String t = Files.readString(p, StandardCharsets.UTF_8);
                if (!t.contains("dark_oak") || t.contains("pale_oak")) continue;
                StringBuilder sb = new StringBuilder();
                for (String line : t.split("\n", -1)) {
                    if (line.contains("dark_oak")) {
                        String l2 = line.replace("dark_oak", "pale_oak");
                        String with = line.stripTrailing().endsWith(",") ? line : line.stripTrailing() + ",";
                        sb.append(with).append("\n");
                        sb.append(line.stripTrailing().endsWith(",") ? l2 : l2).append("\n");
                    } else sb.append(line).append("\n");
                }
                String res = sb.toString();
                res = res.substring(0, res.length() - 1);
                Files.writeString(p, res, StandardCharsets.UTF_8);
                System.out.println("tag " + p.getFileName());
            }
        }

        // lang
        addLang("en_us", "Pale Oak Leaf Pile", "Pale Oak Bark");
        addLang("ru_ru", "Куча листьев бледного дуба", "Кора бледного дуба");
    }

    static int lum(int c) { return (((c >> 16) & 255) * 3 + ((c >> 8) & 255) * 6 + (c & 255)) / 10; }

    static void copyJson(Path p, String from, String to, boolean leaves) throws IOException {
        String t = Files.readString(p, StandardCharsets.UTF_8);
        if (leaves) t = t.replaceAll("immersive_weathering:block/(light|medium|heavy)_cherry_leaves", "minecraft:block/pale_oak_leaves");
        t = t.replace(from, to);
        Path target = p.resolveSibling(p.getFileName().toString().replace(from, to));
        Files.writeString(target, t, StandardCharsets.UTF_8);
        System.out.println(R.relativize(target));
    }

    static void addLang(String lang, String pile, String bark) throws IOException {
        Path p = R.resolve(A + "lang/" + lang + ".json");
        String t = Files.readString(p, StandardCharsets.UTF_8);
        if (t.contains("pale_oak_leaf_pile")) return;
        int i = t.indexOf('{');
        t = t.substring(0, i + 1) + "\n  \"block.immersive_weathering.pale_oak_leaf_pile\": \"" + pile + "\",\n  \"item.immersive_weathering.pale_oak_bark\": \"" + bark + "\"," + t.substring(i + 1);
        Files.writeString(p, t, StandardCharsets.UTF_8);
    }
}
