package com.ordana.immersive_weathering.platform.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ordana.immersive_weathering.ImmersiveWeathering;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A built config: loads / saves its json file and can temporarily take values sent by a server.
 */
public class ModConfigHolder {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<ModConfigHolder> SYNCED = new ArrayList<>();

    private final Identifier id;
    private final ConfigType type;
    private final List<ConfigValue<?>> values;
    private final List<String[]> categoryComments;

    ModConfigHolder(Identifier id, ConfigType type, List<ConfigValue<?>> values, List<String[]> categoryComments) {
        this.id = id;
        this.type = type;
        this.values = values;
        this.categoryComments = categoryComments;
        if (type == ConfigType.COMMON_SYNCED) SYNCED.add(this);
    }

    public List<ConfigValue<?>> getValues() {
        return values;
    }

    public Identifier getId() {
        return id;
    }

    public ConfigType getType() {
        return type;
    }

    public Path getPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(id.getNamespace() + "-" + id.getPath() + ".json");
    }

    /** Reads the file (creating / completing it with defaults when needed). */
    public void forceLoad() {
        Path path = getPath();
        JsonObject root = new JsonObject();
        boolean dirty = !Files.exists(path);
        if (!dirty) {
            try {
                root = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
            } catch (Exception e) {
                ImmersiveWeathering.LOGGER.error("Failed to read config {}, using defaults", path, e);
                root = new JsonObject();
                dirty = true;
            }
        }
        dirty |= !apply(root);
        if (dirty) save();
    }

    /** @return true if every value was present and valid. */
    private boolean apply(JsonObject root) {
        boolean ok = true;
        for (ConfigValue<?> v : values) {
            JsonElement e = root;
            for (String p : v.path) {
                e = e != null && e.isJsonObject() ? e.getAsJsonObject().get(p) : null;
            }
            ok &= v.fromJson(e);
        }
        return ok;
    }

    public JsonObject toJson(boolean withComments) {
        JsonObject root = new JsonObject();
        if (withComments) {
            for (String[] c : categoryComments) objectAt(root, List.of(c[0].split("\\."))).addProperty("_comment", c[1]);
        }
        for (ConfigValue<?> v : values) {
            JsonObject parent = objectAt(root, v.path.subList(0, v.path.size() - 1));
            if (withComments && v.comment != null) parent.addProperty("_comment_" + v.name(), v.comment);
            parent.add(v.name(), v.toJson());
        }
        return root;
    }

    private static JsonObject objectAt(JsonObject root, List<String> path) {
        JsonObject o = root;
        for (String p : path) {
            JsonElement c = o.get(p);
            if (c == null || !c.isJsonObject()) {
                c = new JsonObject();
                o.add(p, c);
            }
            o = c.getAsJsonObject();
        }
        return o;
    }

    public void save() {
        try {
            Path path = getPath();
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(toJson(true)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            ImmersiveWeathering.LOGGER.error("Failed to save config {}", id, e);
        }
    }

    // --- sync ---

    public String toSyncString() {
        return GSON.toJson(toJson(false));
    }

    /** Client side: use the values received from the server (not written to disk). */
    public void acceptSynced(String json) {
        try {
            apply(JsonParser.parseString(json).getAsJsonObject());
            ImmersiveWeathering.LOGGER.info("Received config {} from server", id);
        } catch (Exception e) {
            ImmersiveWeathering.LOGGER.error("Invalid synced config {}", id, e);
        }
    }

    public static List<ModConfigHolder> syncedConfigs() {
        return SYNCED;
    }
}
