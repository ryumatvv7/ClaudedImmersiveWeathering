package com.ordana.immersive_weathering.util;

import com.ordana.immersive_weathering.reg.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Port addition: which vanilla wooden blocks rot / grow moss, and what they rot into.
 * Overworld woods only (crimson / warped don't rot); planks, logs, wood, stairs, slabs and fences.
 */
public final class WoodWeathering {

    private static final Set<String> WOODS = Set.of("oak", "spruce", "birch", "jungle", "acacia", "dark_oak",
            "mangrove", "cherry", "pale_oak", "poplar", "bamboo");
    private static final String[] SHAPES = {"_planks", "_log", "_wood", "_stairs", "_slab", "_fence",
            "_mosaic", "_mosaic_stairs", "_mosaic_slab", "_block"};

    private static final Map<Block, Boolean> CACHE = new IdentityHashMap<>();

    /** Vanilla wooden block that can rot (also used to make these blocks randomly ticking). */
    public static boolean isWeatherableWood(Block block) {
        synchronized (CACHE) {
            return CACHE.computeIfAbsent(block, WoodWeathering::compute);
        }
    }

    private static boolean compute(Block block) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        if (id == null || !id.getNamespace().equals("minecraft")) return false;
        String p = id.getPath();
        if (p.startsWith("stripped_")) p = p.substring("stripped_".length());
        for (String s : SHAPES) {
            if (p.endsWith(s)) {
                String wood = p.substring(0, p.length() - s.length());
                if (wood.equals("bamboo") && !(s.equals("_block") || s.contains("planks") || s.contains("mosaic")
                        || s.equals("_stairs") || s.equals("_slab") || s.equals("_fence"))) return false;
                if (!wood.equals("bamboo") && (s.equals("_block") || s.contains("mosaic"))) return false;
                return WOODS.contains(wood);
            }
        }
        return false;
    }

    /** The rotten block with the same shape (stairs keep facing, slabs keep type...). */
    @Nullable
    public static BlockState getRotten(BlockState state) {
        Block b = state.getBlock();
        if (!isWeatherableWood(b)) return null;
        Block target;
        if (b instanceof StairBlock) target = ModBlocks.ROTTEN_STAIRS.get();
        else if (b instanceof SlabBlock) target = ModBlocks.ROTTEN_SLAB.get();
        else if (b instanceof FenceBlock) target = ModBlocks.ROTTEN_FENCE.get();
        else if (b instanceof RotatedPillarBlock) target = ModBlocks.ROTTEN_LOG.get();
        else target = ModBlocks.ROTTEN_PLANKS.get();
        return target.withPropertiesOf(state);
    }

    public static boolean isRotten(Block b) {
        return b == ModBlocks.ROTTEN_PLANKS.get() || b == ModBlocks.ROTTEN_LOG.get() || b == ModBlocks.ROTTEN_STAIRS.get()
                || b == ModBlocks.ROTTEN_SLAB.get() || b == ModBlocks.ROTTEN_FENCE.get();
    }

    private WoodWeathering() {
    }
}
