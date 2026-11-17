package com.ordana.immersive_weathering.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.ordana.immersive_weathering.data.block_growths.BlockGrowthHandler;
import com.ordana.immersive_weathering.data.block_growths.TickSource;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks the server random tick loop: every randomly picked position also runs the data driven block growths.
 * Server only (ServerLevel does not tick chunks on clients).
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    //right after the vanilla random tick of the picked block, before its fluid tick
    @Inject(method = "tickChunk",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;"),
            require = 1)
    private void iw$weatheringTick(LevelChunk chunk, int tickSpeed, CallbackInfo ci, @Local BlockPos pos) {
        ServerLevel level = (ServerLevel) (Object) this;
        // we need to get it again as it might have been changed by its own random tick
        BlockState newState = chunk.getBlockState(pos);
        BlockGrowthHandler.tickBlock(TickSource.BLOCK_TICK, newState, level, pos);
    }

    @Inject(method = "tickChunk", require = 0, at = @At(value = "TAIL"))
    private void iw$precipitationTick(LevelChunk chunk, int tickSpeed, CallbackInfo ci) {
        ProfilerFiller p = Profiler.get();
        p.push("ImmWeatheringExtraRandomTicks");
        BlockGrowthHandler.performSkyAccessTick((ServerLevel) (Object) this, chunk, tickSpeed);
        com.ordana.immersive_weathering.util.Seasons.chunkTick((ServerLevel) (Object) this, chunk);
        p.pop();
    }
}
