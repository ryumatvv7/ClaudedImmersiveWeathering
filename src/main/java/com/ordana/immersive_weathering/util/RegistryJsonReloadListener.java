package com.ordana.immersive_weathering.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

/**
 * Server data reload listener that reads raw json files from a data folder and hands them over
 * together with the registries (needed to resolve tags and holders inside codecs).
 * <p>
 * Written for this port: the Moonlight class the original used does not exist anymore.
 */
public abstract class RegistryJsonReloadListener extends SimpleJsonResourceReloadListener<JsonElement> {

    private final HolderLookup.Provider registries;

    protected RegistryJsonReloadListener(HolderLookup.Provider registries, String directory) {
        super(ExtraCodecs.JSON, FileToIdConverter.json(directory));
        this.registries = registries;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> jsons, ResourceManager manager, ProfilerFiller profiler) {
        this.parse(jsons, this.registries);
    }

    public abstract void parse(Map<Identifier, JsonElement> jsons, HolderLookup.Provider registries);

    /**
     * Dispatch codecs need map codecs now. Record codecs are map codecs under the hood.
     */
    public static <T> MapCodec<T> asMapCodec(Codec<T> codec) {
        if (codec instanceof MapCodec.MapCodecCodec<T> m) return m.codec();
        return MapCodec.assumeMapUnsafe(codec);
    }
}
