package com.ordana.immersive_weathering.blocks.snowy;

import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.item.ItemStack;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class SnowyBlock extends Block implements Snowy{

    public SnowyBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(interactWithPlayer(state, level, pos, player, hand, hit)){
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(heldStack, state, level, pos, player, hand, hit);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Snowy.super.randomTick(state, level, pos, random);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation neighborPos, boolean isMoving) {
        Snowy.super.neighborChanged(state, level, pos, block, neighborPos, isMoving);
    }
}