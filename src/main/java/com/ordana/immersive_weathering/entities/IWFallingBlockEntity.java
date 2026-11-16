package com.ordana.immersive_weathering.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Falling block entity with a settable block state (replaces Moonlight's ImprovedFallingBlockEntity).
 * Rendered with the vanilla falling block renderer.
 */
public class IWFallingBlockEntity extends FallingBlockEntity {

    public IWFallingBlockEntity(EntityType<? extends FallingBlockEntity> type, Level level) {
        super(type, level);
    }

    public IWFallingBlockEntity(EntityType<? extends FallingBlockEntity> type, Level level, BlockPos pos,
                                BlockState state, boolean canDrop) {
        this(type, level);
        this.blocksBuilding = true;
        this.xo = pos.getX() + 0.5D;
        this.yo = pos.getY();
        this.zo = pos.getZ() + 0.5D;
        this.setPos(xo, yo + (double) ((1.0F - this.getBbHeight()) / 2.0F), zo);
        this.setDeltaMovement(Vec3.ZERO);
        this.setStartPos(this.blockPosition());
        this.setBlockState(state);
        this.dropItem = canDrop;
    }

    public void setBlockState(BlockState state) {
        this.blockState = state;
    }
}
