package com.ordana.immersive_weathering.platform;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import com.mojang.serialization.MapCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Deferred registration helper (replaces Moonlight's RegHelper).
 * Every entry is queued when its holder class loads and registered by {@link #flush()} in a fixed
 * registry order, or earlier on its first {@code get()}.
 */
public final class Reg {

    private static final List<Entry<?>> ENTRIES = new ArrayList<>();

    public static final class Entry<T> implements Supplier<T> {
        private final int order;
        private final Supplier<T> registrar;
        private T value;

        private Entry(int order, Supplier<T> registrar) {
            this.order = order;
            this.registrar = registrar;
        }

        @Override
        public synchronized T get() {
            if (value == null) value = registrar.get();
            return value;
        }
    }

    private static <T> Entry<T> add(int order, Supplier<T> registrar) {
        Entry<T> e = new Entry<>(order, registrar);
        ENTRIES.add(e);
        return e;
    }

    /** Registers everything queued so far, in registry order. */
    public static void flush() {
        List<Entry<?>> sorted = new ArrayList<>(ENTRIES);
        sorted.sort((a, b) -> Integer.compare(a.order, b.order));
        sorted.forEach(Entry::get);
    }

    public static <T extends Block> Entry<T> block(Identifier id, Function<BlockBehaviour.Properties, T> factory,
                                                   Supplier<BlockBehaviour.Properties> properties) {
        return add(10, () -> Registry.register(BuiltInRegistries.BLOCK, id,
                factory.apply(properties.get().setId(ResourceKey.create(Registries.BLOCK, id)))));
    }

    public static <T extends Item> Entry<T> item(Identifier id, Function<Item.Properties, T> factory, Item.Properties properties) {
        return add(20, () -> Registry.register(BuiltInRegistries.ITEM, id,
                factory.apply(properties.setId(ResourceKey.create(Registries.ITEM, id)))));
    }

    public static Entry<BlockItem> blockItem(Identifier id, Supplier<? extends Block> block, Item.Properties properties) {
        return item(id, p -> new BlockItem(block.get(), p.useBlockDescriptionPrefix()), properties);
    }

    public static Entry<SoundEvent> sound(Identifier id) {
        return add(0, () -> Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id)));
    }

    public static Entry<SimpleParticleType> particle(Identifier id) {
        return add(0, () -> Registry.register(BuiltInRegistries.PARTICLE_TYPE, id, FabricParticleTypes.simple()));
    }

    public static <T extends Entity> Entry<EntityType<T>> entity(Identifier id, EntityType.Builder<T> builder) {
        return add(30, () -> Registry.register(BuiltInRegistries.ENTITY_TYPE, id,
                builder.build(ResourceKey.create(Registries.ENTITY_TYPE, id))));
    }

    public static <T extends BlockEntity> Entry<BlockEntityType<T>> blockEntity(
            Identifier id, BlockEntityType.BlockEntitySupplier<T> factory, Supplier<? extends Block> block) {
        return add(30, () -> Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id,
                new BlockEntityType<>(factory, Set.of(block.get()))));
    }

    public static <F extends Feature> Entry<MapCodec<F>> featureType(Identifier id, Supplier<MapCodec<F>> codec) {
        return add(30, () -> Registry.register(BuiltInRegistries.FEATURE_TYPE, id, codec.get()));
    }

    public static Entry<CreativeModeTab> creativeTab(Identifier id, Supplier<CreativeModeTab> tab) {
        return add(40, () -> Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id, tab.get()));
    }

    private Reg() {
    }
}
