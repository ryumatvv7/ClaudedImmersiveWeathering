package com.ordana.immersive_weathering.platform.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class ConfigValue<T> implements Supplier<T> {

    enum Kind {BOOL, INT, DOUBLE, STRING, STRING_LIST}

    final List<String> path;
    final T def;
    final Kind kind;
    final double min, max;
    final String comment;
    private volatile T value;

    ConfigValue(List<String> path, T def, Kind kind, double min, double max, String comment) {
        this.path = List.copyOf(path);
        this.def = def;
        this.kind = kind;
        this.min = min;
        this.max = max;
        this.comment = comment;
        this.value = def;
    }

    @Override
    public T get() {
        return value;
    }

    /** Sets a number value (clamped to its range). @return false if this is not a number value. */
    @SuppressWarnings("unchecked")
    public boolean setNumber(double v) {
        if (kind == Kind.DOUBLE) value = (T) (Double) Math.max(min, Math.min(max, v));
        else if (kind == Kind.INT) value = (T) (Integer) (int) Math.max(min, Math.min(max, Math.round(v)));
        else return false;
        return true;
    }

    /** Sets a boolean value. @return false if this is not a boolean value. */
    @SuppressWarnings("unchecked")
    public boolean setBool(boolean v) {
        if (kind != Kind.BOOL) return false;
        value = (T) (Boolean) v;
        return true;
    }

    public boolean isNumber() {
        return kind == Kind.DOUBLE || kind == Kind.INT;
    }

    public java.util.List<String> path() {
        return path;
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    public String name() {
        return path.get(path.size() - 1);
    }

    JsonElement toJson() {
        return switch (kind) {
            case BOOL -> new JsonPrimitive((Boolean) value);
            case INT, DOUBLE -> new JsonPrimitive((Number) value);
            case STRING -> new JsonPrimitive((String) value);
            case STRING_LIST -> {
                JsonArray a = new JsonArray();
                for (Object o : (List<?>) value) a.add((String) o);
                yield a;
            }
        };
    }

    /** @return false if the json value was missing or invalid and the default was used. */
    @SuppressWarnings("unchecked")
    boolean fromJson(JsonElement e) {
        try {
            if (e == null || e.isJsonNull()) {
                value = def;
                return false;
            }
            Object v = switch (kind) {
                case BOOL -> e.getAsBoolean();
                case INT -> (int) Math.max(min, Math.min(max, e.getAsInt()));
                case DOUBLE -> Math.max(min, Math.min(max, e.getAsDouble()));
                case STRING -> e.getAsString();
                case STRING_LIST -> {
                    List<String> l = new ArrayList<>();
                    for (JsonElement x : e.getAsJsonArray()) l.add(x.getAsString());
                    yield List.copyOf(l);
                }
            };
            value = (T) v;
            return true;
        } catch (Exception ex) {
            value = def;
            return false;
        }
    }
}
