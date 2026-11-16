package com.ordana.immersive_weathering.util;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Fixed list of vanilla wood and leaves types (replaces Moonlight's dynamic BlockSetAPI).
 * Modded wood types are intentionally not supported.
 */
public final class VanillaTypes {

    public static final class Wood {
        public final Identifier id;
        public final Block log, wood;
        @Nullable
        public final Block strippedLog, strippedWood;
        public final boolean canBurn;

        Wood(String name, Block log, Block wood, Block strippedLog, Block strippedWood, boolean canBurn) {
            this.id = Identifier.withDefaultNamespace(name);
            this.log = log;
            this.wood = wood;
            this.strippedLog = strippedLog;
            this.strippedWood = strippedWood;
            this.canBurn = canBurn;
        }

        /** "oak" -> "oak_bark", or "crimson_scales" for nether woods. */
        public String barkName() {
            return id.getPath() + (canBurn ? "_bark" : "_scales");
        }

        @Override
        public String toString() {
            return id.toString();
        }
    }

    public static final class Leaves {
        public final Identifier id;
        public final Block leaves;

        Leaves(String name, Block leaves) {
            this.id = Identifier.withDefaultNamespace(name);
            this.leaves = leaves;
        }

        public String leafPileName() {
            return id.getPath() + "_leaf_pile";
        }

        @Override
        public String toString() {
            return id.toString();
        }
    }

    public static final Wood OAK = new Wood("oak", Blocks.OAK_LOG, Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_LOG, Blocks.STRIPPED_OAK_WOOD, true);
    public static final Wood SPRUCE = new Wood("spruce", Blocks.SPRUCE_LOG, Blocks.SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_WOOD, true);
    public static final Wood BIRCH = new Wood("birch", Blocks.BIRCH_LOG, Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_LOG, Blocks.STRIPPED_BIRCH_WOOD, true);
    public static final Wood JUNGLE = new Wood("jungle", Blocks.JUNGLE_LOG, Blocks.JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_WOOD, true);
    public static final Wood ACACIA = new Wood("acacia", Blocks.ACACIA_LOG, Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_LOG, Blocks.STRIPPED_ACACIA_WOOD, true);
    public static final Wood DARK_OAK = new Wood("dark_oak", Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_WOOD, true);
    public static final Wood MANGROVE = new Wood("mangrove", Blocks.MANGROVE_LOG, Blocks.MANGROVE_WOOD, Blocks.STRIPPED_MANGROVE_LOG, Blocks.STRIPPED_MANGROVE_WOOD, true);
    public static final Wood CHERRY = new Wood("cherry", Blocks.CHERRY_LOG, Blocks.CHERRY_WOOD, Blocks.STRIPPED_CHERRY_LOG, Blocks.STRIPPED_CHERRY_WOOD, true);
    public static final Wood PALE_OAK = new Wood("pale_oak", Blocks.PALE_OAK_LOG, Blocks.PALE_OAK_WOOD, Blocks.STRIPPED_PALE_OAK_LOG, Blocks.STRIPPED_PALE_OAK_WOOD, true);
    public static final Wood POPLAR = new Wood("poplar", Blocks.POPLAR_LOG, Blocks.POPLAR_WOOD, Blocks.STRIPPED_POPLAR_LOG, Blocks.STRIPPED_POPLAR_WOOD, true);
    public static final Wood BAMBOO = new Wood("bamboo", Blocks.BAMBOO_BLOCK, Blocks.BAMBOO_BLOCK, Blocks.STRIPPED_BAMBOO_BLOCK, null, true);
    public static final Wood CRIMSON = new Wood("crimson", Blocks.CRIMSON_STEM, Blocks.CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_HYPHAE, false);
    public static final Wood WARPED = new Wood("warped", Blocks.WARPED_STEM, Blocks.WARPED_HYPHAE, Blocks.STRIPPED_WARPED_STEM, Blocks.STRIPPED_WARPED_HYPHAE, false);

    public static final List<Wood> WOODS = List.of(OAK, SPRUCE, BIRCH, JUNGLE, ACACIA, DARK_OAK, MANGROVE, CHERRY, PALE_OAK, POPLAR, BAMBOO, CRIMSON, WARPED);

    public static final Leaves OAK_LEAVES = new Leaves("oak", Blocks.OAK_LEAVES);
    public static final Leaves SPRUCE_LEAVES = new Leaves("spruce", Blocks.SPRUCE_LEAVES);
    public static final Leaves BIRCH_LEAVES = new Leaves("birch", Blocks.BIRCH_LEAVES);
    public static final Leaves JUNGLE_LEAVES = new Leaves("jungle", Blocks.JUNGLE_LEAVES);
    public static final Leaves ACACIA_LEAVES = new Leaves("acacia", Blocks.ACACIA_LEAVES);
    public static final Leaves DARK_OAK_LEAVES = new Leaves("dark_oak", Blocks.DARK_OAK_LEAVES);
    public static final Leaves MANGROVE_LEAVES = new Leaves("mangrove", Blocks.MANGROVE_LEAVES);
    public static final Leaves CHERRY_LEAVES = new Leaves("cherry", Blocks.CHERRY_LEAVES);
    public static final Leaves PALE_OAK_LEAVES = new Leaves("pale_oak", Blocks.PALE_OAK_LEAVES);
    public static final Leaves RED_POPLAR_LEAVES = new Leaves("red_poplar", Blocks.RED_POPLAR_LEAVES);
    public static final Leaves ORANGE_POPLAR_LEAVES = new Leaves("orange_poplar", Blocks.ORANGE_POPLAR_LEAVES);
    public static final Leaves YELLOW_POPLAR_LEAVES = new Leaves("yellow_poplar", Blocks.YELLOW_POPLAR_LEAVES);
    public static final Leaves AZALEA_LEAVES = new Leaves("azalea", Blocks.AZALEA_LEAVES);
    public static final Leaves FLOWERING_AZALEA_LEAVES = new Leaves("flowering_azalea", Blocks.FLOWERING_AZALEA_LEAVES);

    public static final List<Leaves> LEAVES = List.of(OAK_LEAVES, SPRUCE_LEAVES, BIRCH_LEAVES, JUNGLE_LEAVES, ACACIA_LEAVES,
            DARK_OAK_LEAVES, MANGROVE_LEAVES, CHERRY_LEAVES, PALE_OAK_LEAVES, RED_POPLAR_LEAVES, ORANGE_POPLAR_LEAVES, YELLOW_POPLAR_LEAVES, AZALEA_LEAVES, FLOWERING_AZALEA_LEAVES);

    @Nullable
    public static Wood woodOf(Block block) {
        for (Wood w : WOODS) {
            if (w.log == block || w.wood == block || w.strippedLog == block || w.strippedWood == block) return w;
        }
        return null;
    }

    @Nullable
    public static Wood wood(String name) {
        for (Wood w : WOODS) if (w.id.getPath().equals(name)) return w;
        return null;
    }

    @Nullable
    public static Leaves leaves(String name) {
        for (Leaves l : LEAVES) if (l.id.getPath().equals(name)) return l;
        return null;
    }

    private VanillaTypes() {
    }
}
