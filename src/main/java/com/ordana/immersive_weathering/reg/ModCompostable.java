package com.ordana.immersive_weathering.reg;

import net.fabricmc.fabric.api.registry.CompostableRegistry;

public class ModCompostable {
    public static void register() {
        ModItems.BARK.values().forEach(b -> CompostableRegistry.INSTANCE.add(b, 0.8f));
        ModBlocks.LEAF_PILES.values().forEach(b -> CompostableRegistry.INSTANCE.add(b.asItem(), 0.1f));
        CompostableRegistry.INSTANCE.add(ModItems.AZALEA_FLOWER_PILE.get(), 0.3f);
        CompostableRegistry.INSTANCE.add(ModItems.MOSS_CLUMP.get(), 0.5f);
        CompostableRegistry.INSTANCE.add(ModItems.AZALEA_FLOWERS.get(), 0.5f);
        CompostableRegistry.INSTANCE.add(ModBlocks.MULCH_BLOCK.get().asItem(), 1f);
        CompostableRegistry.INSTANCE.add(ModItems.FLOWER_CROWN.get(), 0.8f);
        CompostableRegistry.INSTANCE.add(ModBlocks.WEEDS.get().asItem(), 0.3f);
        CompostableRegistry.INSTANCE.add(ModBlocks.IVY.get().asItem(), 0.3f);
        CompostableRegistry.INSTANCE.add(ModBlocks.DUNE_GRASS.get().asItem(), 0.3f);
    }
}
