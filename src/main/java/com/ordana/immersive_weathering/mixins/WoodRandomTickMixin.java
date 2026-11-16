package com.ordana.immersive_weathering.mixins;

import com.ordana.immersive_weathering.util.WoodWeathering;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//port addition: wooden blocks get random ticks so they can rot / grow moss (even under water)
@Mixin(BlockBehaviour.class)
public abstract class WoodRandomTickMixin {

    @Inject(method = "isRandomlyTicking", at = @At("HEAD"), cancellable = true)
    private void iw$woodTicks(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Block b && WoodWeathering.isWeatherableWood(b)) cir.setReturnValue(true);
    }
}
