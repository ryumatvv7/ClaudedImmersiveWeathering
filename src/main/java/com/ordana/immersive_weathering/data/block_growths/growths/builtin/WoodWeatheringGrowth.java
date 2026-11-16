package com.ordana.immersive_weathering.data.block_growths.growths.builtin;

import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.data.block_growths.TickSource;
import com.ordana.immersive_weathering.reg.ModBlocks;
import com.ordana.immersive_weathering.reg.ModTags;
import com.ordana.immersive_weathering.util.WoodWeathering;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Port addition: wood in or next to water rots, damp wood grows moss, rotten wood under water
 * eventually falls apart. Rain in wet biomes rots exposed wood slowly.
 */
public class WoodWeatheringGrowth extends BuiltinBlockGrowth {

    public WoodWeatheringGrowth(String name, @Nullable HolderSet<Block> owners, List<TickSource> sources, float chance) {
        super(name, owners, sources, chance);
    }

    @Override
    public @Nullable Iterable<Block> getOwners() {
        List<Block> l = new ArrayList<>();
        for (Block b : BuiltInRegistries.BLOCK) {
            if (WoodWeathering.isWeatherableWood(b) || WoodWeathering.isRotten(b)) l.add(b);
        }
        return l;
    }

    private static boolean touchesWater(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) return true;
        for (Direction d : Direction.values()) {
            if (level.getFluidState(pos.relative(d)).is(FluidTags.WATER)) return true;
        }
        return false;
    }

    private static boolean submerged(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) return true;
        return level.getFluidState(pos.above()).is(FluidTags.WATER);
    }

    @Override
    public void tryGrowing(BlockPos pos, BlockState state, ServerLevel level, Supplier<Holder<Biome>> biome) {
        RandomSource random = level.getRandom();
        if (!(growthChance >= 1 || random.nextFloat() < growthChance)) return;

        if (com.ordana.immersive_weathering.util.Varnish.isVarnished(level, pos)) return;
        boolean water = touchesWater(level, pos, state);
        boolean rain = level.isRainingAt(pos.above());
        boolean wetBiome = biome.get().is(ModTags.WET);
        boolean damp = water || (rain && wetBiome);
        if (!damp && !(wetBiome && random.nextFloat() < 0.15f)) return;

        boolean rotten = WoodWeathering.isRotten(state.getBlock());
        if (rotten) {
            //rotten wood under water slowly falls apart
            if (CommonConfigs.WOOD_ROT.get() && submerged(level, pos, state) && random.nextFloat() < 0.04f) {
                level.levelEvent(2001, pos, Block.getId(state));
                level.setBlockAndUpdate(pos, state.getFluidState().createLegacyBlock());
                return;
            }
        } else if (CommonConfigs.WOOD_ROT.get()) {
            float rotChance = water ? 0.5f : 0.06f;
            if (random.nextFloat() < rotChance) {
                BlockState r = WoodWeathering.getRotten(state);
                if (r != null) {
                    level.setBlockAndUpdate(pos, r);
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.05);
                    return;
                }
            }
        }

        if (CommonConfigs.WOOD_MOSS.get() && random.nextFloat() < 0.5f) growMoss(level, pos, random);
    }

    //a layer of moss on a random free side (not under water)
    private static void growMoss(ServerLevel level, BlockPos pos, RandomSource random) {
        Direction dir = Direction.getRandom(random);
        BlockPos side = pos.relative(dir);
        BlockState sideState = level.getBlockState(side);
        Block moss = ModBlocks.MOSS.get();
        Direction face = dir.getOpposite();
        if (sideState.isAir()) {
            BlockState s = moss.defaultBlockState().setValue(MultifaceBlock.getFaceProperty(face), true);
            if (s.canSurvive(level, side)) level.setBlockAndUpdate(side, s);
        } else if (sideState.is(moss) && !sideState.getValue(MultifaceBlock.getFaceProperty(face))) {
            BlockState s = sideState.setValue(MultifaceBlock.getFaceProperty(face), true);
            if (s.canSurvive(level, side)) level.setBlockAndUpdate(side, s);
        }
    }
}
