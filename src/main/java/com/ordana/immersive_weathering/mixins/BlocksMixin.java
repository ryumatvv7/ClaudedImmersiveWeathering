package com.ordana.immersive_weathering.mixins;

import com.ordana.immersive_weathering.reg.VanillaBlockReplacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Function;

/**
 * Swaps the factory of some vanilla blocks while they are being registered so they become
 * crackable / mossable / rustable. Replaces the 56 constructor redirects of the original mixin.
 */
@Mixin(Blocks.class)
public abstract class BlocksMixin {

    @ModifyVariable(
            method = "register(Lnet/minecraft/resources/ResourceKey;Ljava/util/function/Function;Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)Lnet/minecraft/world/level/block/Block;",
            at = @At("HEAD"), argsOnly = true)
    private static Function<BlockBehaviour.Properties, Block> iw$replaceVanillaBlock(
            Function<BlockBehaviour.Properties, Block> factory, ResourceKey<Block> id) {
        return VanillaBlockReplacements.get(id, factory);
    }
}
