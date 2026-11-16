package com.ordana.immersive_weathering.reg;

import com.ordana.immersive_weathering.blocks.ModPropaguleBlock;
import com.ordana.immersive_weathering.blocks.cracked.*;
import com.ordana.immersive_weathering.blocks.mossy.*;
import com.ordana.immersive_weathering.blocks.rusty.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Vanilla blocks that get replaced by weathering-aware classes (crackable bricks, mossable stone, rustable iron...).
 * <p>
 * The original mod redirected the constructor calls inside {@code Blocks.<clinit>}. Since vanilla now
 * registers blocks through factories, a single hook on {@code Blocks.register} swaps the factory instead
 * (see BlocksMixin). Generated from the original mixin by tools/gen_block_replacements.pl.
 */
public class VanillaBlockReplacements {

    private static final Map<String, Function<BlockBehaviour.Properties, Block>> R = new HashMap<>();

    static {
        R.put("mangrove_propagule", p -> new ModPropaguleBlock(p));
        R.put("polished_blackstone_bricks", p -> new CrackableBlock(Crackable.CrackLevel.UNCRACKED, () -> ModItems.BLACKSTONE_BRICK.get(), p));
        R.put("polished_blackstone_brick_slab", p -> new CrackableSlabBlock(Crackable.CrackLevel.UNCRACKED, () -> ModItems.BLACKSTONE_BRICK.get(), p));
        R.put("polished_blackstone_brick_stairs", p -> new CrackableStairsBlock(Crackable.CrackLevel.UNCRACKED, () -> Blocks.POLISHED_BLACKSTONE_BRICKS, () -> ModItems.BLACKSTONE_BRICK.get(), p));
        R.put("polished_blackstone_brick_wall", p -> new CrackableWallBlock(Crackable.CrackLevel.UNCRACKED, () -> ModItems.BLACKSTONE_BRICK.get(), p));
        R.put("cracked_polished_blackstone_bricks", p -> new CrackedBlock(Crackable.CrackLevel.CRACKED, () -> ModItems.BLACKSTONE_BRICK.get(), p));
        R.put("cracked_stone_bricks", p -> new CrackedBlock(Crackable.CrackLevel.CRACKED, () -> ModItems.STONE_BRICK.get(), p));
        R.put("cracked_deepslate_bricks", p -> new CrackedBlock(Crackable.CrackLevel.CRACKED, () -> ModItems.DEEPSLATE_BRICK.get(), p));
        R.put("cracked_deepslate_tiles", p -> new CrackedBlock(Crackable.CrackLevel.CRACKED, () -> ModItems.DEEPSLATE_TILE.get(), p));
        R.put("nether_bricks", p -> new CrackableBlock(Crackable.CrackLevel.UNCRACKED, () -> Items.NETHER_BRICK, p));
        R.put("nether_brick_slab", p -> new CrackableSlabBlock(Crackable.CrackLevel.UNCRACKED, () -> Items.NETHER_BRICK, p));
        R.put("nether_brick_stairs", p -> new CrackableStairsBlock(Crackable.CrackLevel.UNCRACKED, () -> Blocks.NETHER_BRICKS, () -> Items.NETHER_BRICK, p));
        R.put("nether_brick_wall", p -> new CrackableWallBlock(Crackable.CrackLevel.UNCRACKED, () -> Items.NETHER_BRICK, p));
        R.put("cracked_nether_bricks", p -> new CrackedBlock(Crackable.CrackLevel.CRACKED, () -> Items.NETHER_BRICK, p));
        R.put("bricks", p -> new CrackableMossableBlock(Mossable.MossLevel.UNAFFECTED, Crackable.CrackLevel.UNCRACKED, () -> Items.BRICK, p));
        R.put("brick_slab", p -> new CrackableMossableSlabBlock(Mossable.MossLevel.UNAFFECTED, Crackable.CrackLevel.UNCRACKED, () -> Items.BRICK, p));
        R.put("brick_stairs", p -> new CrackableMossableStairsBlock(Mossable.MossLevel.UNAFFECTED, Crackable.CrackLevel.UNCRACKED, () -> Items.BRICK, () -> Blocks.BRICKS, p));
        R.put("brick_wall", p -> new CrackableMossableWallBlock(Mossable.MossLevel.UNAFFECTED, Crackable.CrackLevel.UNCRACKED, () -> Items.BRICK, p));
        R.put("stone_bricks", p -> new CrackableMossableBlock(Mossable.MossLevel.UNAFFECTED, Crackable.CrackLevel.UNCRACKED, () -> ModItems.STONE_BRICK.get(), p));
        R.put("stone_brick_slab", p -> new CrackableMossableSlabBlock(Mossable.MossLevel.UNAFFECTED, Crackable.CrackLevel.UNCRACKED, () -> ModItems.STONE_BRICK.get(), p));
        R.put("stone_brick_stairs", p -> new CrackableMossableStairsBlock(Mossable.MossLevel.UNAFFECTED, Crackable.CrackLevel.UNCRACKED, () -> ModItems.STONE_BRICK.get(), () -> Blocks.STONE_BRICKS, p));
        R.put("stone_brick_wall", p -> new CrackableMossableWallBlock(Mossable.MossLevel.UNAFFECTED, Crackable.CrackLevel.UNCRACKED, () -> ModItems.STONE_BRICK.get(), p));
        R.put("cobblestone", p -> new MossableBlock(Mossable.MossLevel.UNAFFECTED, p));
        R.put("chiseled_stone_bricks", p -> new MossableBlock(Mossable.MossLevel.UNAFFECTED, p));
        R.put("cobblestone_slab", p -> new MossableSlabBlock(Mossable.MossLevel.UNAFFECTED, p));
        R.put("cobblestone_stairs", p -> new MossableStairsBlock(Mossable.MossLevel.UNAFFECTED, () -> Blocks.COBBLESTONE, p));
        R.put("cobblestone_wall", p -> new MossableWallBlock(Mossable.MossLevel.UNAFFECTED, p));
        R.put("mossy_stone_bricks", p -> new MossyBlock(Mossable.MossLevel.MOSSY, p));
        R.put("mossy_stone_brick_slab", p -> new MossySlabBlock(Mossable.MossLevel.MOSSY, p));
        R.put("mossy_stone_brick_stairs", p -> new MossyStairsBlock(Mossable.MossLevel.MOSSY, () -> Blocks.MOSSY_STONE_BRICKS, p));
        R.put("mossy_stone_brick_wall", p -> new MossyWallBlock(Mossable.MossLevel.MOSSY, p));
        R.put("mossy_cobblestone", p -> new MossyBlock(Mossable.MossLevel.MOSSY, p));
        R.put("mossy_cobblestone_slab", p -> new MossySlabBlock(Mossable.MossLevel.MOSSY, p));
        R.put("mossy_cobblestone_stairs", p -> new MossyStairsBlock(Mossable.MossLevel.MOSSY, () -> Blocks.MOSSY_COBBLESTONE, p));
        R.put("mossy_cobblestone_wall", p -> new MossyWallBlock(Mossable.MossLevel.MOSSY, p));
        R.put("iron_bars", p -> new RustableBarsBlock(Rustable.RustLevel.UNAFFECTED, p));
        R.put("iron_chain", p -> new com.ordana.immersive_weathering.blocks.rusty.RustableChainBlock(Rustable.RustLevel.UNAFFECTED, p));
        R.put("lantern", p -> new com.ordana.immersive_weathering.blocks.rusty.RustableLanternBlock(Rustable.RustLevel.UNAFFECTED, p));
        R.put("iron_door", p -> new RustableDoorBlock(Rustable.RustLevel.UNAFFECTED, p));
        R.put("iron_trapdoor", p -> new RustableTrapdoorBlock(Rustable.RustLevel.UNAFFECTED, p));
        R.put("prismarine_bricks", p -> new CrackableBlock(Crackable.CrackLevel.UNCRACKED, () -> ModItems.PRISMARINE_BRICK.get(), p));
        R.put("prismarine_brick_slab", p -> new CrackableSlabBlock(Crackable.CrackLevel.UNCRACKED, () -> ModItems.PRISMARINE_BRICK.get(), p));
        R.put("prismarine_brick_stairs", p -> new CrackableStairsBlock(Crackable.CrackLevel.UNCRACKED, () -> Blocks.PRISMARINE_BRICKS, () -> ModItems.PRISMARINE_BRICK.get(), p));
        R.put("end_stone_bricks", p -> new CrackableBlock(Crackable.CrackLevel.UNCRACKED, () -> ModItems.END_STONE_BRICK.get(), p));
        R.put("end_stone_brick_slab", p -> new CrackableSlabBlock(Crackable.CrackLevel.UNCRACKED, () -> ModItems.END_STONE_BRICK.get(), p));
        R.put("end_stone_brick_stairs", p -> new CrackableStairsBlock(Crackable.CrackLevel.UNCRACKED, () -> Blocks.END_STONE_BRICKS, () -> ModItems.END_STONE_BRICK.get(), p));
        R.put("end_stone_brick_wall", p -> new CrackableWallBlock(Crackable.CrackLevel.UNCRACKED, () -> ModItems.END_STONE_BRICK.get(), p));
        R.put("tuff_bricks", p -> new CrackableMossableBlock(Mossable.MossLevel.UNAFFECTED, Crackable.CrackLevel.UNCRACKED, () -> Items.TUFF, p));
        R.put("tuff_brick_stairs", p -> new CrackableMossableStairsBlock(Mossable.MossLevel.UNAFFECTED, Crackable.CrackLevel.UNCRACKED, () -> Items.TUFF, () -> Blocks.TUFF_BRICKS, p));
        R.put("tuff_brick_slab", p -> new CrackableMossableSlabBlock(Mossable.MossLevel.UNAFFECTED, Crackable.CrackLevel.UNCRACKED, () -> Items.TUFF, p));
        R.put("tuff_brick_wall", p -> new CrackableMossableWallBlock(Mossable.MossLevel.UNAFFECTED, Crackable.CrackLevel.UNCRACKED, () -> Items.TUFF, p));
        R.put("mud_bricks", p -> new CrackableMossableBlock(Mossable.MossLevel.UNAFFECTED, Crackable.CrackLevel.UNCRACKED, () -> Items.PACKED_MUD, p));
        R.put("mud_brick_stairs", p -> new CrackableMossableStairsBlock(Mossable.MossLevel.UNAFFECTED, Crackable.CrackLevel.UNCRACKED, () -> Items.PACKED_MUD, () -> Blocks.MUD_BRICKS, p));
        R.put("mud_brick_slab", p -> new CrackableMossableSlabBlock(Mossable.MossLevel.UNAFFECTED, Crackable.CrackLevel.UNCRACKED, () -> Items.PACKED_MUD, p));
        R.put("mud_brick_wall", p -> new CrackableMossableWallBlock(Mossable.MossLevel.UNAFFECTED, Crackable.CrackLevel.UNCRACKED, () -> Items.PACKED_MUD, p));
        R.put("deepslate_bricks", p -> new CrackableMossableBlock(Mossable.MossLevel.UNAFFECTED, Crackable.CrackLevel.UNCRACKED, () -> ModItems.DEEPSLATE_BRICK.get(), p));
        R.put("deepslate_brick_stairs", p -> new CrackableMossableStairsBlock(Mossable.MossLevel.UNAFFECTED, Crackable.CrackLevel.UNCRACKED, () -> ModItems.DEEPSLATE_BRICK.get(), () -> Blocks.DEEPSLATE_BRICKS, p));
        R.put("deepslate_brick_slab", p -> new CrackableMossableSlabBlock(Mossable.MossLevel.UNAFFECTED, Crackable.CrackLevel.UNCRACKED, () -> ModItems.DEEPSLATE_BRICK.get(), p));
        R.put("deepslate_brick_wall", p -> new CrackableMossableWallBlock(Mossable.MossLevel.UNAFFECTED, Crackable.CrackLevel.UNCRACKED, () -> ModItems.DEEPSLATE_BRICK.get(), p));
        R.put("deepslate_tiles", p -> new CrackableMossableBlock(Mossable.MossLevel.UNAFFECTED, Crackable.CrackLevel.UNCRACKED, () -> ModItems.DEEPSLATE_TILE.get(), p));
        R.put("deepslate_tile_stairs", p -> new CrackableMossableStairsBlock(Mossable.MossLevel.UNAFFECTED, Crackable.CrackLevel.UNCRACKED, () -> ModItems.DEEPSLATE_TILE.get(), () -> Blocks.DEEPSLATE_TILES, p));
        R.put("deepslate_tile_slab", p -> new CrackableMossableSlabBlock(Mossable.MossLevel.UNAFFECTED, Crackable.CrackLevel.UNCRACKED, () -> ModItems.DEEPSLATE_TILE.get(), p));
        R.put("deepslate_tile_wall", p -> new CrackableMossableWallBlock(Mossable.MossLevel.UNAFFECTED, Crackable.CrackLevel.UNCRACKED, () -> ModItems.DEEPSLATE_TILE.get(), p));
        R.put("stone", p -> new MossableBlock(Mossable.MossLevel.UNAFFECTED, p));
        R.put("stone_slab", p -> new MossableSlabBlock(Mossable.MossLevel.UNAFFECTED, p));
        R.put("stone_stairs", p -> new MossableStairsBlock(Mossable.MossLevel.UNAFFECTED, () -> Blocks.STONE, p));
    }

    public static Function<BlockBehaviour.Properties, Block> get(ResourceKey<Block> id, Function<BlockBehaviour.Properties, Block> original) {
        if (!id.identifier().getNamespace().equals("minecraft")) return original;
        Function<BlockBehaviour.Properties, Block> replacement = R.get(id.identifier().getPath());
        return replacement != null ? replacement : original;
    }
}
