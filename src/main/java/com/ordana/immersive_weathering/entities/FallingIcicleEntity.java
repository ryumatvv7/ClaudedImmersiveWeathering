package com.ordana.immersive_weathering.entities;

import com.ordana.immersive_weathering.reg.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class FallingIcicleEntity extends IWFallingBlockEntity {

    public FallingIcicleEntity(EntityType<? extends FallingBlockEntity> type, Level level) {
        super(type, level);
    }

    public FallingIcicleEntity(Level level, double x, double y, double z, BlockState state) {
        this(ModEntities.FALLING_ICICLE.get(), level);
        this.setBlockState(state);
        this.blocksBuilding = true;
        this.setPos(x, y, z);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.setStartPos(this.blockPosition());
    }

    //workaround
    @Override
    public void setBlockState(BlockState state) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        super.setBlockState(state);
    }

    public static FallingBlockEntity fall(Level level, BlockPos pos, BlockState state) {
        FallingBlockEntity fallingblockentity = new FallingIcicleEntity(level,
                pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                state.hasProperty(BlockStateProperties.WATERLOGGED) ? state.setValue(BlockStateProperties.WATERLOGGED, false) : state);
        level.setBlock(pos, state.getFluidState().createLegacyBlock(), 3);
        level.addFreshEntity(fallingblockentity);
        return fallingblockentity;
    }

    @Nullable
    @Override
    public ItemEntity spawnAtLocation(ServerLevel serverLevel, ItemStack stack, float yOffset) {
        return null;
    }
}
