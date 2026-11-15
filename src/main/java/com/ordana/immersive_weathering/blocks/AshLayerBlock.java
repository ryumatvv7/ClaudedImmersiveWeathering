package com.ordana.immersive_weathering.blocks;

import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.reg.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/**
 * Port addition (ash existed in older Immersive Weathering versions, removed before 2.0.5):
 * a falling layer of ash left by fire. Rain slowly washes it away.
 */
public class AshLayerBlock extends SandLayerBlock {

    public AshLayerBlock(Properties properties) {
        super(0x6b6966, properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.isRainingAt(pos.above()) && random.nextFloat() < CommonConfigs.ASH_RAIN_WASH_CHANCE.get()) {
            removeOneLayer(state, pos, level);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(12) == 0) {
            level.addParticle(ParticleTypes.WHITE_ASH, pos.getX() + random.nextDouble(),
                    pos.getY() + getLayers(state) / 8.0 + 0.05, pos.getZ() + random.nextDouble(), 0, 0.02, 0);
        }
    }

    /**
     * Puts ash at pos (adding layers to existing ash). Server side only.
     */
    public static void placeAsh(LevelAccessor level, BlockPos pos, int layers) {
        if (!CommonConfigs.ASH_ENABLED.get() || layers <= 0) return;
        BlockState current = level.getBlockState(pos);
        BlockState ash = ModBlocks.ASH_LAYER_BLOCK.get().defaultBlockState();
        if (current.is(ModBlocks.ASH_LAYER_BLOCK.get())) {
            int l = Math.min(8, current.getValue(LAYERS_8) + layers);
            level.setBlock(pos, current.setValue(LAYERS_8, l), Block.UPDATE_ALL);
        } else if ((current.isAir() || current.is(net.minecraft.world.level.block.Blocks.FIRE))
                && current.getFluidState().getType() == Fluids.EMPTY
                && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP)) {
            level.setBlock(pos, ash.setValue(LAYERS_8, Math.min(8, layers)), Block.UPDATE_ALL);
            if (CommonConfigs.ASH_KILLS_GRASS.get() && level.getBlockState(pos.below()).is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)) {
                level.setBlock(pos.below(), net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }
}
