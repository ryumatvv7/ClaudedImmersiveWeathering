package com.ordana.immersive_weathering.blocks.soil_types;

import net.minecraft.world.level.block.SpreadingSnowyBlock;
import com.ordana.immersive_weathering.reg.ModBlocks;
import com.ordana.immersive_weathering.util.WeatheringHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;


public class RootedGrassBlock extends GrassBlock implements BonemealableBlock {
    public RootedGrassBlock(Properties settings) {
        super(settings);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);
        Item item = stack.getItem();
        if (item.getDefaultInstance().is(net.minecraft.tags.ItemTags.SHOVELS) && !state.getValue(SNOWY)) {
            level.playSound(player, pos, SoundEvents.HOE_TILL.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
            stack.hurtAndBreak(1, player, hand);
            if (player instanceof ServerPlayer) {
                level.setBlockAndUpdate(pos, Blocks.DIRT_PATH.defaultBlockState());
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            }
            return InteractionResult.SUCCESS;
        }
        if (item.getDefaultInstance().is(net.minecraft.tags.ItemTags.HOES)) {
            level.playSound(player, pos, SoundEvents.HOE_TILL.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
            stack.hurtAndBreak(1, player, hand);
            if (player instanceof ServerPlayer) {
                level.setBlockAndUpdate(pos, Blocks.GRASS_BLOCK.withPropertiesOf(state));
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                Block.popResourceFromFace(level, pos, hitResult.getDirection(), Items.HANGING_ROOTS.getDefaultInstance());
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(heldStack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, net.minecraft.world.level.block.BonemealSource source) {
        boolean space = false;
        for (Direction dir : Direction.values()) {
            var targetState = level.getBlockState(pos.relative(dir));
            if (dir == Direction.UP) {if (level.getBlockState(pos.above()).isAir()) space = true;}
            else if (targetState.canBeReplaced() &&
                !targetState.is(Blocks.HANGING_ROOTS) &&
                !targetState.is(ModBlocks.HANGING_ROOTS_WALL.get())) space = true;
        }
        return space;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, net.minecraft.world.level.block.BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, net.minecraft.world.level.block.BonemealSource source) {
        WeatheringHelper.growHangingRoots(level, random, pos);
        super.performBonemeal(level, random, pos, state, net.minecraft.world.level.block.BonemealSource.INTERACTION);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!SpreadingSnowyBlock.canStayAlive(state, level, pos)) {
            level.setBlockAndUpdate(pos, Blocks.ROOTED_DIRT.defaultBlockState());
        }
        else super.randomTick(state, level, pos, random);
    }
}
