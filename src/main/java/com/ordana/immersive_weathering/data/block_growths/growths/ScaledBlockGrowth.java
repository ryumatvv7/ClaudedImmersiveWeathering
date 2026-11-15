package com.ordana.immersive_weathering.data.block_growths.growths;

import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.data.block_growths.TickSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Port addition: applies the "speed" config multipliers to any block growth (built in or from a datapack).
 * Multiplier below 1 skips some attempts, above 1 runs extra attempts per tick.
 */
public record ScaledBlockGrowth(IBlockGrowth inner, Supplier<Double> category) implements IBlockGrowth {

    public static IBlockGrowth wrap(String name, IBlockGrowth growth) {
        return new ScaledBlockGrowth(growth, categoryOf(name));
    }

    private static Supplier<Double> categoryOf(String n) {
        if (n.startsWith("soot") || n.startsWith("fire") || n.startsWith("magma")) return CommonConfigs.GROWTH_FIRE;
        if (n.startsWith("lightning")) return CommonConfigs.GROWTH_LIGHTNING;
        if (n.startsWith("frost") || n.contains("ice") || n.contains("icicle") || n.contains("snow")) return CommonConfigs.GROWTH_FROST;
        if (n.startsWith("sand") || n.contains("sandstorm")) return CommonConfigs.GROWTH_SAND;
        if (n.startsWith("coral")) return CommonConfigs.GROWTH_CORAL;
        if (n.startsWith("leaf")) return CommonConfigs.GROWTH_LEAVES;
        if (n.startsWith("weeds")) return CommonConfigs.GROWTH_WEEDS;
        if (n.contains("nylium") || n.startsWith("wart") || n.endsWith("nether")) return CommonConfigs.GROWTH_NETHER;
        if (n.startsWith("grass") || n.startsWith("rooted") || n.startsWith("roots") || n.startsWith("sapling")
                || n.startsWith("tall") || n.startsWith("podzol") || n.startsWith("mycelium") || n.contains("sugarcane"))
            return CommonConfigs.GROWTH_VEGETATION;
        return CommonConfigs.GROWTH_OTHER;
    }

    @Override
    public @Nullable Iterable<? extends Block> getOwners() {
        return inner.getOwners();
    }

    @Override
    public Collection<TickSource> getTickSources() {
        return inner.getTickSources();
    }

    @Override
    public void tryGrowing(BlockPos pos, BlockState state, ServerLevel level, Supplier<Holder<Biome>> biome) {
        double m = CommonConfigs.GROWTH_SPEED.get() * category.get();
        if (m == 1) {
            inner.tryGrowing(pos, state, level, biome);
            return;
        }
        int times = (int) m;
        if (level.getRandom().nextDouble() < m - times) times++;
        for (int i = 0; i < times && i < 100; i++) {
            //stop once the block changed, the growth is meant for the old block
            if (i > 0 && level.getBlockState(pos) != state) return;
            inner.tryGrowing(pos, state, level, biome);
        }
    }
}
