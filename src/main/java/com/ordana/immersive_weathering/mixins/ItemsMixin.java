package com.ordana.immersive_weathering.mixins;

import java.util.function.BiFunction;
import com.ordana.immersive_weathering.items.CeilingAndWallBlockItem;
import com.ordana.immersive_weathering.reg.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Items.class)
public abstract class ItemsMixin {

    @Shadow
    private static Item registerBlock(net.minecraft.references.BlockItemId id, Block block,
                                      BiFunction<Block, Item.Properties, Item> itemFactory, Item.Properties properties) {
        return null;
    }

    //hanging roots can also be placed on walls
    @Inject(method = "registerBlock(Lnet/minecraft/references/BlockItemId;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/item/Item$Properties;)Lnet/minecraft/world/item/Item;",
            at = @At("HEAD"), cancellable = true)
    private static void addRoot(net.minecraft.references.BlockItemId id, Block block, Item.Properties properties, CallbackInfoReturnable<Item> cir) {
        if (block == Blocks.HANGING_ROOTS) {
            cir.setReturnValue(registerBlock(id, block, (b, p) -> new CeilingAndWallBlockItem(b, () -> ModBlocks.HANGING_ROOTS_WALL.get(), p), properties));
        }
    }
}
