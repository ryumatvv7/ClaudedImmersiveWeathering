package com.ordana.immersive_weathering.platform;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;

import java.util.function.Supplier;

/**
 * Stairs whose base block is given lazily (replaces Moonlight's ModStairBlock).
 */
public class ModStairBlock extends StairBlock {

    public ModStairBlock(Supplier<? extends Block> base, Properties properties) {
        super(base.get().defaultBlockState(), properties);
    }
}
