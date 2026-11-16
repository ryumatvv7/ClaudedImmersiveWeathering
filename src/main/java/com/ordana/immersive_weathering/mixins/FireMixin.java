package com.ordana.immersive_weathering.mixins;

import com.ordana.immersive_weathering.reg.ModTags;
import com.ordana.immersive_weathering.util.WeatheringHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(FireBlock.class)
public abstract class FireMixin extends BaseFireBlock{

    protected FireMixin(Properties settings, float damage) {
        super(settings, damage);
    }


    //expired fire turns into soot
    @Inject(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z",
                    ordinal = 2,
                    shift = At.Shift.AFTER))
    private void removeBlock(BlockState state, ServerLevel serverLevel, BlockPos pos, RandomSource random, CallbackInfo ci) {
        WeatheringHelper.onFireExpired(serverLevel, pos, state);
    }

    //burned out at max age
    @Inject(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z",
                    ordinal = 3,
                    shift = At.Shift.AFTER))
    private void removeBlockOld(BlockState state, ServerLevel serverLevel, BlockPos pos, RandomSource random, CallbackInfo ci) {
        WeatheringHelper.onFireExpired(serverLevel, pos, state);
    }

    //fire can replace soot
    @Inject(method = "getIgniteOdds(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)I",
            at = @At(value = "HEAD"), cancellable = true)
    private void canFireReplace(LevelReader reader, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (reader.getBlockState(pos).is(ModTags.FIRE_REPLACEABLE)) cir.setReturnValue(0);
    }



    //fire burning a block away (replaces Moonlight's IFireConsumeBlockEvent)
    @org.spongepowered.asm.mixin.Unique
    private static final ThreadLocal<BlockState> IW_BURNING = new ThreadLocal<>();

    @Inject(method = "checkBurnOut", at = @At("HEAD"))
    private void iw$rememberBurning(net.minecraft.world.level.Level level, BlockPos pos, int chance, RandomSource random, int age, CallbackInfo ci) {
        IW_BURNING.set(level.getBlockState(pos));
    }

    @Inject(method = "checkBurnOut", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z",
            shift = At.Shift.AFTER))
    private void iw$replacedByFire(net.minecraft.world.level.Level level, BlockPos pos, int chance, RandomSource random, int age, CallbackInfo ci) {
        com.ordana.immersive_weathering.events.ModEvents.onFireConsume(level, pos, IW_BURNING.get(), true);
    }

    @Inject(method = "checkBurnOut", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z",
            shift = At.Shift.AFTER))
    private void iw$burnedAway(net.minecraft.world.level.Level level, BlockPos pos, int chance, RandomSource random, int age, CallbackInfo ci) {
        com.ordana.immersive_weathering.events.ModEvents.onFireConsume(level, pos, IW_BURNING.get(), false);
    }

}
