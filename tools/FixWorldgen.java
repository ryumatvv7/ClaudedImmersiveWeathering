import com.google.gson.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.stream.Stream;

/**
 * One-off: updates the mod's worldgen json (copied from 1.20.1) to the 26.1.2 formats.
 * java -cp <gson.jar> tools/FixWorldgen.java
 */
public class FixWorldgen {
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static void main(String[] args) throws IOException {
        Path dir = Path.of("src/main/resources/data/immersive_weathering/worldgen");
        try (Stream<Path> s = Files.walk(dir)) {
            for (Path p : (Iterable<Path>) s.filter(x -> x.toString().endsWith(".json"))::iterator) {
                JsonElement in = JsonParser.parseString(Files.readString(p, StandardCharsets.UTF_8));
                JsonElement out = fix(in);
                Files.writeString(p, GSON.toJson(out) + "\n", StandardCharsets.UTF_8);
            }
        }
    }

    static JsonElement fix(JsonElement e) {
        if (e.isJsonArray()) {
            JsonArray a = new JsonArray();
            for (JsonElement c : e.getAsJsonArray()) a.add(fix(c));
            return a;
        }
        if (!e.isJsonObject()) return e;
        JsonObject o = e.getAsJsonObject();
        JsonObject n = new JsonObject();
        // int/float providers: {"type": X, "value": {...}} -> {"type": X, ...}
        if (o.has("type") && o.has("value") && o.get("value").isJsonObject() && o.size() == 2) {
            n.add("type", o.get("type"));
            for (var en : o.getAsJsonObject("value").entrySet()) n.add(en.getKey(), fix(en.getValue()));
            return n;
        }
        // old rule based state provider without type: {"fallback": P, "rules": [...]}
        if (o.has("fallback") && o.has("rules") && !o.has("type")) {
            n.addProperty("type", "minecraft:rule_based_state_provider");
        }
        for (var en : o.entrySet()) n.add(en.getKey(), fix(en.getValue()));
        return n;
    }
}
