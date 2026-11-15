package com.ordana.immersive_weathering.items;

import com.ordana.immersive_weathering.configs.CommonConfigs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Sword stats come from {@code Item.Properties#sword} (SwordItem no longer exists).
 */
public class IceSickleItem extends Item {

    public IceSickleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!CommonConfigs.ICICLE_FOOD.get()) return InteractionResult.PASS;
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        IcicleItem.applyIcicleEffects(level, entity);
        return super.finishUsingItem(stack, level, entity);
    }
}
