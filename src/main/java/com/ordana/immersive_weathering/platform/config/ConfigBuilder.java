package com.ordana.immersive_weathering.platform.config;

import net.minecraft.resources.Identifier;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Supplier;

/**
 * Minimal config builder with the same shape as the Moonlight one the original code was written against.
 * Values are stored in {@code config/<namespace>-<name>.json}.
 */
public class ConfigBuilder {

    private final Identifier id;
    private final ConfigType type;
    private final Deque<String> path = new ArrayDeque<>();
    private final List<ConfigValue<?>> values = new ArrayList<>();
    private final List<String[]> categoryComments = new ArrayList<>();
    private String pendingComment;

    private ConfigBuilder(Identifier id, ConfigType type) {
        this.id = id;
        this.type = type;
    }

    public static ConfigBuilder create(Identifier id, ConfigType type) {
        return new ConfigBuilder(id, type);
    }

    public ConfigBuilder comment(String comment) {
        this.pendingComment = comment;
        return this;
    }

    public ConfigBuilder push(String category) {
        path.addLast(category);
        if (pendingComment != null) {
            categoryComments.add(new String[]{String.join(".", path), pendingComment});
            pendingComment = null;
        }
        return this;
    }

    public ConfigBuilder pop() {
        path.removeLast();
        return this;
    }

    private <T> ConfigValue<T> add(String name, T def, ConfigValue.Kind kind, double min, double max) {
        List<String> p = new ArrayList<>(path);
        p.add(name);
        ConfigValue<T> v = new ConfigValue<>(p, def, kind, min, max, pendingComment);
        pendingComment = null;
        values.add(v);
        return v;
    }

    public Supplier<Boolean> define(String name, boolean def) {
        return add(name, def, ConfigValue.Kind.BOOL, 0, 0);
    }

    public Supplier<Integer> define(String name, int def, int min, int max) {
        return add(name, def, ConfigValue.Kind.INT, min, max);
    }

    public Supplier<Double> define(String name, double def, double min, double max) {
        return add(name, def, ConfigValue.Kind.DOUBLE, min, max);
    }

    public Supplier<String> define(String name, String def) {
        return add(name, def, ConfigValue.Kind.STRING, 0, 0);
    }

    public Supplier<List<String>> define(String name, List<String> def) {
        return add(name, List.copyOf(def), ConfigValue.Kind.STRING_LIST, 0, 0);
    }

    public ModConfigHolder build() {
        return new ModConfigHolder(id, type, List.copyOf(values), List.copyOf(categoryComments));
    }
}
