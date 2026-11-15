package com.ordana.immersive_weathering.items;

import com.ordana.immersive_weathering.configs.CommonConfigs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class IcicleItem extends BlockItem {

    public IcicleItem(Block block, Properties properties) {
        super(block, properties);
    }

    //food is a static component now; the server config decides if eating is allowed at all
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!CommonConfigs.ICICLE_FOOD.get()) return InteractionResult.PASS;
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        applyIcicleEffects(level, entity);
        return super.finishUsingItem(stack, level, entity);
    }

    public static void applyIcicleEffects(Level level, LivingEntity entity) {
        entity.setTicksFrozen(Math.min(entity.getTicksRequiredToFreeze(), entity.getTicksFrozen() + 80));
        if (!level.isClientSide() && CommonConfigs.ICICLE_FIRE_RESISTANCE.get()) {
            entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 80, 1, false, false));
        }
    }
}
