package com.ordana.immersive_weathering.reg;

import com.ordana.immersive_weathering.ImmersiveWeathering;
import com.ordana.immersive_weathering.blocks.*;
import com.ordana.immersive_weathering.blocks.charred.*;
import com.ordana.immersive_weathering.blocks.cracked.*;
import com.ordana.immersive_weathering.blocks.frosted.FrostBlock;
import com.ordana.immersive_weathering.blocks.frosted.FrostyGlassBlock;
import com.ordana.immersive_weathering.blocks.frosted.FrostyGlassPaneBlock;
import com.ordana.immersive_weathering.blocks.frosted.FrostyGrassBlock;
import com.ordana.immersive_weathering.blocks.mossy.*;
import com.ordana.immersive_weathering.blocks.rusty.*;
import com.ordana.immersive_weathering.blocks.sandy.SandyBlock;
import com.ordana.immersive_weathering.blocks.sandy.SandySlabBlock;
import com.ordana.immersive_weathering.blocks.sandy.SandyStairsBlock;
import com.ordana.immersive_weathering.blocks.sandy.SandyWallBlock;
import com.ordana.immersive_weathering.blocks.snowy.SnowyBlock;
import com.ordana.immersive_weathering.blocks.snowy.SnowySlabBlock;
import com.ordana.immersive_weathering.blocks.snowy.SnowyStairsBlock;
import com.ordana.immersive_weathering.blocks.snowy.SnowyWallBlock;
import com.ordana.immersive_weathering.blocks.soil_types.*;
import net.mehvahdjukaar.moonlight.api.block.ModStairBlock;
import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.leaves.LeavesType;
import net.mehvahdjukaar.moonlight.api.set.leaves.LeavesTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ItemLike;
import java.util.function.Function;
import net.mehvahdjukaar.moonlight.api.set.leaves.VanillaLeavesTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

@SuppressWarnings("unused")
public class ModBlocks {

    public static void init() {
        BlockSetAPI.addDynamicRegistration(ImmersiveWeathering.MOD_ID, ModBlocks::registerLeafPiles, BuiltInRegistries.BLOCK);
    }

    //item -> burn time, applied through the Fabric fuel registry in the main class
    public static final Map<Supplier<? extends ItemLike>, Integer> FUELS = new LinkedHashMap<>();

    private static boolean always(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        return true;
    }

    public static <T extends Block> Supplier<T> regBlock(String name, Function<Properties, T> factory, Supplier<Properties> properties) {
        return RegHelper.registerBlock(ImmersiveWeathering.res(name), factory, properties);
    }

    public static <T extends Block> Supplier<T> regWithItem(String name, Function<Properties, T> factory, Supplier<Properties> properties) {
        Supplier<T> block = regBlock(name, factory, properties);
        regBlockItem(name, block, new Item.Properties());
        return block;
    }

    public static <T extends Block> Supplier<T> regWithBurnableItem(String name, Function<Properties, T> factory, Supplier<Properties> properties, Supplier<Integer> time) {
        Supplier<T> block = regBlock(name, factory, properties);
        FUELS.put(regBlockItem(name, block, new Item.Properties()), time.get());
        return block;
    }

    public static Supplier<BlockItem> regBlockItem(String name, Supplier<? extends Block> blockSup, Item.Properties properties) {
        return RegHelper.registerBlockItem(ImmersiveWeathering.res(name), blockSup, properties);
    }

    //predicates

    private static ToIntFunction<BlockState> litLightLevel(int litLevel) {
        return (state) -> state.getValue(ModBlockProperties.SMOLDERING) ? litLevel : 0;
    }

    private static ToIntFunction<BlockState> moltenLightLevel(int litLevel) {
        return (state) -> state.getValue(ModBlockProperties.MOLTEN) ? litLevel : 0;
    }

    private static final BlockBehaviour.StateArgumentPredicate<EntityType<?>> CAN_SPAWN_ON_LEAVES = (a, b, c, t) ->
        t == EntityType.OCELOT || t == EntityType.PARROT;

    private static final BlockBehaviour.StatePredicate NEVER = (s, w, p) -> false;


    public static Properties leafPileProperties() {
        return Properties.of()
        .randomTicks().instabreak().sound(SoundType.GRASS)
        .noOcclusion().isValidSpawn(CAN_SPAWN_ON_LEAVES)
        .isSuffocating(NEVER).isViewBlocking(NEVER);
    }


    public static final Map<LeavesType, LeafPileBlock> LEAF_PILES = new LinkedHashMap<>();

    public static final Supplier<LeafPileBlock> AZALEA_FLOWER_PILE = regBlock("azalea_flower_pile", p ->
        new LeafPileBlock(p, VanillaLeavesTypes.OAK), () -> leafPileProperties().sound(SoundType.AZALEA));

    //layer stuff

    public static final Supplier<Block> SAND_LAYER_BLOCK = regWithItem("sand_layer_block", p ->
        new SandLayerBlock(14406560, p), () -> Properties.ofFullCopy(Blocks.SAND).strength(0.5f)
            .sound(SoundType.SAND).isSuffocating(NEVER)
            .isViewBlocking((blockState, blockView, blockPos) -> blockState.getValue(LayerBlock.LAYERS_8) >= 8)
            .noOcclusion().requiresCorrectToolForDrops());
    public static final Supplier<Block> RED_SAND_LAYER_BLOCK = regWithItem("red_sand_layer_block", p ->
        new SandLayerBlock(11098145, p), () -> Properties.ofFullCopy(Blocks.RED_SAND).strength(0.5f)
            .sound(SoundType.SAND).isSuffocating(NEVER)
            .isViewBlocking((blockState, blockView, blockPos) -> blockState.getValue(LayerBlock.LAYERS_8) >= 8)
            .noOcclusion().requiresCorrectToolForDrops());


    //vegetation

    public static final Supplier<Block> MOSS = regBlock("moss", p ->
        new MossMultifaceBlock(p), () -> Properties.ofFullCopy(Blocks.MOSS_BLOCK).randomTicks().instabreak().sound(SoundType.MOSS_CARPET).noOcclusion().noCollision());

    public static final Supplier<Block> WEEDS = regWithItem("weeds", p ->
        new WeedsBlock(p), () -> Properties.ofFullCopy(Blocks.SHORT_GRASS).noCollision().instabreak().sound(SoundType.GRASS));

    public static final Supplier<Block> HANGING_ROOTS_WALL = regBlock("hanging_roots_wall", p ->
        new WallRootsBlock(p), () -> Properties.ofFullCopy(Blocks.HANGING_ROOTS));

    public static final Supplier<IvyBlock> IVY = regWithItem("ivy", p ->
        new IvyBlock(p), () -> Properties.ofFullCopy(Blocks.VINE).noCollision().strength(0.2f)
            .sound(SoundType.AZALEA_LEAVES));

    public static final Supplier<Block> DUNE_GRASS = regWithItem("dune_grass", p ->
        new DuneGrassBlock(p), () -> Properties.ofFullCopy(Blocks.SHORT_GRASS).noCollision().instabreak().sound(SoundType.GRASS));


    //mossy blocks

    public static final Supplier<Block> MOSSY_BRICKS = regWithItem("mossy_bricks", p ->
        new MossyBlock(Mossable.MossLevel.MOSSY, p), () -> Properties.ofFullCopy(Blocks.BRICKS)
            .requiresCorrectToolForDrops().strength(2f, 6f));
    public static final Supplier<Block> MOSSY_BRICK_STAIRS = regWithItem("mossy_brick_stairs", p ->
        new MossyStairsBlock(Mossable.MossLevel.MOSSY, MOSSY_BRICKS, p), () -> Properties.ofFullCopy(MOSSY_BRICKS.get()));
    public static final Supplier<Block> MOSSY_BRICK_SLAB = regWithItem("mossy_brick_slab", p ->
        new MossySlabBlock(Mossable.MossLevel.MOSSY, p), () -> Properties.ofFullCopy(MOSSY_BRICKS.get()));
    public static final Supplier<Block> MOSSY_BRICK_WALL = regWithItem("mossy_brick_wall", p ->
        new MossyWallBlock(Mossable.MossLevel.MOSSY, p), () -> Properties.ofFullCopy(MOSSY_BRICKS.get()));

    public static final Supplier<Block> MOSSY_STONE = regWithItem("mossy_stone", p ->
        new MossyBlock(Mossable.MossLevel.MOSSY, p), () -> Properties.ofFullCopy(Blocks.STONE).requiresCorrectToolForDrops().strength(1.5f, 6f));
    public static final Supplier<Block> MOSSY_CHISELED_STONE_BRICKS = regWithItem("mossy_chiseled_stone_bricks", p ->
        new MossyBlock(Mossable.MossLevel.MOSSY, p), () -> Properties.ofFullCopy(MOSSY_STONE.get()));
    public static final Supplier<Block> MOSSY_STONE_STAIRS = regWithItem("mossy_stone_stairs", p ->
        new MossyStairsBlock(Mossable.MossLevel.MOSSY, MOSSY_STONE, p), () -> Properties.ofFullCopy(MOSSY_STONE.get()));
    public static final Supplier<Block> MOSSY_STONE_SLAB = regWithItem("mossy_stone_slab", p ->
        new MossySlabBlock(Mossable.MossLevel.MOSSY, p), () -> Properties.ofFullCopy(MOSSY_STONE.get()));
    public static final Supplier<Block> MOSSY_STONE_WALL = regWithItem("mossy_stone_wall", p ->
        new MossyWallBlock(Mossable.MossLevel.MOSSY, p), () -> Properties.ofFullCopy(Blocks.COBBLESTONE_WALL));


    //snowy blocks

    public static final Supplier<Block> SNOWY_STONE = regWithItem("snowy_stone", p ->
        new SnowyBlock(p), () -> Properties.ofFullCopy(Blocks.STONE).randomTicks());
    public static final Supplier<Block> SNOWY_STONE_STAIRS = regWithItem("snowy_stone_stairs", p ->
        new SnowyStairsBlock(SNOWY_STONE, p), () -> Properties.ofFullCopy(SNOWY_STONE.get()).randomTicks());
    public static final Supplier<Block> SNOWY_STONE_SLAB = regWithItem("snowy_stone_slab", p ->
        new SnowySlabBlock(p), () -> Properties.ofFullCopy(SNOWY_STONE.get()).randomTicks());
    public static final Supplier<Block> SNOWY_STONE_WALL = regWithItem("snowy_stone_wall", p ->
        new SnowyWallBlock(p), () -> Properties.ofFullCopy(Blocks.COBBLESTONE_WALL).randomTicks());

    public static final Supplier<Block> SNOWY_COBBLESTONE = regWithItem("snowy_cobblestone", p ->
        new SnowyBlock(p), () -> Properties.ofFullCopy(Blocks.COBBLESTONE).randomTicks());
    public static final Supplier<Block> SNOWY_COBBLESTONE_STAIRS = regWithItem("snowy_cobblestone_stairs", p ->
        new SnowyStairsBlock(SNOWY_STONE, p), () -> Properties.ofFullCopy(SNOWY_STONE.get()).randomTicks());
    public static final Supplier<Block> SNOWY_COBBLESTONE_SLAB = regWithItem("snowy_cobblestone_slab", p ->
        new SnowySlabBlock(p), () -> Properties.ofFullCopy(SNOWY_STONE.get()).randomTicks());
    public static final Supplier<Block> SNOWY_COBBLESTONE_WALL = regWithItem("snowy_cobblestone_wall", p ->
        new SnowyWallBlock(p), () -> Properties.ofFullCopy(Blocks.COBBLESTONE_WALL).randomTicks());

    public static final Supplier<Block> SNOWY_STONE_BRICKS = regWithItem("snowy_stone_bricks", p ->
        new SnowyBlock(p), () -> Properties.ofFullCopy(Blocks.STONE).randomTicks());
    public static final Supplier<Block> SNOWY_CHISELED_STONE_BRICKS = regWithItem("snowy_chiseled_stone_bricks", p ->
        new SnowyBlock(p), () -> Properties.ofFullCopy(Blocks.STONE).randomTicks());
    public static final Supplier<Block> SNOWY_STONE_BRICK_STAIRS = regWithItem("snowy_stone_brick_stairs", p ->
        new SnowyStairsBlock(SNOWY_STONE, p), () -> Properties.ofFullCopy(SNOWY_STONE.get()).randomTicks());
    public static final Supplier<Block> SNOWY_STONE_BRICK_SLAB = regWithItem("snowy_stone_brick_slab", p ->
        new SnowySlabBlock(p), () -> Properties.ofFullCopy(SNOWY_STONE.get()).randomTicks());
    public static final Supplier<Block> SNOWY_STONE_BRICK_WALL = regWithItem("snowy_stone_brick_wall", p ->
        new SnowyWallBlock(p), () -> Properties.ofFullCopy(Blocks.COBBLESTONE_WALL).randomTicks());

    //sandy blocks

    public static final Supplier<Block> SANDY_STONE = regWithItem("sandy_stone", p ->
        new SandyBlock(p), () -> Properties.ofFullCopy(Blocks.STONE).randomTicks());
    public static final Supplier<Block> SANDY_STONE_STAIRS = regWithItem("sandy_stone_stairs", p ->
        new SandyStairsBlock(SANDY_STONE, p), () -> Properties.ofFullCopy(SANDY_STONE.get()).randomTicks());
    public static final Supplier<Block> SANDY_STONE_SLAB = regWithItem("sandy_stone_slab", p ->
        new SandySlabBlock(p), () -> Properties.ofFullCopy(SANDY_STONE.get()).randomTicks());
    public static final Supplier<Block> SANDY_STONE_WALL = regWithItem("sandy_stone_wall", p ->
        new SandyWallBlock(p), () -> Properties.ofFullCopy(Blocks.COBBLESTONE_WALL).randomTicks());

    public static final Supplier<Block> SANDY_COBBLESTONE = regWithItem("sandy_cobblestone", p ->
        new SandyBlock(p), () -> Properties.ofFullCopy(Blocks.COBBLESTONE).randomTicks());
    public static final Supplier<Block> SANDY_COBBLESTONE_STAIRS = regWithItem("sandy_cobblestone_stairs", p ->
        new SandyStairsBlock(SANDY_STONE, p), () -> Properties.ofFullCopy(SANDY_STONE.get()).randomTicks());
    public static final Supplier<Block> SANDY_COBBLESTONE_SLAB = regWithItem("sandy_cobblestone_slab", p ->
        new SandySlabBlock(p), () -> Properties.ofFullCopy(SANDY_STONE.get()).randomTicks());
    public static final Supplier<Block> SANDY_COBBLESTONE_WALL = regWithItem("sandy_cobblestone_wall", p ->
        new SandyWallBlock(p), () -> Properties.ofFullCopy(Blocks.COBBLESTONE_WALL).randomTicks());

    public static final Supplier<Block> SANDY_STONE_BRICKS = regWithItem("sandy_stone_bricks", p ->
        new SandyBlock(p), () -> Properties.ofFullCopy(Blocks.STONE).randomTicks());
    public static final Supplier<Block> SANDY_CHISELED_STONE_BRICKS = regWithItem("sandy_chiseled_stone_bricks", p ->
        new SandyBlock(p), () -> Properties.ofFullCopy(Blocks.STONE).randomTicks());
    public static final Supplier<Block> SANDY_STONE_BRICK_STAIRS = regWithItem("sandy_stone_brick_stairs", p ->
        new SandyStairsBlock(SANDY_STONE, p), () -> Properties.ofFullCopy(SANDY_STONE.get()).randomTicks());
    public static final Supplier<Block> SANDY_STONE_BRICK_SLAB = regWithItem("sandy_stone_brick_slab", p ->
        new SandySlabBlock(p), () -> Properties.ofFullCopy(SANDY_STONE.get()).randomTicks());
    public static final Supplier<Block> SANDY_STONE_BRICK_WALL = regWithItem("sandy_stone_brick_wall", p ->
        new SandyWallBlock(p), () -> Properties.ofFullCopy(Blocks.COBBLESTONE_WALL).randomTicks());

    //cracked blocks

    public static final Supplier<Block> CRACKED_BRICKS = regWithItem("cracked_bricks", p ->
        new CrackedBlock(Crackable.CrackLevel.CRACKED, () -> Items.BRICK,
            p), () -> Properties.ofFullCopy(Blocks.BRICKS).requiresCorrectToolForDrops().strength(2f, 6f));
    public static final Supplier<Block> CRACKED_BRICK_STAIRS = regWithItem("cracked_brick_stairs", p ->
        new CrackedStairsBlock(Crackable.CrackLevel.CRACKED, CRACKED_BRICKS, () -> Items.BRICK,
            p), () -> Properties.ofFullCopy(CRACKED_BRICKS.get()));
    public static final Supplier<Block> CRACKED_BRICK_SLAB = regWithItem("cracked_brick_slab", p ->
        new CrackedSlabBlock(Crackable.CrackLevel.CRACKED, () -> Items.BRICK,
            p), () -> Properties.ofFullCopy(CRACKED_BRICKS.get()));
    public static final Supplier<Block> CRACKED_BRICK_WALL = regWithItem("cracked_brick_wall", p ->
        new CrackedWallBlock(Crackable.CrackLevel.CRACKED, () -> Items.BRICK,
            p), () -> Properties.ofFullCopy(CRACKED_BRICKS.get()));

    public static final Supplier<Block> CRACKED_STONE_BRICK_STAIRS = regWithItem("cracked_stone_brick_stairs", p ->
        new CrackedStairsBlock(Crackable.CrackLevel.CRACKED, () -> Blocks.CRACKED_STONE_BRICKS, ModItems.STONE_BRICK,
            p), () -> Properties.ofFullCopy(Blocks.CRACKED_STONE_BRICKS));
    public static final Supplier<Block> CRACKED_STONE_BRICK_SLAB = regWithItem("cracked_stone_brick_slab", p ->
        new CrackedSlabBlock(Crackable.CrackLevel.CRACKED, ModItems.STONE_BRICK,
            p), () -> Properties.ofFullCopy(Blocks.CRACKED_STONE_BRICKS));
    public static final Supplier<Block> CRACKED_STONE_BRICK_WALL = regWithItem("cracked_stone_brick_wall", p ->
        new CrackedWallBlock(Crackable.CrackLevel.CRACKED, ModItems.STONE_BRICK,
            p), () -> Properties.ofFullCopy(Blocks.CRACKED_STONE_BRICKS));

    public static final Supplier<Block> CRACKED_POLISHED_BLACKSTONE_BRICK_STAIRS = regWithItem("cracked_polished_blackstone_brick_stairs", p ->
        new CrackedStairsBlock(Crackable.CrackLevel.CRACKED, () -> Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, ModItems.BLACKSTONE_BRICK,
            p), () -> Properties.ofFullCopy(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS));
    public static final Supplier<Block> CRACKED_POLISHED_BLACKSTONE_BRICK_SLAB = regWithItem("cracked_polished_blackstone_brick_slab", p ->
        new CrackedSlabBlock(Crackable.CrackLevel.CRACKED, ModItems.BLACKSTONE_BRICK,
            p), () -> Properties.ofFullCopy(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS));
    public static final Supplier<Block> CRACKED_POLISHED_BLACKSTONE_BRICK_WALL = regWithItem("cracked_polished_blackstone_brick_wall", p ->
        new CrackedWallBlock(Crackable.CrackLevel.CRACKED, ModItems.BLACKSTONE_BRICK,
            p), () -> Properties.ofFullCopy(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS));

    public static final Supplier<Block> CRACKED_NETHER_BRICK_STAIRS = regWithItem("cracked_nether_brick_stairs", p ->
        new CrackedStairsBlock(Crackable.CrackLevel.CRACKED, () -> Blocks.CRACKED_NETHER_BRICKS, () -> Items.NETHER_BRICK,
            p), () -> Properties.ofFullCopy(Blocks.NETHER_BRICKS));
    public static final Supplier<Block> CRACKED_NETHER_BRICK_SLAB = regWithItem("cracked_nether_brick_slab", p ->
        new CrackedSlabBlock(Crackable.CrackLevel.CRACKED, () -> Items.NETHER_BRICK,
            p), () -> Properties.ofFullCopy(Blocks.NETHER_BRICKS));
    public static final Supplier<Block> CRACKED_NETHER_BRICK_WALL = regWithItem("cracked_nether_brick_wall", p ->
        new CrackedWallBlock(Crackable.CrackLevel.CRACKED, () -> Items.NETHER_BRICK,
            p), () -> Properties.ofFullCopy(Blocks.NETHER_BRICKS));

    public static final Supplier<Block> CRACKED_DEEPSLATE_BRICK_STAIRS = regWithItem("cracked_deepslate_brick_stairs", p ->
        new CrackedStairsBlock(Crackable.CrackLevel.CRACKED, () -> Blocks.CRACKED_DEEPSLATE_BRICKS, ModItems.DEEPSLATE_BRICK,
            p), () -> Properties.ofFullCopy(Blocks.CRACKED_DEEPSLATE_BRICKS));
    public static final Supplier<Block> CRACKED_DEEPSLATE_BRICK_SLAB = regWithItem("cracked_deepslate_brick_slab", p ->
        new CrackedSlabBlock(Crackable.CrackLevel.CRACKED, ModItems.DEEPSLATE_BRICK,
            p), () -> Properties.ofFullCopy(Blocks.CRACKED_DEEPSLATE_BRICKS));
    public static final Supplier<Block> CRACKED_DEEPSLATE_BRICK_WALL = regWithItem("cracked_deepslate_brick_wall", p ->
        new CrackedWallBlock(Crackable.CrackLevel.CRACKED, ModItems.DEEPSLATE_BRICK,
            p), () -> Properties.ofFullCopy(Blocks.CRACKED_DEEPSLATE_BRICKS));

    public static final Supplier<Block> CRACKED_DEEPSLATE_TILE_STAIRS = regWithItem("cracked_deepslate_tile_stairs", p ->
        new CrackedStairsBlock(Crackable.CrackLevel.CRACKED, () -> Blocks.CRACKED_DEEPSLATE_TILES, ModItems.DEEPSLATE_TILE,
            p), () -> Properties.ofFullCopy(Blocks.CRACKED_DEEPSLATE_TILES));
    public static final Supplier<Block> CRACKED_DEEPSLATE_TILE_SLAB = regWithItem("cracked_deepslate_tile_slab", p ->
        new CrackedSlabBlock(Crackable.CrackLevel.CRACKED, ModItems.DEEPSLATE_TILE,
            p), () -> Properties.ofFullCopy(Blocks.CRACKED_DEEPSLATE_TILES));
    public static final Supplier<Block> CRACKED_DEEPSLATE_TILE_WALL = regWithItem("cracked_deepslate_tile_wall", p ->
        new CrackedWallBlock(Crackable.CrackLevel.CRACKED, ModItems.DEEPSLATE_TILE,
            p), () -> Properties.ofFullCopy(Blocks.CRACKED_DEEPSLATE_TILES));

    public static final Supplier<Block> CRACKED_END_STONE_BRICKS = regWithItem("cracked_end_stone_bricks", p ->
        new CrackedBlock(Crackable.CrackLevel.CRACKED, ModItems.END_STONE_BRICK, p), () -> Properties.ofFullCopy(Blocks.END_STONE));
    public static final Supplier<Block> CRACKED_END_STONE_BRICK_STAIRS = regWithItem("cracked_end_stone_brick_stairs", p ->
        new CrackedStairsBlock(Crackable.CrackLevel.CRACKED, CRACKED_END_STONE_BRICKS, ModItems.END_STONE_BRICK, p), () -> Properties.ofFullCopy(Blocks.END_STONE));
    public static final Supplier<Block> CRACKED_END_STONE_BRICK_SLAB = regWithItem("cracked_end_stone_brick_slab", p ->
        new CrackedSlabBlock(Crackable.CrackLevel.CRACKED, ModItems.END_STONE_BRICK, p), () -> Properties.ofFullCopy(Blocks.END_STONE));
    public static final Supplier<Block> CRACKED_END_STONE_BRICK_WALL = regWithItem("cracked_end_stone_brick_wall", p ->
        new CrackedWallBlock(Crackable.CrackLevel.CRACKED, ModItems.END_STONE_BRICK, p), () -> Properties.ofFullCopy(Blocks.END_STONE));


    public static final Supplier<Block> CRACKED_PRISMARINE_BRICKS = regWithItem("cracked_prismarine_bricks", p ->
        new CrackedBlock(Crackable.CrackLevel.CRACKED, ModItems.PRISMARINE_BRICK, p), () -> Properties.ofFullCopy(Blocks.PRISMARINE));
    public static final Supplier<Block> CRACKED_PRISMARINE_BRICK_STAIRS = regWithItem("cracked_prismarine_brick_stairs", p ->
        new CrackedStairsBlock(Crackable.CrackLevel.CRACKED, CRACKED_END_STONE_BRICKS, ModItems.PRISMARINE_BRICK, p), () -> Properties.ofFullCopy(Blocks.PRISMARINE));
    public static final Supplier<Block> CRACKED_PRISMARINE_BRICK_SLAB = regWithItem("cracked_prismarine_brick_slab", p ->
        new CrackedSlabBlock(Crackable.CrackLevel.CRACKED, ModItems.PRISMARINE_BRICK, p), () -> Properties.ofFullCopy(Blocks.PRISMARINE));
    public static final Supplier<Block> CRACKED_PRISMARINE_BRICK_WALL = regWithItem("cracked_prismarine_brick_wall", p ->
        new CrackedWallBlock(Crackable.CrackLevel.CRACKED, ModItems.PRISMARINE_BRICK, p), () -> Properties.ofFullCopy(Blocks.PRISMARINE));



    //vitrified sand

    public static final Supplier<Block> FULGURITE = regWithItem("fulgurite", p ->
        new FulguriteBlock(7, 3, p), () -> Properties.ofFullCopy(Blocks.GLASS)
            .instabreak().lightLevel((s) -> s.getValue(FulguriteBlock.POWERED) ? 5 : 0)
            .dynamicShape().requiresCorrectToolForDrops());

    public static final Supplier<Block> VITRIFIED_SAND = regWithItem("vitrified_sand", p ->
        new TransparentBlock(p), () -> Properties.ofFullCopy(Blocks.GLASS)
            .strength(2f, 6f).sound(SoundType.TUFF)
            .requiresCorrectToolForDrops().noOcclusion().isViewBlocking((s, l, p) -> false));


    //soil blocks

    public static final Supplier<Block> MULCH_BLOCK = regWithItem("mulch_block", p ->
        new MulchBlock(p), () -> Properties.ofFullCopy(Blocks.DIRT).strength(1f, 1f)
            .sound(SoundType.ROOTED_DIRT).randomTicks());
    public static final Supplier<Block> NULCH_BLOCK = regWithItem("nulch_block", p ->
        new NulchBlock(p), () -> Properties.ofFullCopy(Blocks.DIRT).strength(1f, 1f)
            .sound(SoundType.NETHER_WART).lightLevel(moltenLightLevel(10)).randomTicks());

    public static final Supplier<Block> SILT = regWithItem("silt", p ->
        new SiltBlock(p), () -> Properties.ofFullCopy(Blocks.DIRT).strength(0.5f).sound(SoundType.MUD));
    public static final Supplier<Block> GRASSY_SILT = regWithItem("grassy_silt", p ->
        new SiltBlockGrassy(p), () -> Properties.ofFullCopy(Blocks.DIRT).strength(0.5f).sound(SoundType.MUD));
    public static final Supplier<Block> SILTY_FARMLAND = regWithItem("silty_farmland", p ->
        new SiltyFarmlandBlock(p), () -> Properties.ofFullCopy(Blocks.DIRT).strength(0.5f).sound(SoundType.MUD));

    public static final Supplier<Block> SANDY_DIRT = regWithItem("sandy_dirt", p ->
        new SandyDirtBlock(p), () -> Properties.ofFullCopy(Blocks.DIRT).strength(0.5f).sound(SoundType.SAND));
    public static final Supplier<Block> GRASSY_SANDY_DIRT = regWithItem("grassy_sandy_dirt", p ->
        new SandyDirtBlockGrassy(p), () -> Properties.ofFullCopy(Blocks.DIRT).strength(0.5f).sound(SoundType.SAND));
    public static final Supplier<Block> SANDY_FARMLAND = regWithItem("sandy_farmland", p ->
        new SandyFarmlandBlock(p), () -> Properties.ofFullCopy(Blocks.DIRT).strength(0.5f).sound(SoundType.SAND));

    public static final Supplier<Block> EARTHEN_CLAY = regWithItem("earthen_clay", p ->
        new EarthenClayBlock(p), () -> Properties.ofFullCopy(Blocks.DIRT).strength(0.5f).sound(SoundType.BASALT));
    public static final Supplier<Block> GRASSY_EARTHEN_CLAY = regWithItem("grassy_earthen_clay", p ->
        new EarthenClayBlockGrassy(p), () -> Properties.ofFullCopy(Blocks.DIRT).strength(0.5f).sound(SoundType.BASALT));
    public static final Supplier<Block> EARTHEN_CLAY_FARMLAND = regWithItem("earthen_clay_farmland", p ->
        new EarthenClayFarmlandBlock(p), () -> Properties.ofFullCopy(Blocks.DIRT).strength(0.5f).sound(SoundType.BASALT));

    public static final Supplier<Block> LOAM = regWithItem("loam", p ->
        new LoamBlock(p), () -> Properties.ofFullCopy(Blocks.DIRT).strength(0.5f).sound(SoundType.GRAVEL));
    public static final Supplier<Block> LOAMY_FARMLAND = regWithItem("loamy_farmland", p ->
        new LoamyFarmlandBlock(p), () -> Properties.ofFullCopy(Blocks.DIRT).strength(0.5f).sound(SoundType.GRAVEL));

    public static final Supplier<Block> PERMAFROST = regWithItem("permafrost", p ->
        new PermafrostBlock(p), () -> Properties.ofFullCopy(Blocks.DIRT).strength(0.5f).sound(SoundType.CALCITE));
    public static final Supplier<Block> GRASSY_PERMAFROST = regWithItem("grassy_permafrost", p ->
        new PermafrostBlockGrassy(p), () -> Properties.ofFullCopy(Blocks.DIRT).strength(0.5f).sound(SoundType.CALCITE));


    public static final Supplier<Block> ROOTED_GRASS_BLOCK = regWithItem("rooted_grass_block", p ->
        new RootedGrassBlock(p), () -> Properties.ofFullCopy(Blocks.GRASS_BLOCK).randomTicks().strength(0.5F)
            .sound(SoundType.ROOTED_DIRT));


    //frost and ice

    public static final Supplier<Block> ICICLE = regBlock("icicle", p ->
        new IcicleBlock(p), () -> Properties.ofFullCopy(Blocks.ICE).randomTicks().instabreak()
            .sound(SoundType.GLASS).noOcclusion().dynamicShape());
    public static final Supplier<Block> THIN_ICE = regBlock("thin_ice", p ->
        new ThinIceBlock(p), () -> Properties.ofFullCopy(Blocks.ICE)
            .isViewBlocking(NEVER).isSuffocating(NEVER).isViewBlocking(NEVER));

    public static final Supplier<Block> FROST = regBlock("frost", p ->
        new FrostBlock(p), () -> Properties.ofFullCopy(Blocks.SNOW)
            .randomTicks().instabreak().sound(SoundType.POWDER_SNOW).noOcclusion().noCollision());
    public static final Supplier<Block> FROSTY_GRASS = regWithItem("frosty_grass", p ->
        new FrostyGrassBlock(p), () -> Properties.ofFullCopy(Blocks.SHORT_GRASS)
            .randomTicks().sound(SoundType.POWDER_SNOW));
    public static final Supplier<Block> FROSTY_FERN = regWithItem("frosty_fern", p ->
        new FrostyGrassBlock(p), () -> Properties.ofFullCopy(FROSTY_GRASS.get()));
    public static final Supplier<Block> FROSTY_GLASS = regWithItem("frosty_glass", p ->
        new FrostyGlassBlock(p), () -> Properties.ofFullCopy(Blocks.GLASS).randomTicks());
    public static final Supplier<Block> FROSTY_GLASS_PANE = regWithItem("frosty_glass_pane", p ->
        new FrostyGlassPaneBlock(p), () -> Properties.ofFullCopy(Blocks.GLASS_PANE).randomTicks());


    //charred blocks
    public static final Supplier<Block> SOOT = regWithItem("soot", p ->
        new SootBlock(p), () -> Properties.ofFullCopy(Blocks.SCULK_VEIN).noCollision().instabreak().sound(SoundType.SNOW).randomTicks());

    public static final Supplier<Block> CHARRED_LOG = regWithBurnableItem("charred_log", p ->
        new CharredPillarBlock(p), () -> Properties.ofFullCopy(Blocks.BASALT)
            .strength(1.5f, 0.5f).sound(SoundType.BASALT)
            .lightLevel(litLightLevel(5)).randomTicks(), () -> 1600);
    public static final Supplier<Block> CHARRED_PLANKS = regWithBurnableItem("charred_planks", p ->
        new CharredBlock(p), () -> Properties.ofFullCopy(CHARRED_LOG.get()), () -> 400);
    public static final Supplier<Block> CHARRED_SLAB = regWithBurnableItem("charred_slab", p ->
        new CharredSlabBlock(p), () -> Properties.ofFullCopy(CHARRED_LOG.get()), () -> 200);
    public static final Supplier<Block> CHARRED_STAIRS = regWithBurnableItem("charred_stairs", p ->
        new CharredStairsBlock(CHARRED_PLANKS, p), () -> Properties.ofFullCopy(CHARRED_LOG.get()), () -> 200);
    public static final Supplier<Block> CHARRED_FENCE = regWithBurnableItem("charred_fence", p ->
        new CharredFenceBlock(p), () -> Properties.ofFullCopy(CHARRED_LOG.get()), () -> 200);
    public static final Supplier<Block> CHARRED_FENCE_GATE = regWithBurnableItem("charred_fence_gate", p ->
        new CharredFenceGateBlock(p, WoodType.OAK), () -> Properties.ofFullCopy(CHARRED_LOG.get()), () -> 200);


    //cut iron

    public static final Supplier<Block> CUT_IRON = regWithItem("cut_iron", p ->
        new RustableBlock(Rustable.RustLevel.UNAFFECTED, p), () -> Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final Supplier<Block> EXPOSED_CUT_IRON = regWithItem("exposed_cut_iron", p ->
        new RustableBlock(Rustable.RustLevel.EXPOSED, p), () -> Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final Supplier<Block> WEATHERED_CUT_IRON = regWithItem("weathered_cut_iron", p ->
        new RustableBlock(Rustable.RustLevel.WEATHERED, p), () -> Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final Supplier<Block> RUSTED_CUT_IRON = regWithItem("rusted_cut_iron", p ->
        new RustableBlock(Rustable.RustLevel.RUSTED, p), () -> Properties.ofFullCopy(Blocks.IRON_BLOCK));

    public static final Supplier<Block> CUT_IRON_STAIRS = regWithItem("cut_iron_stairs", p ->
        new RustableStairsBlock(Rustable.RustLevel.UNAFFECTED, CUT_IRON, p), () -> Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final Supplier<Block> EXPOSED_CUT_IRON_STAIRS = regWithItem("exposed_cut_iron_stairs", p ->
        new RustableStairsBlock(Rustable.RustLevel.EXPOSED, CUT_IRON, p), () -> Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final Supplier<Block> WEATHERED_CUT_IRON_STAIRS = regWithItem("weathered_cut_iron_stairs", p ->
        new RustableStairsBlock(Rustable.RustLevel.WEATHERED, CUT_IRON, p), () -> Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final Supplier<Block> RUSTED_CUT_IRON_STAIRS = regWithItem("rusted_cut_iron_stairs", p ->
        new RustableStairsBlock(Rustable.RustLevel.RUSTED, CUT_IRON, p), () -> Properties.ofFullCopy(Blocks.IRON_BLOCK));

    public static final Supplier<Block> CUT_IRON_SLAB = regWithItem("cut_iron_slab", p ->
        new RustableSlabBlock(Rustable.RustLevel.UNAFFECTED, p), () -> Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final Supplier<Block> EXPOSED_CUT_IRON_SLAB = regWithItem("exposed_cut_iron_slab", p ->
        new RustableSlabBlock(Rustable.RustLevel.EXPOSED, p), () -> Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final Supplier<Block> WEATHERED_CUT_IRON_SLAB = regWithItem("weathered_cut_iron_slab", p ->
        new RustableSlabBlock(Rustable.RustLevel.WEATHERED, p), () -> Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final Supplier<Block> RUSTED_CUT_IRON_SLAB = regWithItem("rusted_cut_iron_slab", p ->
        new RustableSlabBlock(Rustable.RustLevel.RUSTED, p), () -> Properties.ofFullCopy(Blocks.IRON_BLOCK));

    //waxed
    public static final Supplier<Block> WAXED_CUT_IRON = regWithItem("waxed_cut_iron", p ->
        new Block(p), () -> noTick(Blocks.IRON_BLOCK));
    public static final Supplier<Block> WAXED_EXPOSED_CUT_IRON = regWithItem("waxed_exposed_cut_iron", p ->
        new Block(p), () -> noTick(Blocks.IRON_BLOCK));
    public static final Supplier<Block> WAXED_WEATHERED_CUT_IRON = regWithItem("waxed_weathered_cut_iron", p ->
        new Block(p), () -> noTick(Blocks.IRON_BLOCK));
    public static final Supplier<Block> WAXED_RUSTED_CUT_IRON = regWithItem("waxed_rusted_cut_iron", p ->
        new Block(p), () -> noTick(Blocks.IRON_BLOCK));

    public static final Supplier<Block> WAXED_CUT_IRON_STAIRS = regWithItem("waxed_cut_iron_stairs", p ->
        new ModStairBlock(WAXED_CUT_IRON, p), () -> noTick(Blocks.IRON_BLOCK));
    public static final Supplier<Block> WAXED_EXPOSED_CUT_IRON_STAIRS = regWithItem("waxed_exposed_cut_iron_stairs", p ->
        new ModStairBlock(WAXED_EXPOSED_CUT_IRON, p), () -> noTick(Blocks.IRON_BLOCK));
    public static final Supplier<Block> WAXED_WEATHERED_CUT_IRON_STAIRS = regWithItem("waxed_weathered_cut_iron_stairs", p ->
        new ModStairBlock(WAXED_WEATHERED_CUT_IRON, p), () -> noTick(Blocks.IRON_BLOCK));
    public static final Supplier<Block> WAXED_RUSTED_CUT_IRON_STAIRS = regWithItem("waxed_rusted_cut_iron_stairs", p ->
        new ModStairBlock(WAXED_RUSTED_CUT_IRON, p), () -> noTick(Blocks.IRON_BLOCK));

    public static final Supplier<Block> WAXED_CUT_IRON_SLAB = regWithItem("waxed_cut_iron_slab", p ->
        new SlabBlock(p), () -> noTick(Blocks.IRON_BLOCK));
    public static final Supplier<Block> WAXED_EXPOSED_CUT_IRON_SLAB = regWithItem("waxed_exposed_cut_iron_slab", p ->
        new SlabBlock(p), () -> noTick(Blocks.IRON_BLOCK));
    public static final Supplier<Block> WAXED_WEATHERED_CUT_IRON_SLAB = regWithItem("waxed_weathered_cut_iron_slab", p ->
        new SlabBlock(p), () -> noTick(Blocks.IRON_BLOCK));
    public static final Supplier<Block> WAXED_RUSTED_CUT_IRON_SLAB = regWithItem("waxed_rusted_cut_iron_slab", p ->
        new SlabBlock(p), () -> noTick(Blocks.IRON_BLOCK));

    //plate iron

    public static final Supplier<Block> PLATE_IRON = regWithItem("plate_iron", p ->
        new RustableBlock(Rustable.RustLevel.UNAFFECTED, p), () -> Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final Supplier<Block> EXPOSED_PLATE_IRON = regWithItem("exposed_plate_iron", p ->
        new RustableBlock(Rustable.RustLevel.EXPOSED, p), () -> Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final Supplier<Block> WEATHERED_PLATE_IRON = regWithItem("weathered_plate_iron", p ->
        new RustableBlock(Rustable.RustLevel.WEATHERED, p), () -> Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final Supplier<Block> RUSTED_PLATE_IRON = regWithItem("rusted_plate_iron", p ->
        new RustableBlock(Rustable.RustLevel.RUSTED, p), () -> Properties.ofFullCopy(Blocks.IRON_BLOCK));

    public static final Supplier<Block> PLATE_IRON_STAIRS = regWithItem("plate_iron_stairs", p ->
        new RustableStairsBlock(Rustable.RustLevel.UNAFFECTED, CUT_IRON, p), () -> Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final Supplier<Block> EXPOSED_PLATE_IRON_STAIRS = regWithItem("exposed_plate_iron_stairs", p ->
        new RustableStairsBlock(Rustable.RustLevel.EXPOSED, CUT_IRON, p), () -> Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final Supplier<Block> WEATHERED_PLATE_IRON_STAIRS = regWithItem("weathered_plate_iron_stairs", p ->
        new RustableStairsBlock(Rustable.RustLevel.WEATHERED, CUT_IRON, p), () -> Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final Supplier<Block> RUSTED_PLATE_IRON_STAIRS = regWithItem("rusted_plate_iron_stairs", p ->
        new RustableStairsBlock(Rustable.RustLevel.RUSTED, CUT_IRON, p), () -> Properties.ofFullCopy(Blocks.IRON_BLOCK));

    public static final Supplier<Block> PLATE_IRON_SLAB = regWithItem("plate_iron_slab", p ->
        new RustableSlabBlock(Rustable.RustLevel.UNAFFECTED, p), () -> Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final Supplier<Block> EXPOSED_PLATE_IRON_SLAB = regWithItem("exposed_plate_iron_slab", p ->
        new RustableSlabBlock(Rustable.RustLevel.EXPOSED, p), () -> Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final Supplier<Block> WEATHERED_PLATE_IRON_SLAB = regWithItem("weathered_plate_iron_slab", p ->
        new RustableSlabBlock(Rustable.RustLevel.WEATHERED, p), () -> Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final Supplier<Block> RUSTED_PLATE_IRON_SLAB = regWithItem("rusted_plate_iron_slab", p ->
        new RustableSlabBlock(Rustable.RustLevel.RUSTED, p), () -> Properties.ofFullCopy(Blocks.IRON_BLOCK));

    //waxed
    public static final Supplier<Block> WAXED_PLATE_IRON = regWithItem("waxed_plate_iron", p ->
        new Block(p), () -> noTick(Blocks.IRON_BLOCK));
    public static final Supplier<Block> WAXED_EXPOSED_PLATE_IRON = regWithItem("waxed_exposed_plate_iron", p ->
        new Block(p), () -> noTick(Blocks.IRON_BLOCK));
    public static final Supplier<Block> WAXED_WEATHERED_PLATE_IRON = regWithItem("waxed_weathered_plate_iron", p ->
        new Block(p), () -> noTick(Blocks.IRON_BLOCK));
    public static final Supplier<Block> WAXED_RUSTED_PLATE_IRON = regWithItem("waxed_rusted_plate_iron", p ->
        new Block(p), () -> noTick(Blocks.IRON_BLOCK));

    public static final Supplier<Block> WAXED_PLATE_IRON_STAIRS = regWithItem("waxed_plate_iron_stairs", p ->
        new ModStairBlock(WAXED_PLATE_IRON, p), () -> noTick(Blocks.IRON_BLOCK));
    public static final Supplier<Block> WAXED_EXPOSED_PLATE_IRON_STAIRS = regWithItem("waxed_exposed_plate_iron_stairs", p ->
        new ModStairBlock(WAXED_EXPOSED_PLATE_IRON, p), () -> noTick(Blocks.IRON_BLOCK));
    public static final Supplier<Block> WAXED_WEATHERED_PLATE_IRON_STAIRS = regWithItem("waxed_weathered_plate_iron_stairs", p ->
        new ModStairBlock(WAXED_WEATHERED_PLATE_IRON, p), () -> noTick(Blocks.IRON_BLOCK));
    public static final Supplier<Block> WAXED_RUSTED_PLATE_IRON_STAIRS = regWithItem("waxed_rusted_plate_iron_stairs", p ->
        new ModStairBlock(WAXED_RUSTED_PLATE_IRON, p), () -> noTick(Blocks.IRON_BLOCK));

    public static final Supplier<Block> WAXED_PLATE_IRON_SLAB = regWithItem("waxed_plate_iron_slab", p ->
        new SlabBlock(p), () -> noTick(Blocks.IRON_BLOCK));
    public static final Supplier<Block> WAXED_EXPOSED_PLATE_IRON_SLAB = regWithItem("waxed_exposed_plate_iron_slab", p ->
        new SlabBlock(p), () -> noTick(Blocks.IRON_BLOCK));
    public static final Supplier<Block> WAXED_WEATHERED_PLATE_IRON_SLAB = regWithItem("waxed_weathered_plate_iron_slab", p ->
        new SlabBlock(p), () -> noTick(Blocks.IRON_BLOCK));
    public static final Supplier<Block> WAXED_RUSTED_PLATE_IRON_SLAB = regWithItem("waxed_rusted_plate_iron_slab", p ->
        new SlabBlock(p), () -> noTick(Blocks.IRON_BLOCK));

    //iron decor

    public static final Supplier<Block> EXPOSED_IRON_DOOR = regWithItem("exposed_iron_door", p ->
        new RustableDoorBlock(Rustable.RustLevel.EXPOSED, p), () -> Properties.ofFullCopy(Blocks.IRON_DOOR));
    public static final Supplier<Block> WEATHERED_IRON_DOOR = regWithItem("weathered_iron_door", p ->
        new RustableDoorBlock(Rustable.RustLevel.WEATHERED, p), () -> Properties.ofFullCopy(Blocks.IRON_DOOR));
    public static final Supplier<Block> RUSTED_IRON_DOOR = regWithItem("rusted_iron_door", p ->
        new RustableDoorBlock(Rustable.RustLevel.RUSTED, p), () -> Properties.ofFullCopy(Blocks.IRON_DOOR));

    public static final Supplier<Block> EXPOSED_IRON_TRAPDOOR = regWithItem("exposed_iron_trapdoor", p ->
        new RustableTrapdoorBlock(Rustable.RustLevel.EXPOSED, p), () -> Properties.ofFullCopy(Blocks.IRON_TRAPDOOR));
    public static final Supplier<Block> WEATHERED_IRON_TRAPDOOR = regWithItem("weathered_iron_trapdoor", p ->
        new RustableTrapdoorBlock(Rustable.RustLevel.WEATHERED, p), () -> Properties.ofFullCopy(Blocks.IRON_TRAPDOOR));
    public static final Supplier<Block> RUSTED_IRON_TRAPDOOR = regWithItem("rusted_iron_trapdoor", p ->
        new RustableTrapdoorBlock(Rustable.RustLevel.RUSTED, p), () -> Properties.ofFullCopy(Blocks.IRON_TRAPDOOR));

    public static final Supplier<Block> EXPOSED_IRON_BARS = regWithItem("exposed_iron_bars", p ->
        new RustableBarsBlock(Rustable.RustLevel.EXPOSED, p), () -> Properties.ofFullCopy(Blocks.IRON_BARS));
    public static final Supplier<Block> WEATHERED_IRON_BARS = regWithItem("weathered_iron_bars", p ->
        new RustableBarsBlock(Rustable.RustLevel.WEATHERED, p), () -> Properties.ofFullCopy(Blocks.IRON_BARS));
    public static final Supplier<Block> RUSTED_IRON_BARS = regWithItem("rusted_iron_bars", p ->
        new RustableBarsBlock(Rustable.RustLevel.RUSTED, p), () -> Properties.ofFullCopy(Blocks.IRON_BARS));

    //waxed
    public static final Supplier<Block> WAXED_IRON_DOOR = regWithItem("waxed_iron_door", p ->
        new RustAffectedDoorBlock(Rustable.RustLevel.UNAFFECTED, p), () -> noTick(Blocks.IRON_DOOR));
    public static final Supplier<Block> WAXED_EXPOSED_IRON_DOOR = regWithItem("waxed_exposed_iron_door", p ->
        new RustAffectedDoorBlock(Rustable.RustLevel.EXPOSED, p), () -> noTick(Blocks.IRON_DOOR));
    public static final Supplier<Block> WAXED_WEATHERED_IRON_DOOR = regWithItem("waxed_weathered_iron_door", p ->
        new RustAffectedDoorBlock(Rustable.RustLevel.WEATHERED, p), () -> noTick(Blocks.IRON_DOOR));
    public static final Supplier<Block> WAXED_RUSTED_IRON_DOOR = regWithItem("waxed_rusted_iron_door", p ->
        new RustAffectedDoorBlock(Rustable.RustLevel.RUSTED, p), () -> noTick(Blocks.IRON_DOOR));

    public static final Supplier<Block> WAXED_IRON_TRAPDOOR = regWithItem("waxed_iron_trapdoor", p ->
        new RustAffectedTrapdoorBlock(Rustable.RustLevel.UNAFFECTED, p,BlockSetType.IRON), () -> noTick(Blocks.IRON_TRAPDOOR));
    public static final Supplier<Block> WAXED_EXPOSED_IRON_TRAPDOOR = regWithItem("waxed_exposed_iron_trapdoor", p ->
        new RustAffectedTrapdoorBlock(Rustable.RustLevel.EXPOSED, p, BlockSetType.IRON), () -> noTick(Blocks.IRON_TRAPDOOR));
    public static final Supplier<Block> WAXED_WEATHERED_IRON_TRAPDOOR = regWithItem("waxed_weathered_iron_trapdoor", p ->
        new RustAffectedTrapdoorBlock(Rustable.RustLevel.WEATHERED, p, BlockSetType.IRON), () -> noTick(Blocks.IRON_TRAPDOOR));
    public static final Supplier<Block> WAXED_RUSTED_IRON_TRAPDOOR = regWithItem("waxed_rusted_iron_trapdoor", p ->
        new RustAffectedTrapdoorBlock(Rustable.RustLevel.RUSTED, p, BlockSetType.IRON), () -> noTick(Blocks.IRON_TRAPDOOR));

    public static final Supplier<Block> WAXED_IRON_BARS = regWithItem("waxed_iron_bars", p ->
        new IronBarsBlock(p), () -> noTick(Blocks.IRON_BARS));
    public static final Supplier<Block> WAXED_EXPOSED_IRON_BARS = regWithItem("waxed_exposed_iron_bars", p ->
        new IronBarsBlock(p), () -> noTick(Blocks.IRON_BARS));
    public static final Supplier<Block> WAXED_WEATHERED_IRON_BARS = regWithItem("waxed_weathered_iron_bars", p ->
        new IronBarsBlock(p), () -> noTick(Blocks.IRON_BARS));
    public static final Supplier<Block> WAXED_RUSTED_IRON_BARS = regWithItem("waxed_rusted_iron_bars", p ->
        new IronBarsBlock(p), () -> noTick(Blocks.IRON_BARS));


    //vanilla completion blocks
    public static final Supplier<Block> STONE_WALL = regWithItem("stone_wall", p ->
        new MossableWallBlock(Mossable.MossLevel.UNAFFECTED, p), () -> Properties.ofFullCopy(Blocks.COBBLESTONE_WALL));

    public static final Supplier<Block> CHISELED_PRISMARINE_BRICKS = regWithItem("chiseled_prismarine_bricks", p ->
        new Block(p), () -> Properties.ofFullCopy(Blocks.PRISMARINE));

    public static final Supplier<Block> PRISMARINE_BRICK_WALL = regWithItem("prismarine_brick_wall", p ->
        new CrackedWallBlock(Crackable.CrackLevel.UNCRACKED, ModItems.PRISMARINE_BRICK, p), () -> Properties.ofFullCopy(Blocks.PRISMARINE));

    public static final Supplier<Block> DARK_PRISMARINE_WALL = regWithItem("dark_prismarine_wall", p ->
        new WallBlock(p), () -> Properties.ofFullCopy(Blocks.DARK_PRISMARINE));

    public static final Supplier<Block> TINTED_GLASS_PANE = regWithItem("tinted_glass_pane", p ->
        new TintedGlassPane(p), () -> Properties.of().strength(0.3F).sound(SoundType.GLASS).noOcclusion().isRedstoneConductor(NEVER).isSuffocating(NEVER).isViewBlocking(NEVER));


    private static void registerLeafPiles(Registrator<Block> event) {
        for (LeavesType type : LeavesTypeRegistry.INSTANCE.getValues()) {
            String name = type.getVariantId("leaf_pile", false);

            LeafPileBlock block = new LeafPileBlock(leafPileProperties().setId(ResourceKey.create(Registries.BLOCK, ImmersiveWeathering.res(name))), type);
            event.register(ImmersiveWeathering.res(name), block);

            LEAF_PILES.put(type, block);
            type.addChild("immersive_weathering:leaf_pile", block);
        }
    }

    private static BlockBehaviour.Properties noTick(Block copyFrom){
        var p = BlockBehaviour.Properties.ofFullCopy(copyFrom);
        p.isRandomlyTicking = false;
        return p;
    }
}
