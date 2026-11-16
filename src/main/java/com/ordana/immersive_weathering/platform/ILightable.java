package com.ordana.immersive_weathering.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * A block that can be lit up / put out (replaces Moonlight's ILightable).
 */
public interface ILightable {

    boolean isLitUp(BlockState state, BlockGetter level, BlockPos pos);

    void setLitUp(BlockState state, LevelAccessor level, BlockPos pos, @Nullable Entity entity, boolean lit);

    /** Burning projectiles light the block up. */
    default boolean lightableInteractWithEntity(Level level, BlockState state, Projectile projectile, BlockPos pos) {
        if (level.isClientSide() || !projectile.isOnFire()) return false;
        if (isLitUp(state, level, pos)) return false;
        Entity owner = projectile.getOwner();
        setLitUp(state, level, pos, owner, true);
        level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.5f, 1.5f);
        return true;
    }
}
