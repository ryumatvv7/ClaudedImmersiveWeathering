package com.ordana.immersive_weathering.items;

import com.ordana.immersive_weathering.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port addition: ash places ash layers; on plants it works like bone meal (configurable).
 */
public class AshItem extends BlockItem {

    public AshItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (CommonConfigs.ASH_FERTILIZER.get() && state.getBlock() instanceof BonemealableBlock b
                && b.isValidBonemealTarget(level, pos, state, net.minecraft.world.level.block.BonemealSource.INTERACTION)) {
            if (level instanceof ServerLevel serverLevel) {
                if (b.isBonemealSuccess(level, level.getRandom(), pos, state, net.minecraft.world.level.block.BonemealSource.INTERACTION)) {
                    b.performBonemeal(serverLevel, level.getRandom(), pos, state, net.minecraft.world.level.block.BonemealSource.INTERACTION);
                }
                context.getItemInHand().consume(1, context.getPlayer());
                level.levelEvent(1505, pos, 15); //bone meal particles
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }
}
