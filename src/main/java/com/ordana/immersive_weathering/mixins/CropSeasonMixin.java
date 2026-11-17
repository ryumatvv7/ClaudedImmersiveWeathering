package com.ordana.immersive_weathering.mixins;

import com.ordana.immersive_weathering.util.Seasons;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Seasons: outdoor crops grow slower in autumn and barely in winter. Greenhouses (no sky above) are unaffected. */
@Mixin(CropBlock.class)
public abstract class CropSeasonMixin {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void iw$season(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        double f = Seasons.cropFactor();
        if (f < 1 && random.nextDouble() >= f && level.canSeeSky(pos)) ci.cancel();
    }
}
