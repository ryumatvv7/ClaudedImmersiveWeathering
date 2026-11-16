package com.ordana.immersive_weathering.configs;

import com.ordana.immersive_weathering.ImmersiveWeathering;
import com.ordana.immersive_weathering.platform.config.ConfigBuilder;
import com.ordana.immersive_weathering.platform.config.ModConfigHolder;
import com.ordana.immersive_weathering.platform.config.ConfigType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CommonConfigs {


    public static final ModConfigHolder SERVER_SPEC;

    public static final Supplier<Boolean> BLOCK_GROWTHS;
    public static final Supplier<List<String>> DISABLED_GROWTHS;
    public static final Supplier<Boolean> CREATIVE_TAB;
    public static final Supplier<Boolean> CREATIVE_DROP;
    public static final Supplier<Boolean> DEBUG_RESOURCES;

    public static final Supplier<Double> MOSS_INTERESTS_FOR_FACE;
    public static final Supplier<Double> MOSS_PATCHINESS;
    public static final Supplier<Double> MOSS_IMMUNE_CHANCE;
    public static final Supplier<Boolean> MOSS_NEEDS_AIR;
    public static final Supplier<Boolean> MOSS_SPREADING_ENABLED;


    public static final Supplier<Boolean> CRACKING_DROPS_BRICK;
    public static final Supplier<Double> CRACK_INTERESTS_FOR_FACE;
    public static final Supplier<Double> CRACK_PATCHINESS;
    public static final Supplier<Double> CRACK_IMMUNE_CHANCE;
    public static final Supplier<Boolean> CRACK_NEEDS_AIR;
    public static final Supplier<Boolean> CRACK_SPREADING_ENABLED;


    public static final Supplier<Boolean> FALLING_ICICLES;
    public static final Supplier<Integer> ICICLE_RARITY;
    public static final Supplier<Boolean> DISABLE_ICICLES;

    public static final Supplier<Integer> FREEZING_ICICLE_SEVERITY;

    public static final Supplier<Double> FIRE_CHARS_WOOD_CHANCE;
    public static final Supplier<Boolean> SOOT_SPAWN;
    public static final Supplier<Boolean> FLAMMABLE_COBWEBS;


    public static final Supplier<Boolean> PICKAXE_CRACKING;
    public static final Supplier<Boolean> PICKAXE_CRACKING_SHIFT;
    public static final Supplier<Boolean> BRICK_REPAIRING;
    public static final Supplier<Boolean> AZALEA_SHEARING;
    public static final Supplier<Boolean> MOSS_SHEARING;
    public static final Supplier<Boolean> MOSS_BURNING;
    public static final Supplier<Boolean> SHOVEL_EXTINGUISH;
    public static final Supplier<Boolean> SPONGE_RUSTING;
    public static final Supplier<Boolean> SPONGE_RUST_DRYING;
    public static final Supplier<Boolean> AXE_STRIPPING;
    public static final Supplier<Boolean> AXE_SCRAPING;


    public static final Supplier<Boolean> ICICLE_FOOD;
    public static final Supplier<Boolean> ICICLE_FIRE_RESISTANCE;

    public static final Supplier<Boolean> COMPOSTER_DIRT;
    public static final Supplier<Boolean> LEGGINGS_PREVENTS_THORN_DAMAGE;
    public static final Supplier<String> GENERIC_BARK;
    public static final Supplier<Boolean> GRASS_OVER_MYCELIUM;
    public static final Supplier<Boolean> MYCELIUM_OVER_GRASS;

    public static final Supplier<Boolean> LEAF_PILES_SLOW;
    public static final Supplier<Double> LEAF_PILES_FROM_DECAY_CHANCE;
    public static final Supplier<Integer> LEAF_PILE_MAX_HEIGHT;
    public static final Supplier<Integer> LEAF_PILES_REACH;
    public static final Supplier<List<String>> LEAF_PILES_BLACKLIST;


    public static final Supplier<Boolean> THIN_ICE_MELTING;
    public static final Supplier<Boolean> PUDDLES;
    public static final Supplier<Boolean> WOOD_ROT;
    public static final Supplier<Boolean> WOOD_MOSS;
    public static final Supplier<Double> WOOD_SPEED;

    public static final Supplier<Boolean> VITRIFIED_LIGHTNING;
    public static final Supplier<Double> FULGURITE_CHANCE;


    public static final Supplier<Boolean> RUSTING;
    public static final Supplier<Integer> RUSTING_INFLUENCE_RADIUS;
    public static final Supplier<Double> RUSTING_RATE;
    public static final Supplier<Boolean> RUST_STREAKING;

    public static final Supplier<Double> GROWTH_SPEED;
    public static final Supplier<Double> GROWTH_VEGETATION;
    public static final Supplier<Double> GROWTH_FROST;
    public static final Supplier<Double> GROWTH_FIRE;
    public static final Supplier<Double> GROWTH_SAND;
    public static final Supplier<Double> GROWTH_CORAL;
    public static final Supplier<Double> GROWTH_LIGHTNING;
    public static final Supplier<Double> GROWTH_LEAVES;
    public static final Supplier<Double> GROWTH_NETHER;
    public static final Supplier<Double> GROWTH_WEEDS;
    public static final Supplier<Double> GROWTH_OTHER;
    public static final Supplier<Double> MOSS_SPEED;
    public static final Supplier<Double> CRACK_SPEED;
    public static final Supplier<Double> RUST_SPEED;

    public static final Supplier<Boolean> ASH_ENABLED;
    public static final Supplier<Double> ASH_FROM_BURNED_BLOCK;
    public static final Supplier<Double> ASH_FROM_EXPIRED_FIRE;
    public static final Supplier<Integer> ASH_MAX_LAYERS;
    public static final Supplier<Double> ASH_RAIN_WASH_CHANCE;
    public static final Supplier<Boolean> ASH_FERTILIZER;
    public static final Supplier<Boolean> ASH_KILLS_GRASS;


    public static void init() {
    }

    static{
        ConfigBuilder builder = ConfigBuilder.create(ImmersiveWeathering.res("common"), ConfigType.COMMON_SYNCED);

        builder.push("general");
        BLOCK_GROWTHS = builder.define("block_growths", true);
        DISABLED_GROWTHS = builder.comment("put here the name of a block growth json you want to disable i.e: [weeds, weeds_spread]." +
                        "Note that this is not the preferred way to do this as block growths are all data driven so it would be best to disable or tweak them by creating a datapack that overrides them" +
                        "Check the mod data folder for the required names. Requires resource reload (/data reload)")
                .define("block_growth_blacklist", new ArrayList<>());
        CREATIVE_TAB = builder.define("creative_tab", false);
        CREATIVE_DROP = builder.comment("Drop stuff when in creative").define("drop_in_creative", false);
        DEBUG_RESOURCES = builder.comment("Save generated resources to disk in a 'debug' folder in your game directory. Mainly for debug purposes but can be used to generate assets in all wood types for your mods :0")
                .define("debug_save_dynamic_pack", false);

        builder.pop();

        builder.push("mossy_blocks");
        MOSS_INTERESTS_FOR_FACE = builder.define("interest_for_face", 0.3, 0, 1d);
        MOSS_PATCHINESS = builder.define("patchiness", 0.5, 0, 1);
        MOSS_IMMUNE_CHANCE = builder.define("immune_chance", 0.4, 0, 1);
        MOSS_NEEDS_AIR = builder.define("needs_air", true);
        MOSS_SPREADING_ENABLED = builder.define("enabled", true);
        builder.pop();

        builder.push("cracked_blocks");
        CRACKING_DROPS_BRICK = builder.define("cracking_drops_brick", false);
        CRACK_INTERESTS_FOR_FACE = builder.define("interest_for_face", 0.6, 0, 1d);
        CRACK_PATCHINESS = builder.define("patchiness", 0.4, 0, 1);
        CRACK_IMMUNE_CHANCE = builder.define("immune_chance", 0.4, 0, 1);
        CRACK_NEEDS_AIR = builder.define("needs_air", false);
        CRACK_SPREADING_ENABLED = builder.define("enabled", true);

        builder.pop();

        builder.push("icicle");
        FALLING_ICICLES = builder.define("react_to_vibrations", true);
        ICICLE_RARITY = builder.define("spawn_rarity", 12, 1, 100);
        DISABLE_ICICLES = builder.define("disable_icicles", false);
        builder.pop();

        builder.push("freezing");
        //all these are disabled when at 0 of course
        FREEZING_ICICLE_SEVERITY = builder.define("icicle", 300, 0, 1000);
        builder.pop();

        builder.push("charring");
        FIRE_CHARS_WOOD_CHANCE = builder.define("fire_chars_wood", 0.3, 0, 1);
        SOOT_SPAWN = builder.define("soot_spawn", true);
        FLAMMABLE_COBWEBS = builder.define("flammable_cobweb", true);
        builder.pop();


        builder.push("item_interaction");
        PICKAXE_CRACKING = builder.define("pickaxe_cracking", true);
        PICKAXE_CRACKING_SHIFT = builder.define("pickaxe_cracking_shift", false);
        BRICK_REPAIRING = builder.define("brick_breaking", true);
        AZALEA_SHEARING = builder.define("azalea_shearing", true);
        MOSS_SHEARING = builder.define("moss_shearing", true);
        MOSS_BURNING = builder.define("moss_burning", true);
        SHOVEL_EXTINGUISH = builder.define("shovel_extinguish", true);
        SPONGE_RUSTING = builder.define("sponge_rusting", true);
        SPONGE_RUST_DRYING = builder.define("sponge_rust_drying", false);
        AXE_STRIPPING = builder.define("axe_stripping", true);
        AXE_SCRAPING = builder.define("axe_rusting", true);
        builder.pop();

        builder.push("food");
        ICICLE_FOOD = builder.define("icicle_food", true);
        ICICLE_FIRE_RESISTANCE = builder.define("icicle_fire_resistance", true);
        builder.pop();

        builder.push("misc");
        COMPOSTER_DIRT = builder.define("composter_dirt", true);
        GRASS_OVER_MYCELIUM = builder.define("grass_over_mycelium", true);
        MYCELIUM_OVER_GRASS = builder.define("mycelium_over_grass", true);
        GENERIC_BARK = builder.define("generic_bark", "");
        LEGGINGS_PREVENTS_THORN_DAMAGE = builder.define("leggings_prevents_thorn_damage", true);
        builder.pop();

        builder.push("leaf_piles");
        LEAF_PILES_SLOW = builder.define("leaf_piles_slow", true);
        LEAF_PILES_FROM_DECAY_CHANCE = builder.define("spawn_entity_from_decay", 0.3, 0, 1);

        LEAF_PILES_REACH = builder.define("reach", 12, 1, 256);
        LEAF_PILE_MAX_HEIGHT = builder.define("max_pile_height", 3, 1, 8);
        LEAF_PILES_BLACKLIST = builder.comment("leaves that wont spawn leaf piles").define("leaf_piles_blacklist", List.of());
        builder.pop();

        builder.push("wood");
        WOOD_ROT = builder.comment("wood in or next to water rots; rotten wood under water falls apart").define("rot", true);
        WOOD_MOSS = builder.comment("moss grows on damp wood").define("moss", true);
        builder.pop();

        builder.push("puddles");
        PUDDLES = builder.comment("puddles appear on the ground while it rains and dry up afterwards").define("enabled", true);
        builder.pop();

        builder.push("thin_ice");
        THIN_ICE_MELTING = builder.define("natural_melting", false);
        builder.pop();

        builder.push("lightning_growths"); //TODO:move to data
        VITRIFIED_LIGHTNING = builder.define("vitrified_lightning", true);
        FULGURITE_CHANCE = builder.comment("chance that a lightning strike on sand creates fulgurite")
                .define("fulgurite_chance", 0.4, 0, 1);
        builder.pop();

        builder.push("rusting");
        RUSTING = builder.define("rusting", true);
        RUSTING_INFLUENCE_RADIUS = builder.define("rusting_influence_radius", 4, 1, 8);
        RUSTING_RATE = builder.comment("base rusting chance per random tick (higher = faster rust)").define("rusting_rate", 0.06, 0, 1);
        RUST_STREAKING = builder.define("rust_streaking", true);
        builder.pop();


        builder.comment("Speed multipliers for all random mechanics (block growths). 1 = original speed, 2 = twice as fast, 0 = off. Final speed = global * category")
                .push("speed");
        GROWTH_SPEED = builder.comment("global multiplier for every block growth").define("global", 1.0, 0, 100);
        GROWTH_VEGETATION = builder.comment("grass, saplings, roots, rooted dirt, podzol, mycelium, tall grass, ferns, seagrass").define("vegetation", 1.0, 0, 100);
        GROWTH_FROST = builder.comment("frost, frosty glass/grass, thin ice, icicles, snow storms").define("frost_and_ice", 1.0, 0, 100);
        GROWTH_FIRE = builder.comment("soot from fire and campfires, fire burning grass, magma").define("fire_and_soot", 1.0, 0, 100);
        GROWTH_SAND = builder.comment("sand layers, sandstone erosion, sand storms").define("sand", 1.0, 0, 100);
        GROWTH_CORAL = builder.define("coral", 1.0, 0, 100);
        GROWTH_LIGHTNING = builder.comment("lightning effects (only multipliers <= 1 make sense)").define("lightning", 1.0, 0, 100);
        GROWTH_LEAVES = builder.comment("leaf piles falling from leaves").define("leaf_piles", 1.0, 0, 100);
        GROWTH_NETHER = builder.comment("nylium, wart vines, nether saplings").define("nether", 1.0, 0, 100);
        GROWTH_WEEDS = builder.define("weeds", 1.0, 0, 100);
        GROWTH_OTHER = builder.comment("everything else, including growths from datapacks").define("other", 1.0, 0, 100);
        MOSS_SPEED = builder.comment("moss spreading on stone / bricks").define("moss", 1.0, 0, 100);
        CRACK_SPEED = builder.comment("cracks spreading on bricks").define("cracks", 1.0, 0, 100);
        WOOD_SPEED = builder.comment("wood rotting and moss on wood").define("wood", 1.0, 0, 100);
        RUST_SPEED = builder.comment("iron rusting (each rust stage)").define("rust", 1.0, 0, 100);
        builder.pop();

        builder.push("ash");
        ASH_ENABLED = builder.define("enabled", true);
        ASH_FROM_BURNED_BLOCK = builder.comment("chance that a block destroyed by fire (and not charred) leaves an ash layer").define("from_burned_block", 0.5, 0, 1);
        ASH_FROM_EXPIRED_FIRE = builder.comment("chance that fire going out leaves an ash layer").define("from_expired_fire", 0.3, 0, 1);
        ASH_MAX_LAYERS = builder.comment("max ash layers created by one burned block").define("max_layers", 2, 1, 8);
        ASH_RAIN_WASH_CHANCE = builder.comment("chance per random tick that rain washes one ash layer away").define("rain_wash_chance", 0.3, 0, 1);
        ASH_FERTILIZER = builder.comment("ash works like bone meal").define("ash_fertilizer", true);
        ASH_KILLS_GRASS = builder.comment("burning on grass turns it into dirt under the ash").define("burns_grass", true);
        builder.pop();

        SERVER_SPEC = builder.build();
        SERVER_SPEC.forceLoad();
    }


    //stuff belows represents the configs that need to be added


    //moss


    // todo: LEAF_PILES_BLACKLIST tag


    //leaf piles


    public static LeafPileMode fallenLeafPiles() {
        throw new AssertionError();
    }

    public enum LeafPileMode {
        LEAF_LAYER, SIMPLE, OFF
    }


    //TODO: fix campfire soot onto non burnable
    //fire stuff


    public static boolean vitrifiedSand() {
        throw new AssertionError();
    }


    public static boolean fulgurite() {
        throw new AssertionError();
    }

    //frost


    public static boolean naturalIceMelt() { //?
        throw new AssertionError();
    }

    public static boolean iciclePlacement() { //?
        throw new AssertionError();
    }

    //food

    public static boolean VITRIFIED_LAVA;

    //here are forge old config values. only here for their old descriptionKey


}
