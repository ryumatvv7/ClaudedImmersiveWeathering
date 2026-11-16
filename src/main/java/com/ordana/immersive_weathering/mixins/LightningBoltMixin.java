package com.ordana.immersive_weathering.mixins;

import com.ordana.immersive_weathering.events.ModEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//replaces Moonlight's ILightningStruckBlockEvent
@Mixin(LightningBolt.class)
public abstract class LightningBoltMixin {

    @Shadow
    private BlockPos getStrikePosition() { throw new AssertionError(); }

    @Shadow
    private boolean visualOnly;

    @Inject(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LightningBolt;clearCopperOnLightningStrike(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V",
            shift = At.Shift.AFTER))
    private void iw$onStrike(CallbackInfo ci) {
        LightningBolt self = (LightningBolt) (Object) this;
        if (self.level() instanceof ServerLevel level && !this.visualOnly) {
            ModEvents.onLightningHit(level, getStrikePosition());
        }
    }
}
