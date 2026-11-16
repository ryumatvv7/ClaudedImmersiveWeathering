package com.ordana.immersive_weathering.reg;

import com.ordana.immersive_weathering.ImmersiveWeathering;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Compostable;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Composting chances and burn times. Since 26.3 both are item components whose values come from
 * data driven int providers (data/immersive_weathering/context_int_provider).
 */
public class ModCompostable {

    private static ResourceKey<ContextIntProvider> key(String path) {
        return ResourceKey.create(Registries.CONTEXT_INT_PROVIDER, ImmersiveWeathering.res(path));
    }

    private static final ResourceKey<ContextIntProvider> VERY_LOW = key("compostable/very_low");

    private static ResourceKey<ContextIntProvider> fuel(int ticks) {
        return key("cooking/time_" + ticks);
    }

    public static void register() {
        DefaultItemComponentEvents.MODIFY.register(context -> {
            Map<Item, ResourceKey<ContextIntProvider>> compost = new LinkedHashMap<>();
            ModItems.BARK.values().forEach(b -> compost.put(b, ContextIntProviders.COMPOSTABLE_MEDIUM_HIGH));
            ModBlocks.LEAF_PILES.values().forEach(b -> compost.put(b.asItem(), VERY_LOW));
            compost.put(ModItems.AZALEA_FLOWER_PILE.get(), ContextIntProviders.COMPOSTABLE_LOW);
            compost.put(ModItems.MOSS_CLUMP.get(), ContextIntProviders.COMPOSTABLE_LOW_MEDIUM);
            compost.put(ModItems.AZALEA_FLOWERS.get(), ContextIntProviders.COMPOSTABLE_LOW_MEDIUM);
            compost.put(ModBlocks.MULCH_BLOCK.get().asItem(), ContextIntProviders.COMPOSTABLE_ALWAYS_ADD_ONE);
            compost.put(ModItems.FLOWER_CROWN.get(), ContextIntProviders.COMPOSTABLE_MEDIUM_HIGH);
            compost.put(ModBlocks.WEEDS.get().asItem(), ContextIntProviders.COMPOSTABLE_LOW);
            compost.put(ModBlocks.IVY.get().asItem(), ContextIntProviders.COMPOSTABLE_LOW);
            compost.put(ModBlocks.DUNE_GRASS.get().asItem(), ContextIntProviders.COMPOSTABLE_LOW);
            compost.forEach((item, chance) -> context.modify(item, b -> b.set(DataComponents.COMPOSTABLE, new Compostable(chance))));

            for (Map.Entry<Supplier<? extends ItemLike>, Integer> e : ModBlocks.FUELS.entrySet()) {
                Item item = e.getKey().get().asItem();
                ResourceKey<ContextIntProvider> time = fuel(e.getValue());
                context.modify(item, b -> b.set(DataComponents.COOKING_FUEL,
                        new CookingFuel(time, ContextFloatProviders.COOKING_DEFAULT_SPEED_MULTIPLIER)));
            }
        });
    }
}
