package com.ordana.immersive_weathering.data.block_growths.growths.builtin;

import com.ordana.immersive_weathering.data.block_growths.TickSource;
import com.ordana.immersive_weathering.reg.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class FireSootGrowth extends BuiltinBlockGrowth {

    public FireSootGrowth(String name, @Nullable HolderSet<Block> owners, List<TickSource> sources, float chance) {
        super(name, owners, sources, chance);
    }

    @Override
    public void tryGrowing(BlockPos pos, BlockState state, ServerLevel level, Supplier<Holder<Biome>> biome) {
        if (!(growthChance == 1 || level.getRandom().nextFloat() < growthChance)) return;

        //fire tick game rule became a radius: 0 means fire does not spread
        if (level.getGameRules().get(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER) != 0) {
            spawnSootAboveFire(level, pos, 6);
        }
    }

    public static void spawnSootAboveFire(ServerLevel level, BlockPos pos, int smokeHeight) {
        BlockPos sootPos = pos;
        for (int i = 0; i < smokeHeight; i++) {
            sootPos = sootPos.above();
            BlockState above = level.getBlockState(sootPos.above());
            if (Block.isFaceFull(above.getCollisionShape(level, sootPos.above()), Direction.DOWN)) {
                if (level.getBlockState(sootPos).isAir()) {
                    level.setBlock(sootPos, ModBlocks.SOOT.get().defaultBlockState().setValue(BlockStateProperties.UP, true), Block.UPDATE_CLIENTS);
                }
                return;
            }
        }
    }
}
