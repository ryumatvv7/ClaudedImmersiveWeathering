package com.ordana.immersive_weathering.reg;

import com.ordana.immersive_weathering.ImmersiveWeathering;
import com.ordana.immersive_weathering.blocks.LeafPileBlock;
import com.ordana.immersive_weathering.items.*;
import net.mehvahdjukaar.moonlight.api.item.WoodBasedItem;
import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.leaves.LeavesType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Blocks;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModItems {

    public static void init() {
        BlockSetAPI.addDynamicRegistration(ImmersiveWeathering.MOD_ID, ModItems::registerLeafPilesItems, BuiltInRegistries.ITEM);
        BlockSetAPI.addDynamicRegistration(ImmersiveWeathering.MOD_ID, ModItems::registerBark, BuiltInRegistries.ITEM);
    }

    public static <T extends Item> Supplier<T> regItem(String name, Function<Item.Properties, T> factory, Supplier<Item.Properties> properties) {
        return RegHelper.registerItem(ImmersiveWeathering.res(name), factory, properties.get());
    }

    private static ResourceKey<Item> key(String name) {
        return ResourceKey.create(Registries.ITEM, ImmersiveWeathering.res(name));
    }

    public static final TagKey<Item> ICICLE_TOOL_MATERIALS = TagKey.create(Registries.ITEM, ImmersiveWeathering.res("icicle_tool_materials"));
    public static final TagKey<Item> REPAIRS_FLOWER_CROWN = TagKey.create(Registries.ITEM, ImmersiveWeathering.res("repairs_flower_crown"));

    //old Tier: level 1 (stone), 3 uses, speed 8, +1 damage, enchantability 10, repaired with icicles
    public static final ToolMaterial ICICLE_TOOL_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_STONE_TOOL, 3, 8f, 1f, 10, ICICLE_TOOL_MATERIALS);

    //no defense, enchantability 64, repaired with azalea flowers. Durability (64) is set on the item
    public static final ArmorMaterial FLOWER_CROWN_MATERIAL = new ArmorMaterial(
            6, Map.of(ArmorType.HELMET, 0, ArmorType.CHESTPLATE, 0, ArmorType.LEGGINGS, 0, ArmorType.BOOTS, 0, ArmorType.BODY, 0),
            64, SoundEvents.ARMOR_EQUIP_LEATHER, 0f, 0f, REPAIRS_FLOWER_CROWN, FlowerCrownItem.DEFAULT_ASSET);

    //helpers

    private static Supplier<BlockItem> regLeafPile(String name, Supplier<LeafPileBlock> oakLeafPile) {
        return regItem(name, p -> new LeafPileBlockItem(oakLeafPile.get(), p), () -> new Item.Properties().useBlockDescriptionPrefix());
    }

    //icicle

    public static final Supplier<BlockItem> ICICLE = regItem("icicle", p -> new IcicleItem(
            ModBlocks.ICICLE.get(), p), () -> new Item.Properties()
            .food(ModFoods.ICICLE, ModFoods.ICICLE_CONSUMABLE).useBlockDescriptionPrefix());

    //leaf pile
    public static final Map<LeavesType, BlockItem> LEAF_PILES = new LinkedHashMap<>();
    public static final Supplier<BlockItem> AZALEA_FLOWER_PILE = regLeafPile("azalea_flower_pile", ModBlocks.AZALEA_FLOWER_PILE);

    //bricks

    public static final Supplier<Item> STONE_BRICK = regItem("stone_brick", Item::new, Item.Properties::new);
    public static final Supplier<Item> PRISMARINE_BRICK = regItem("prismarine_brick", Item::new, Item.Properties::new);
    public static final Supplier<Item> END_STONE_BRICK = regItem("end_stone_brick", Item::new, Item.Properties::new);
    public static final Supplier<Item> BLACKSTONE_BRICK = regItem("blackstone_brick", Item::new, Item.Properties::new);
    public static final Supplier<Item> DEEPSLATE_BRICK = regItem("deepslate_brick", Item::new, Item.Properties::new);
    public static final Supplier<Item> DEEPSLATE_TILE = regItem("deepslate_tile", Item::new, Item.Properties::new);
    public static final Supplier<Item> MORTAR = regItem("mortar", Item::new, Item.Properties::new);

    //flowers

    public static final Supplier<Item> AZALEA_FLOWERS = regItem("azalea_flowers", AzaleaFlowersItem::new,
            () -> new Item.Properties().food(ModFoods.AZALEA_FLOWER, ModFoods.AZALEA_FLOWER_CONSUMABLE));

    public static final Supplier<Item> FLOWER_CROWN = regItem("flower_crown", FlowerCrownItem::new,
            () -> new Item.Properties().humanoidArmor(FLOWER_CROWN_MATERIAL, ArmorType.HELMET).durability(64));

    public static final Supplier<Item> MOSS_CLUMP = regItem("moss_clump", p ->
            new MossClumpItem(ModBlocks.MOSS.get(), p), () -> new Item.Properties()
            .food(ModFoods.MOSS_CLUMP, ModFoods.MOSS_CLUMP_CONSUMABLE));

    public static final Supplier<Item> GOLDEN_MOSS_CLUMP = regItem("golden_moss_clump", Item::new,
            () -> new Item.Properties().food(ModFoods.GOLDEN_MOSS_CLUMP, ModFoods.GOLDEN_MOSS_CLUMP_CONSUMABLE));

    public static final Supplier<Item> ENCHANTED_GOLDEN_MOSS_CLUMP = regItem("enchanted_golden_moss_clump",
            EnchantedGoldenMossClumpItem::new, () -> new Item.Properties().rarity(Rarity.EPIC)
                    .food(ModFoods.ENCHANTED_GOLDEN_MOSS_CLUMP, ModFoods.ENCHANTED_GOLDEN_MOSS_CLUMP_CONSUMABLE));

    //bark

    public static final Map<WoodType, Item> BARK = new LinkedHashMap<>();

    public static final Supplier<Item> TALLOW = regItem("tallow", HoneycombItem::new, Item.Properties::new);

    public static final Supplier<Item> STEEL_WOOL = regItem("steel_wool", Item::new,
            () -> new Item.Properties().durability(128));

    public static final Supplier<Item> ICE_SICKLE = regItem("ice_sickle", IceSickleItem::new,
            () -> new Item.Properties().sword(ICICLE_TOOL_MATERIAL, 5, -1f)
                    .food(ModFoods.ICICLE, ModFoods.ICICLE_CONSUMABLE));

    public static final Supplier<Item> THIN_ICE_ITEM = regItem("thin_ice", p ->
            new ThinIceItem(ModBlocks.THIN_ICE.get(), p), () -> new Item.Properties().useBlockDescriptionPrefix());

    public static final Supplier<Item> FROST_ITEM = regItem("frost", p ->
            new FrostItem(ModBlocks.FROST.get(), p), () -> new Item.Properties().useBlockDescriptionPrefix());

    public static final Supplier<Item> FIRE = regItem("fire", p ->
            new BlockItem(Blocks.FIRE, p), () -> new Item.Properties().stacksTo(1));
    public static final Supplier<Item> SOUL_FIRE = regItem("soul_fire", p ->
            new BlockItem(Blocks.SOUL_FIRE, p), () -> new Item.Properties().stacksTo(1));


    private static void registerBark(Registrator<Item> event) {
        for (WoodType type : WoodTypeRegistry.INSTANCE.getValues()) {
            String name = !type.canBurn() ? type.getVariantId("scales", false) : type.getVariantId("bark", false);

            Item item = new WoodBasedItem(new Item.Properties().setId(key(name)), type);
            event.register(ImmersiveWeathering.res(name), item);
            BARK.put(type, item);
            type.addChild("immersive_weathering:bark", item);
            if (type.canBurn()) ModBlocks.FUELS.put(() -> item, 200);
        }
    }

    private static void registerLeafPilesItems(Registrator<Item> event) {
        for (var e : ModBlocks.LEAF_PILES.entrySet()) {
            LeavesType type = e.getKey();
            LeafPileBlock b = e.getValue();
            Identifier id = Utils.getID(b);
            BlockItem i = new LeafPileBlockItem(b, new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, id)).useBlockDescriptionPrefix());
            event.register(id, i);
            LEAF_PILES.put(type, i);
        }
    }
}
