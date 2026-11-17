package com.ordana.immersive_weathering.mixins;

import com.ordana.immersive_weathering.util.Seasons;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Seasons: shifts biome temperatures (after the per-position cache, so a season change applies at once). */
@Mixin(Biome.class)
public abstract class BiomeSeasonMixin {

    @Inject(method = "getTemperature", at = @At("RETURN"), cancellable = true)
    private void iw$season(BlockPos pos, int seaLevel, CallbackInfoReturnable<Float> cir) {
        float o = Seasons.temperatureOffset();
        //deserts, savannas, badlands and the nether stay hot
        if (o != 0 && cir.getReturnValueF() < 1.5f) cir.setReturnValue(cir.getReturnValueF() + o);
    }
}
