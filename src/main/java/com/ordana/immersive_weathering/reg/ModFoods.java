package com.ordana.immersive_weathering.reg;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

import java.util.List;

/**
 * Since 1.21.2 food is split in two components: {@link FoodProperties} (nutrition) and
 * {@link Consumable} (eat time, effects).
 */
public class ModFoods {

    private static final float FAST = 0.8F;

    public static final FoodProperties MOSS_CLUMP = new FoodProperties.Builder()
            .nutrition(1).saturationModifier(0.2F).alwaysEdible().build();
    public static final Consumable MOSS_CLUMP_CONSUMABLE = Consumables.defaultFood().consumeSeconds(FAST).build();

    public static final FoodProperties GOLDEN_MOSS_CLUMP = new FoodProperties.Builder()
            .nutrition(4).saturationModifier(0.6F).alwaysEdible().build();
    public static final Consumable GOLDEN_MOSS_CLUMP_CONSUMABLE = Consumables.defaultFood()
            .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1), 1F))
            .build();

    public static final FoodProperties ENCHANTED_GOLDEN_MOSS_CLUMP = new FoodProperties.Builder()
            .nutrition(6).saturationModifier(0.8F).alwaysEdible().build();
    public static final Consumable ENCHANTED_GOLDEN_MOSS_CLUMP_CONSUMABLE = Consumables.defaultFood()
            .onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
                    new MobEffectInstance(MobEffects.ABSORPTION, 1600, 2),
                    new MobEffectInstance(MobEffects.REGENERATION, 300, 1),
                    new MobEffectInstance(MobEffects.WATER_BREATHING, 1600, 2)), 1F))
            .build();

    //fire resistance is applied in the item class so it can obey the server config
    public static final FoodProperties ICICLE = new FoodProperties.Builder()
            .nutrition(0).saturationModifier(0F).alwaysEdible().build();
    public static final Consumable ICICLE_CONSUMABLE = Consumables.defaultFood().consumeSeconds(FAST).build();

    public static final FoodProperties AZALEA_FLOWER = new FoodProperties.Builder()
            .nutrition(0).saturationModifier(0F).alwaysEdible().build();
    public static final Consumable AZALEA_FLOWER_CONSUMABLE = Consumables.defaultFood().consumeSeconds(FAST).build();
}
