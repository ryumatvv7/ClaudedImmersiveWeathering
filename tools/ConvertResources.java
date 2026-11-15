import com.google.gson.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * One-off converter of the original 1.20.1 resources (upstream/common/src/main/resources)
 * to the 26.1.2 formats. Run from the project root:
 * java -cp <gson.jar> tools/ConvertResources.java
 * <p>
 * It never touches worldgen, block_growths and fluid_generators: those are ported by hand / later stages.
 */
public class ConvertResources {

    static final Path SRC = Path.of("upstream/common/src/main/resources");
    static final Path DST = Path.of("src/main/resources");
    static final Path VANILLA = Path.of("ref/mcres");
    static final String MOD = "immersive_weathering";
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    static final Set<String> vanillaItems = new HashSet<>(), vanillaBlocks = new HashSet<>();
    static final Set<String> modItems = new HashSet<>(), modBlocks = new HashSet<>();
    static final Set<String> problems = new TreeSet<>();

    //vanilla ids renamed since 1.20.1
    static final Map<String, String> RENAMES = Map.of(
            "minecraft:grass", "minecraft:short_grass",
            "minecraft:scute", "minecraft:turtle_scute",
            "minecraft:grass_path", "minecraft:dirt_path"
    );

    public static void main(String[] args) throws IOException {
        list(VANILLA.resolve("assets/minecraft/items"), vanillaItems, "minecraft:");
        list(VANILLA.resolve("assets/minecraft/blockstates"), vanillaBlocks, "minecraft:");
        list(SRC.resolve("assets/" + MOD + "/models/item"), modItems, MOD + ":");
        list(SRC.resolve("assets/" + MOD + "/blockstates"), modBlocks, MOD + ":");
        //block items without a model of their own name still exist as items
        modItems.addAll(modBlocks);

        assets();
        recipes();
        jsonTree("data/" + MOD + "/loot_tables", "data/" + MOD + "/loot_table");
        jsonTree("data/" + MOD + "/advancements", "data/" + MOD + "/advancement");
        tags();

        System.out.println("---- problems (" + problems.size() + ") ----");
        problems.forEach(System.out::println);
    }

    static void list(Path dir, Set<String> out, String prefix) throws IOException {
        try (Stream<Path> s = Files.list(dir)) {
            s.filter(p -> p.toString().endsWith(".json"))
                    .forEach(p -> out.add(prefix + p.getFileName().toString().replace(".json", "")));
        }
    }

    // ------------------------------------------------------------------ assets

    static void assets() throws IOException {
        Path a = SRC.resolve("assets/" + MOD);
        copyTree(a.resolve("blockstates"), DST.resolve("assets/" + MOD + "/blockstates"));
        copyTree(a.resolve("models"), DST.resolve("assets/" + MOD + "/models"));
        copyTree(a.resolve("textures"), DST.resolve("assets/" + MOD + "/textures"));
        copyTree(a.resolve("particles"), DST.resolve("assets/" + MOD + "/particles"));
        copyTree(a.resolve("lang"), DST.resolve("assets/" + MOD + "/lang"));
        copyTree(a.resolve("sounds"), DST.resolve("assets/" + MOD + "/sounds"));
        Files.copy(a.resolve("sounds.json"), DST.resolve("assets/" + MOD + "/sounds.json"), StandardCopyOption.REPLACE_EXISTING);
        copyTree(SRC.resolve("assets/minecraft/models"), DST.resolve("assets/minecraft/models"));
        copyTree(SRC.resolve("assets/minecraft/textures/item"), DST.resolve("assets/minecraft/textures/item"));

        //armor textures -> equipment assets
        Path armor = SRC.resolve("assets/minecraft/textures/models/armor");
        Path eqTex = DST.resolve("assets/" + MOD + "/textures/entity/equipment/humanoid");
        Path eq = DST.resolve("assets/" + MOD + "/equipment");
        Files.createDirectories(eqTex);
        Files.createDirectories(eq);
        try (Stream<Path> s = Files.list(armor)) {
            for (Path p : (Iterable<Path>) s::iterator) {
                String n = p.getFileName().toString();
                if (!n.endsWith(".png")) continue;
                String name = n.equals("flower_layer_1.png") ? "flower" : "flower_" + n.replace(".png", "");
                Files.copy(p, eqTex.resolve(name + ".png"), StandardCopyOption.REPLACE_EXISTING);
                JsonObject layer = new JsonObject();
                layer.addProperty("texture", MOD + ":" + name);
                JsonArray arr = new JsonArray();
                arr.add(layer);
                JsonObject layers = new JsonObject();
                layers.add("humanoid", arr);
                JsonObject root = new JsonObject();
                root.add("layers", layers);
                write(eq.resolve(name + ".json"), root);
            }
        }

        //item model definitions (assets/<ns>/items/<id>.json), required since 1.21.4
        Path itemsDir = DST.resolve("assets/" + MOD + "/items");
        Files.createDirectories(itemsDir);
        try (Stream<Path> s = Files.list(a.resolve("models/item"))) {
            for (Path p : (Iterable<Path>) s::iterator) {
                String id = p.getFileName().toString().replace(".json", "");
                if (id.startsWith("flower_crown")) continue;
                write(itemsDir.resolve(id + ".json"), itemDef(modelRef(id)));
            }
        }
        flowerCrown(a, itemsDir);
    }

    static JsonObject modelRef(String id) {
        JsonObject m = new JsonObject();
        m.addProperty("type", "minecraft:model");
        m.addProperty("model", MOD + ":item/" + id);
        return m;
    }

    static JsonObject itemDef(JsonObject model) {
        JsonObject o = new JsonObject();
        o.add("model", model);
        return o;
    }

    //old "immersive_weathering:supporter" float predicate -> select on the custom name component
    static void flowerCrown(Path assets, Path itemsDir) throws IOException {
        //same names as FlowerCrownItem.SUPPORTERS_LIST (name -> texture variant)
        Map<String, String> names = new LinkedHashMap<>();
        String[][] pairs = {
                {"ordana", "bee"}, {"bee", "bee"}, {"mehvahdjukaar", "jar"}, {"jar", "jar"}, {"bob", "bob"}, {"bobisnotaperson", "bob"},
                {"asexual", "ace"}, {"ace", "ace"}, {"aromantic", "aro"}, {"aro", "aro"}, {"bisexual", "bi"}, {"bi", "bi"},
                {"non-binary", "enby"}, {"nb", "enby"}, {"enby", "enby"}, {"gay", "gay"}, {"mlm", "gay"},
                {"lesbian", "lesbian"}, {"wlw", "lesbian"}, {"rainbow", "rainbow"}, {"pride", "rainbow"},
                {"trans", "trans"}, {"transgender", "trans"}, {"pan", "pan"}, {"pansexual", "pan"},
                {"intersex", "intersex"}, {"genderqueer", "genderqueer"}, {"genderfluid", "fluid"}, {"fluid", "fluid"},
                {"flax", "flax"}, {"neko", "nekomaster"}, {"nekomaster", "nekomaster"}, {"akashii", "akashii"}};
        for (String[] p : pairs) names.put(p[0], p[1]);

        JsonArray cases = new JsonArray();
        Map<String, JsonArray> byVariant = new LinkedHashMap<>();
        for (var e : names.entrySet()) {
            JsonArray when = byVariant.computeIfAbsent(e.getValue(), k -> new JsonArray());
            String n = e.getKey();
            //the component must match exactly, so the common capitalizations are listed
            for (String v : new LinkedHashSet<>(List.of(n, Character.toUpperCase(n.charAt(0)) + n.substring(1), n.toUpperCase(Locale.ROOT)))) {
                when.add(v);
            }
        }
        for (var e : byVariant.entrySet()) {
            if (!Files.exists(assets.resolve("models/item/flower_crown_" + e.getKey() + ".json"))) {
                problems.add("flower crown variant without item model: " + e.getKey());
                continue;
            }
            JsonObject c = new JsonObject();
            c.add("when", e.getValue());
            c.add("model", modelRef("flower_crown_" + e.getKey()));
            cases.add(c);
        }
        JsonObject select = new JsonObject();
        select.addProperty("type", "minecraft:select");
        select.addProperty("property", "minecraft:component");
        select.addProperty("component", "minecraft:custom_name");
        select.add("cases", cases);
        select.add("fallback", modelRef("flower_crown"));
        write(itemsDir.resolve("flower_crown.json"), itemDef(select));

        //the old model had "overrides", which no longer exist
        Path model = DST.resolve("assets/" + MOD + "/models/item/flower_crown.json");
        JsonObject m = read(model).getAsJsonObject();
        m.remove("overrides");
        write(model, m);
    }

    // ------------------------------------------------------------------ recipes

    static void recipes() throws IOException {
        Path in = SRC.resolve("data/" + MOD + "/recipes");
        Path out = DST.resolve("data/" + MOD + "/recipe");
        Files.createDirectories(out);
        try (Stream<Path> s = Files.list(in)) {
            for (Path p : (Iterable<Path>) s::iterator) {
                JsonObject r = read(p).getAsJsonObject();
                String ctx = "recipe " + p.getFileName();
                if (r.has("ingredient")) r.add("ingredient", ingredient(r.get("ingredient"), ctx));
                if (r.has("ingredients")) {
                    JsonArray arr = new JsonArray();
                    for (JsonElement e : r.getAsJsonArray("ingredients")) arr.add(ingredient(e, ctx));
                    r.add("ingredients", arr);
                }
                if (r.has("key")) {
                    JsonObject key = new JsonObject();
                    for (var e : r.getAsJsonObject("key").entrySet()) key.add(e.getKey(), ingredient(e.getValue(), ctx));
                    r.add("key", key);
                }
                JsonObject result = new JsonObject();
                JsonElement old = r.get("result");
                if (old.isJsonPrimitive()) {
                    result.addProperty("id", item(old.getAsString(), ctx));
                } else {
                    JsonObject o = old.getAsJsonObject();
                    result.addProperty("id", item(o.get("item").getAsString(), ctx));
                    if (o.has("count") && o.get("count").getAsInt() != 1) result.addProperty("count", o.get("count").getAsInt());
                }
                if (r.has("count")) {
                    int c = r.remove("count").getAsInt();
                    if (c != 1) result.addProperty("count", c);
                }
                r.add("result", result);
                write(out.resolve(p.getFileName()), r);
            }
        }
    }

    static JsonElement ingredient(JsonElement e, String ctx) {
        if (e.isJsonArray()) {
            JsonArray arr = new JsonArray();
            for (JsonElement c : e.getAsJsonArray()) {
                JsonElement i = ingredient(c, ctx);
                if (i.isJsonPrimitive() && i.getAsString().startsWith("#")) problems.add(ctx + ": tag inside ingredient list");
                arr.add(i);
            }
            return arr;
        }
        if (e.isJsonPrimitive()) return e;
        JsonObject o = e.getAsJsonObject();
        if (o.has("item")) return new JsonPrimitive(item(o.get("item").getAsString(), ctx));
        if (o.has("tag")) return new JsonPrimitive("#" + o.get("tag").getAsString());
        problems.add(ctx + ": unknown ingredient " + o);
        return e;
    }

    static String item(String id, String ctx) {
        id = RENAMES.getOrDefault(id, id);
        if (id.startsWith("minecraft:") && !vanillaItems.contains(id)) problems.add(ctx + ": unknown vanilla item " + id);
        if (id.startsWith(MOD + ":") && !modItems.contains(id)) problems.add(ctx + ": unknown mod item " + id);
        return id;
    }

    // ------------------------------------------------------------------ loot tables / advancements

    static void jsonTree(String from, String to) throws IOException {
        Path in = SRC.resolve(from);
        Path out = DST.resolve(to);
        try (Stream<Path> s = Files.walk(in)) {
            for (Path p : (Iterable<Path>) s::iterator) {
                if (!p.toString().endsWith(".json")) continue;
                Path rel = in.relativize(p);
                JsonElement json = convert(read(p), "", from + "/" + rel.toString().replace('\\', '/'));
                Path target = out.resolve(rel);
                Files.createDirectories(target.getParent());
                write(target, json);
            }
        }
    }

    static JsonElement convert(JsonElement e, String parentKey, String ctx) {
        if (e.isJsonArray()) {
            JsonArray arr = new JsonArray();
            for (JsonElement c : e.getAsJsonArray()) arr.add(convert(c, parentKey, ctx));
            return arr;
        }
        if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isString()) {
            String s = e.getAsString();
            if (RENAMES.containsKey(s)) return new JsonPrimitive(RENAMES.get(s));
            return e;
        }
        if (!e.isJsonObject()) return e;
        JsonObject o = e.getAsJsonObject();
        JsonObject n = new JsonObject();

        boolean matchTool = false;
        for (var en : o.entrySet()) {
            String k = en.getKey();
            JsonElement v = en.getValue();

            //advancement icon
            if (k.equals("icon") && v.isJsonObject() && v.getAsJsonObject().has("item")) {
                JsonObject icon = new JsonObject();
                icon.addProperty("id", item(v.getAsJsonObject().get("item").getAsString(), ctx));
                n.add(k, icon);
                continue;
            }
            //old enchantment list of an item predicate
            if (k.equals("enchantments") && v.isJsonArray() && v.getAsJsonArray().size() > 0
                    && v.getAsJsonArray().get(0).isJsonObject() && v.getAsJsonArray().get(0).getAsJsonObject().has("enchantment")) {
                JsonArray list = new JsonArray();
                for (JsonElement x : v.getAsJsonArray()) {
                    JsonObject old = x.getAsJsonObject();
                    JsonObject ne = new JsonObject();
                    ne.add("enchantments", old.get("enchantment"));
                    if (old.has("levels")) ne.add("levels", old.get("levels"));
                    list.add(ne);
                }
                JsonObject preds = n.has("predicates") ? n.getAsJsonObject("predicates") : new JsonObject();
                preds.add("minecraft:enchantments", list);
                n.add("predicates", preds);
                continue;
            }
            //tag shorthand of predicates
            if (k.equals("tag") && v.isJsonPrimitive()) {
                if (parentKey.equals("block")) { n.addProperty("blocks", "#" + v.getAsString()); continue; }
                if (parentKey.equals("fluid")) { n.addProperty("fluids", "#" + v.getAsString()); continue; }
                if (parentKey.equals("predicate") || parentKey.equals("items") || parentKey.equals("item") || parentKey.equals("mainhand")) {
                    n.addProperty("items", "#" + v.getAsString());
                    continue;
                }
                problems.add(ctx + ": unhandled 'tag' under '" + parentKey + "'");
            }
            if (k.equals("function") && v.isJsonPrimitive() && v.getAsString().equals("minecraft:looting_enchant")) {
                n.addProperty("function", "minecraft:enchanted_count_increase");
                n.addProperty("enchantment", "minecraft:looting");
                continue;
            }
            if (k.equals("condition") && v.isJsonPrimitive() && v.getAsString().equals("minecraft:match_tool")) matchTool = true;
            //loot entry item names
            if (k.equals("name") && v.isJsonPrimitive() && o.has("type") && o.get("type").getAsString().equals("minecraft:item")) {
                n.addProperty(k, item(v.getAsString(), ctx));
                continue;
            }
            n.add(k, convert(v, k, ctx));
        }
        return n;
    }

    // ------------------------------------------------------------------ tags

    static void tags() throws IOException {
        Map<String, String> folders = Map.of("blocks", "block", "items", "item", "entity_types", "entity_type", "worldgen", "worldgen");
        for (String ns : List.of(MOD, "minecraft")) {
            Path in = SRC.resolve("data/" + ns + "/tags");
            try (Stream<Path> s = Files.walk(in)) {
                for (Path p : (Iterable<Path>) s::iterator) {
                    if (!p.toString().endsWith(".json")) continue;
                    Path rel = in.relativize(p);
                    String type = rel.getName(0).toString();
                    Path newRel = Path.of(folders.get(type)).resolve(rel.subpath(1, rel.getNameCount()));
                    JsonObject tag = read(p).getAsJsonObject();
                    JsonArray values = new JsonArray();
                    String ctx = "tag " + ns + ":" + rel.toString().replace('\\', '/');
                    for (JsonElement v : tag.getAsJsonArray("values")) {
                        JsonElement c = tagEntry(v, type, ctx);
                        if (c != null) values.add(c);
                    }
                    tag.add("values", values);
                    Path target = DST.resolve("data/" + ns + "/tags").resolve(newRel);
                    Files.createDirectories(target.getParent());
                    write(target, tag);
                }
            }
        }
    }

    static JsonElement tagEntry(JsonElement v, String type, String ctx) {
        boolean required = true;
        String id;
        if (v.isJsonObject()) {
            id = v.getAsJsonObject().get("id").getAsString();
            if (v.getAsJsonObject().has("required")) required = v.getAsJsonObject().get("required").getAsBoolean();
        } else id = v.getAsString();

        boolean isTag = id.startsWith("#");
        String raw = isTag ? id.substring(1) : id;
        raw = RENAMES.getOrDefault(raw, raw);
        String ns = raw.contains(":") ? raw.substring(0, raw.indexOf(':')) : "minecraft";
        if (!raw.contains(":")) raw = "minecraft:" + raw;

        if (!ns.equals("minecraft") && !ns.equals(MOD)) {
            required = false; //other mods (and the "c"/"forge" common tags) may be missing
            if (ns.equals("forge")) { raw = "c:" + raw.substring(6); }
        } else if (!isTag && (type.equals("blocks") || type.equals("items"))) {
            Set<String> known = ns.equals("minecraft") ? (type.equals("blocks") ? vanillaBlocks : vanillaItems) : (type.equals("blocks") ? modBlocks : modItems);
            if (!known.contains(raw)) {
                problems.add(ctx + ": unknown " + type + " id " + raw + (ns.equals("minecraft") ? " (dropped)" : " (kept as optional)"));
                if (ns.equals("minecraft")) return null;
                required = false;
            }
        } else if (isTag && ns.equals("minecraft")) {
            String folder = Map.of("blocks", "block", "items", "item", "entity_types", "entity_type", "worldgen", "worldgen").get(type);
            String path = raw.substring(raw.indexOf(':') + 1);
            Path f = VANILLA.resolve("data/minecraft/tags/" + folder + (type.equals("worldgen") ? "" : "") + "/" + path + ".json");
            if (!type.equals("worldgen") && !Files.exists(f)) {
                problems.add(ctx + ": unknown vanilla tag #" + raw + " (kept as optional)");
                required = false;
            }
        }
        String out = (isTag ? "#" : "") + raw;
        if (required) return new JsonPrimitive(out);
        JsonObject o = new JsonObject();
        o.addProperty("id", out);
        o.addProperty("required", false);
        return o;
    }

    // ------------------------------------------------------------------ io

    static JsonElement read(Path p) throws IOException {
        return JsonParser.parseString(Files.readString(p, StandardCharsets.UTF_8));
    }

    static void write(Path p, JsonElement e) throws IOException {
        Files.createDirectories(p.getParent());
        Files.writeString(p, GSON.toJson(e) + "\n", StandardCharsets.UTF_8);
    }

    static void copyTree(Path from, Path to) throws IOException {
        try (Stream<Path> s = Files.walk(from)) {
            for (Path p : (Iterable<Path>) s::iterator) {
                Path t = to.resolve(from.relativize(p));
                if (Files.isDirectory(p)) Files.createDirectories(t);
                else if (!p.toString().endsWith(".psd")) Files.copy(p, t, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
