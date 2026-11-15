package com.ordana.immersive_weathering.items;

import com.ordana.immersive_weathering.ImmersiveWeathering;
import com.ordana.immersive_weathering.reg.ModParticles;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * ArmorItem no longer exists: armor stats come from {@code Item.Properties#humanoidArmor}.
 * <p>
 * Named ("supporter") crowns: the worn texture is chosen by the equippable asset id of the stack.
 * It is updated on the server from the custom name and synced to everybody by vanilla,
 * so no client side mixin is needed and all players see the same crown.
 */
public class FlowerCrownItem extends Item {

    public static final ResourceKey<EquipmentAsset> DEFAULT_ASSET = asset("flower");

    public static ResourceKey<EquipmentAsset> asset(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, ImmersiveWeathering.res(name));
    }

    public FlowerCrownItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        updateEquipmentAsset(stack);
        if (slot == EquipmentSlot.HEAD && entity instanceof LivingEntity) {
            spawnParticles(stack, level, entity);
        }
        super.inventoryTick(stack, level, entity, slot);
    }

    private static void updateEquipmentAsset(ItemStack stack) {
        Equippable eq = stack.get(DataComponents.EQUIPPABLE);
        if (eq == null) return;
        SpecialType type = getSpecialType(stack);
        ResourceKey<EquipmentAsset> wanted = type == null ? DEFAULT_ASSET : asset(type.assetName);
        if (eq.assetId().isEmpty() || !eq.assetId().get().equals(wanted)) {
            stack.set(DataComponents.EQUIPPABLE, new Equippable(eq.slot(), eq.equipSound(), Optional.of(wanted),
                    eq.cameraOverlay(), eq.allowedEntities(), eq.dispensable(), eq.swappable(),
                    eq.damageOnHurt(), eq.equipOnInteract(), eq.canBeSheared(), eq.shearingSound()));
        }
    }

    //server side so every player nearby sees them
    private static void spawnParticles(ItemStack stack, ServerLevel level, Entity entity) {
        if (level.getRandom().nextFloat() < 0.05) {
            Vec3 v = entity.getViewVector(1).scale(entity.isSwimming() ? 1.8 : -0.15f);
            level.sendParticles(getParticle(stack),
                    v.x + entity.getRandomX(0.675D),
                    v.y + entity.getY() + entity.getEyeHeight() + 0.15D,
                    v.z + entity.getRandomZ(0.675D), 1, 0, 0, 0, 0);
        }
    }

    public static SimpleParticleType getParticle(ItemStack stack) {
        var type = getSpecialType(stack);
        if (type != null) return type.particle.get();
        return ModParticles.AZALEA_FLOWER.get();
    }

    public static float getItemTextureIndex(ItemStack stack) {
        var type = getSpecialType(stack);
        if (type != null) return type.itemModelIndex;
        return 0f;
    }


    @Nullable
    public static SpecialType getSpecialType(ItemStack stack) {
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            var name = stack.getHoverName().getString();
            return SUPPORTERS_LIST.get(name.toLowerCase(Locale.ROOT));
        }
        return null;
    }

    //pair of item name, entity model texture location and item model index
    //KEEP THESE LOWERCASE
    private static final Map<String, SpecialType> SUPPORTERS_LIST = Util.make(() -> {
        Map<String, SpecialType> map = new HashMap<>();
        //dev and gift crowns
        map.put("ordana", new SpecialType("flower_bee",
                0.10f, ModParticles.FLOWER_BEE));
        map.put("bee", new SpecialType("flower_bee",
                0.10f, ModParticles.FLOWER_BEE));
        map.put("mehvahdjukaar", new SpecialType("flower_jar",
                0.11f, ModParticles.FLOWER_JAR));
        map.put("jar", new SpecialType("flower_jar",
                0.11f, ModParticles.FLOWER_JAR));
        map.put("bob", new SpecialType("flower_bob",
                0.12f, ModParticles.FLOWER_BOB));
        map.put("bobisnotaperson", new SpecialType("flower_bob",
                0.12f, ModParticles.FLOWER_BOB));
        /*
        put("key", new SpecialType("flower_key",
                0.12f, ModParticles.FLOWER_KEY));
        put("keybounce", new SpecialType("flower_key",
                0.12f, ModParticles.FLOWER_KEY));
        put("keyb0unce", new SpecialType("flower_key",
                0.12f, ModParticles.FLOWER_KEY));
         */

        map.put("asexual", new SpecialType("flower_ace",
                0.200f, ModParticles.FLOWER_ACE));
        map.put("ace", new SpecialType("flower_ace",
                0.200f, ModParticles.FLOWER_ACE));
        map.put("aromantic", new SpecialType("flower_aro",
                0.201f, ModParticles.FLOWER_ARO));
        map.put("aro", new SpecialType("flower_aro",
                0.201f, ModParticles.FLOWER_ARO));
        map.put("bisexual", new SpecialType("flower_bi",
                0.202f, ModParticles.FLOWER_BI));
        map.put("bi", new SpecialType("flower_bi",
                0.202f, ModParticles.FLOWER_BI));
        map.put("non-binary", new SpecialType("flower_enby",
                0.203f, ModParticles.FLOWER_ENBY));
        map.put("nb", new SpecialType("flower_enby",
                0.203f, ModParticles.FLOWER_ENBY));
        map.put("enby", new SpecialType("flower_enby",
                0.203f, ModParticles.FLOWER_ENBY));
        map.put("gay", new SpecialType("flower_gay",
                0.204f, ModParticles.FLOWER_GAY));
        map.put("mlm", new SpecialType("flower_gay",
                0.204f, ModParticles.FLOWER_GAY));
        map.put("lesbian", new SpecialType("flower_lesbian",
                0.205f, ModParticles.FLOWER_LESBIAN));
        map.put("wlw", new SpecialType("flower_lesbian",
                0.205f, ModParticles.FLOWER_LESBIAN));
        map.put("rainbow", new SpecialType("flower_rainbow",
                0.206f, ModParticles.FLOWER_RAINBOW));
        map.put("pride", new SpecialType("flower_rainbow",
                0.206f, ModParticles.FLOWER_RAINBOW));
        map.put("trans", new SpecialType("flower_trans",
                0.207f, ModParticles.FLOWER_TRANS));
        map.put("transgender", new SpecialType("flower_trans",
                0.207f, ModParticles.FLOWER_TRANS));
        map.put("pan", new SpecialType("flower_pan",
                0.208f, ModParticles.FLOWER_PAN));
        map.put("pansexual", new SpecialType("flower_pan",
                0.208f, ModParticles.FLOWER_PAN));
        map.put("intersex", new SpecialType("flower_intersex",
                0.209f, ModParticles.FLOWER_INTERSEX));
        map.put("genderqueer", new SpecialType("flower_genderqueer",
                0.210f, ModParticles.FLOWER_GENDERQUEER));
        map.put("genderfluid", new SpecialType("flower_fluid",
                0.211f, ModParticles.FLOWER_FLUID));
        map.put("fluid", new SpecialType("flower_fluid",
                0.211f, ModParticles.FLOWER_FLUID));

        //supporter crowns
        map.put("flax", new SpecialType("flower_flax",
                0.30f, ModParticles.FLOWER_FLAX));
        map.put("neko", new SpecialType("flower_nekomaster",
                0.31f, ModParticles.FLOWER_NEKOMASTER));
        map.put("nekomaster", new SpecialType("flower_nekomaster",
                0.31f, ModParticles.FLOWER_NEKOMASTER));
        map.put("akashii", new SpecialType("flower_akashii",
                0.32f, ModParticles.FLOWER_AKASHII));

        return map;
    });

    public record SpecialType(String assetName, float itemModelIndex,
                              Supplier<SimpleParticleType> particle) {
    }
}