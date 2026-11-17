package com.ordana.immersive_weathering.util;

import com.ordana.immersive_weathering.ImmersiveWeathering;
import com.ordana.immersive_weathering.blocks.cracked.Crackable;
import com.ordana.immersive_weathering.blocks.mossy.Mossable;
import com.ordana.immersive_weathering.blocks.rusty.Rustable;
import com.ordana.immersive_weathering.data.block_growths.BlockGrowthHandler;
import com.ordana.immersive_weathering.data.block_growths.TickSource;
import com.ordana.immersive_weathering.events.ModEvents;
import com.ordana.immersive_weathering.items.CeilingAndWallBlockItem;
import com.ordana.immersive_weathering.reg.ModBlocks;
import com.ordana.immersive_weathering.reg.ModItems;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Development only smoke test (enabled with -Diw.selfTest=true, see build.gradle run configs).
 * Runs once when the dev dedicated server has started: ticks every block of the mod, runs the data driven
 * growths and simulates the tool interactions with a fake player, then logs a summary.
 * It never runs in a normal game.
 */
public class DevSelfTest {

    private static final List<String> failures = new ArrayList<>();
    private static int checks = 0;

    public static void register() {
        if (!Boolean.getBoolean("iw.selfTest")) return;
        ServerLifecycleEvents.SERVER_STARTED.register(DevSelfTest::run);
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) failures.add(name);
    }

    private static void guarded(String name, Runnable r) {
        checks++;
        try {
            r.run();
        } catch (Throwable t) {
            failures.add(name + " threw " + t);
            ImmersiveWeathering.LOGGER.error("Self test '{}' failed", name, t);
        }
    }

    private static void run(MinecraftServer server) {
        ServerLevel level = server.overworld();
        BlockPos base = new BlockPos(8, 200, 8);
        level.getChunk(base);

        // --- vanilla blocks replaced by weathering aware ones
        check("stone bricks are crackable+mossable", Blocks.STONE_BRICKS instanceof Crackable && Blocks.STONE_BRICKS instanceof Mossable);
        check("cobblestone is mossable", Blocks.COBBLESTONE instanceof Mossable);
        check("iron bars are rustable", Blocks.IRON_BARS instanceof Rustable);
        check("iron door is rustable", Blocks.IRON_DOOR instanceof Rustable);
        check("hanging roots item replaced", Items.HANGING_ROOTS instanceof CeilingAndWallBlockItem);

        // --- every block state of the mod survives random ticks, scheduled ticks and growth ticks
        int states = 0;
        for (Block b : BuiltInRegistries.BLOCK) {
            if (!BuiltInRegistries.BLOCK.getKey(b).getNamespace().equals(ImmersiveWeathering.MOD_ID)) continue;
            for (BlockState state : b.getStateDefinition().getPossibleStates()) {
                states++;
                BlockPos pos = base.above(2);
                guarded("ticking " + state, () -> {
                    level.setBlock(base, Blocks.STONE.defaultBlockState(), 2);
                    level.setBlock(pos.below(), Blocks.DIRT.defaultBlockState(), 2);
                    level.setBlock(pos, state, 2);
                    for (int i = 0; i < 3 && level.getBlockState(pos) == state; i++) {
                        state.randomTick(level, pos, level.getRandom());
                    }
                    if (level.getBlockState(pos) == state) state.tick(level, pos, level.getRandom());
                    for (TickSource s : TickSource.values()) {
                        BlockGrowthHandler.tickBlock(s, level.getBlockState(pos), level, pos);
                    }
                });
                clear(level, base);
            }
        }
        ImmersiveWeathering.LOGGER.info("Self test: ticked {} block states", states);
        for (Block b : BuiltInRegistries.BLOCK) {
            if (BuiltInRegistries.BLOCK.getKey(b).getNamespace().equals(ImmersiveWeathering.MOD_ID) && !b.defaultBlockState().getFluidState().isEmpty()) {
                ImmersiveWeathering.LOGGER.info("Self test: default state contains fluid: {}", b.defaultBlockState());
            }
        }

        // --- growths on common vanilla owners
        for (Block b : List.of(Blocks.GRASS_BLOCK, Blocks.STONE, Blocks.STONE_BRICKS, Blocks.OAK_LEAVES, Blocks.ICE, Blocks.FIRE,
                Blocks.SAND, Blocks.MAGMA_BLOCK, Blocks.CAMPFIRE, Blocks.WATER, Blocks.IRON_BLOCK, Blocks.MOSS_BLOCK, Blocks.DIRT,
                Blocks.PODZOL, Blocks.MYCELIUM, Blocks.SNOW_BLOCK, Blocks.COBBLESTONE, Blocks.OAK_LOG, Blocks.BRAIN_CORAL_BLOCK)) {
            guarded("growths on " + b, () -> {
                BlockPos pos = base.above(2);
                level.setBlock(pos.below(), Blocks.DIRT.defaultBlockState(), 2);
                level.setBlock(pos, b.defaultBlockState(), 2);
                for (int i = 0; i < 200; i++) {
                    for (TickSource s : TickSource.values()) {
                        BlockGrowthHandler.tickBlock(s, level.getBlockState(pos), level, pos);
                    }
                }
            });
            clear(level, base);
        }
        check("grass block has block growths", BlockGrowthHandler.getBlockGrowths(TickSource.BLOCK_TICK, Blocks.GRASS_BLOCK).isPresent());

        // --- tool interactions
        FakePlayer player = FakePlayer.get(level);
        BlockPos p = base.above(2);
        interact(level, player, p, Blocks.STONE_BRICKS.defaultBlockState(), new ItemStack(Items.IRON_PICKAXE),
                "pickaxe cracks stone bricks", () -> level.getBlockState(p).is(Blocks.CRACKED_STONE_BRICKS));
        interact(level, player, p, Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), new ItemStack(ModItems.STONE_BRICK.get()),
                "brick repairs cracked stone bricks", () -> level.getBlockState(p).is(Blocks.STONE_BRICKS));
        interact(level, player, p, Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), new ItemStack(Items.SHEARS),
                "shears remove moss", () -> level.getBlockState(p).is(Blocks.STONE_BRICKS));
        interact(level, player, p, ModBlocks.WEATHERED_CUT_IRON.get().defaultBlockState(), new ItemStack(Items.IRON_AXE),
                "axe scrapes weathered iron", () -> level.getBlockState(p).is(ModBlocks.EXPOSED_CUT_IRON.get()));
        interact(level, player, p, ModBlocks.RUSTED_CUT_IRON.get().defaultBlockState(), new ItemStack(ModItems.STEEL_WOOL.get()),
                "steel wool scrapes rusted iron", () -> level.getBlockState(p).is(ModBlocks.WEATHERED_CUT_IRON.get()));
        //the handler only drops the bark and lets vanilla do the stripping
        check("oak log gives bark", WeatheringHelper.getBarkToStrip(Blocks.OAK_LOG.defaultBlockState()) != null);
        check("pale oak log gives bark", WeatheringHelper.getBarkToStrip(Blocks.PALE_OAK_LOG.defaultBlockState()) != null);
        check("poplar log gives bark", WeatheringHelper.getBarkToStrip(Blocks.POPLAR_LOG.defaultBlockState()) != null);
        check("iron chain rusts", Blocks.IRON_CHAIN instanceof com.ordana.immersive_weathering.blocks.rusty.Rustable
                && com.ordana.immersive_weathering.blocks.rusty.Rustable.RUST_LEVEL_INCREASES.get().get(Blocks.IRON_CHAIN) != null);
        check("lantern rusts", Blocks.LANTERN instanceof com.ordana.immersive_weathering.blocks.rusty.Rustable);
        check("tuff bricks get mossy", Blocks.TUFF_BRICKS instanceof com.ordana.immersive_weathering.blocks.mossy.Mossable
                && com.ordana.immersive_weathering.blocks.mossy.Mossable.MOSS_LEVEL_INCREASES.get().get(Blocks.TUFF_BRICK_WALL) != null);
        check("mud bricks crack", com.ordana.immersive_weathering.blocks.cracked.Crackable.CRACK_LEVEL_INCREASES.get().get(Blocks.MUD_BRICK_STAIRS) != null);
        check("deepslate tiles get mossy", com.ordana.immersive_weathering.blocks.mossy.Mossable.MOSS_LEVEL_INCREASES.get().get(Blocks.DEEPSLATE_TILES) != null);
        check("oak planks tick and rot", Blocks.OAK_PLANKS.defaultBlockState().isRandomlyTicking()
                && WoodWeathering.getRotten(Blocks.OAK_STAIRS.defaultBlockState()).is(com.ordana.immersive_weathering.reg.ModBlocks.ROTTEN_STAIRS.get()));
        check("crimson does not rot", WoodWeathering.getRotten(Blocks.CRIMSON_PLANKS.defaultBlockState()) == null);
        check("stone does not tick", !Blocks.STONE_BRICKS.defaultBlockState().is(Blocks.OAK_PLANKS) && !WoodWeathering.isWeatherableWood(Blocks.OAK_DOOR));
        check("poplar leaves drop piles", WeatheringHelper.getFallenLeafPile(Blocks.RED_POPLAR_LEAVES.defaultBlockState()).isPresent());
        check("bark repairs stripped log", WeatheringHelper.getBarkForStrippedLog(Blocks.STRIPPED_OAK_LOG.defaultBlockState()).isPresent());
        interact(level, player, p, Blocks.OAK_LOG.defaultBlockState(), new ItemStack(Items.IRON_AXE),
                "axe on log drops bark", () -> !level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(p).inflate(3)).isEmpty());
        interact(level, player, p, Blocks.STONE.defaultBlockState(), new ItemStack(ModBlocks.SAND_LAYER_BLOCK.get()),
                "sand layer makes sandy stone", () -> level.getBlockState(p).is(ModBlocks.SANDY_STONE.get()));
        interact(level, player, p, Blocks.STONE.defaultBlockState(), new ItemStack(Items.SNOWBALL),
                "snowball makes snowy stone", () -> level.getBlockState(p).is(ModBlocks.SNOWY_STONE.get()));
        guarded("moss clump on stone bricks", () -> {
            level.setBlock(p, Blocks.STONE_BRICKS.defaultBlockState(), 2);
            Mossable.getMossyBlock(level.getBlockState(p));
        });
        check("moss clump target", Mossable.getMossyBlock(Blocks.STONE_BRICKS.defaultBlockState()).is(Blocks.MOSSY_STONE_BRICKS));
        clear(level, base);

        // --- moss / crack spreading by random ticks
        guarded("moss spreading ticks", () -> {
            for (int x = 0; x < 3; x++) for (int z = 0; z < 3; z++) level.setBlock(p.offset(x, 0, z), Blocks.STONE_BRICKS.defaultBlockState(), 3);
            level.setBlock(p.offset(1, 0, 1), Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), 3);
            level.setBlock(p.offset(1, 1, 1), Blocks.WATER.defaultBlockState(), 3);
            for (int i = 0; i < 2000; i++) {
                for (int x = 0; x < 3; x++) for (int z = 0; z < 3; z++) {
                    BlockPos q = p.offset(x, 0, z);
                    BlockState s = level.getBlockState(q);
                    if (s.isRandomlyTicking()) s.randomTick(level, q, level.getRandom());
                }
            }
        });
        int mossy = 0;
        for (int x = 0; x < 3; x++) for (int z = 0; z < 3; z++) if (level.getBlockState(p.offset(x, 0, z)).is(Blocks.MOSSY_STONE_BRICKS)) mossy++;
        ImmersiveWeathering.LOGGER.info("Self test: {} of 9 stone bricks are mossy after 2000 random ticks", mossy);
        for (int x = 0; x < 3; x++) for (int z = 0; z < 3; z++) for (int y = 0; y < 2; y++) level.setBlock(p.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);

        // --- wood rotting in water (port addition)
        guarded("wood rot ticks", () -> {
            level.setBlock(p, Blocks.OAK_PLANKS.defaultBlockState(), 3);
            level.setBlock(p.above(), Blocks.WATER.defaultBlockState(), 3);
            for (int i = 0; i < 5000 && level.getBlockState(p).is(Blocks.OAK_PLANKS); i++) {
                com.ordana.immersive_weathering.data.block_growths.BlockGrowthHandler.tickBlock(
                        com.ordana.immersive_weathering.data.block_growths.TickSource.BLOCK_TICK, level.getBlockState(p), level, p);
            }
        });
        check("oak planks under water rot", level.getBlockState(p).is(ModBlocks.ROTTEN_PLANKS.get()));
        //varnished planks under water stay fresh
        guarded("varnish ticks", () -> {
            level.setBlock(p, Blocks.OAK_PLANKS.defaultBlockState(), 3);
            com.ordana.immersive_weathering.util.Varnish.add(level, p);
            for (int i = 0; i < 5000; i++) {
                com.ordana.immersive_weathering.data.block_growths.BlockGrowthHandler.tickBlock(
                        com.ordana.immersive_weathering.data.block_growths.TickSource.BLOCK_TICK, level.getBlockState(p), level, p);
            }
        });
        check("varnished planks under water don't rot", level.getBlockState(p).is(Blocks.OAK_PLANKS));
        com.ordana.immersive_weathering.util.Varnish.remove(level, p);
        ImmersiveWeathering.LOGGER.info("Self test: varnished oak planks under water stayed {}", level.getBlockState(p));
        level.setBlock(p.above(), Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);

        // --- abandonment (port addition): a small stone brick hut decays
        guarded("abandonment", () -> {
            for (int x = 0; x < 5; x++) for (int z = 0; z < 5; z++) for (int y = 0; y < 4; y++)
                level.setBlock(p.offset(x, y, z), (x == 0 || x == 4 || z == 0 || z == 4 || y == 0 || y == 3)
                        ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
            int n = com.ordana.immersive_weathering.util.Abandonment.age(level, p, 30);
            int old = 0;
            for (int x = 0; x < 5; x++) for (int z = 0; z < 5; z++) for (int y = 0; y < 4; y++) {
                BlockState s = level.getBlockState(p.offset(x, y, z));
                if (s.is(Blocks.MOSSY_STONE_BRICKS) || s.is(Blocks.CRACKED_STONE_BRICKS) || s.is(Blocks.COBWEB)
                        || s.is(ModBlocks.MOSS.get()) || s.is(ModBlocks.IVY.get())) old++;
            }
            ImmersiveWeathering.LOGGER.info("Self test: abandonment changed {} blocks in the chunk, {} in the hut", n, old);
            check("abandoned hut decays", old > 0);
            for (int x = 0; x < 5; x++) for (int z = 0; z < 5; z++) for (int y = 0; y < 4; y++)
                level.setBlock(p.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
        });

        // --- rust progression
        guarded("rusting ticks", () -> {
            level.setBlock(p, ModBlocks.CUT_IRON.get().defaultBlockState(), 3);
            level.setBlock(p.above(), Blocks.WATER.defaultBlockState(), 3);
            for (int i = 0; i < 5000 && level.getBlockState(p).is(ModBlocks.CUT_IRON.get()); i++) {
                BlockState s = level.getBlockState(p);
                if (s.isRandomlyTicking()) s.randomTick(level, p, level.getRandom());
            }
        });
        ImmersiveWeathering.LOGGER.info("Self test: cut iron next to water became {}", level.getBlockState(p));
        clear(level, base);

        // --- fluid generators (lava touching water with a diorite neighbour should make andesite, see data)
        guarded("fluid generators", () -> {
            for (BlockPos q : BlockPos.betweenClosed(p.offset(-1, -1, -1), p.offset(2, -1, 1))) level.setBlock(q, Blocks.STONE.defaultBlockState(), 3);
            level.setBlock(p.offset(-1, 0, 0), Blocks.DIORITE.defaultBlockState(), 3);
            level.setBlock(p.offset(1, 0, 0), Blocks.WATER.defaultBlockState(), 3);
            level.setBlock(p, Blocks.LAVA.defaultBlockState(), 3);
            for (int i = 0; i < 40; i++) level.getBlockState(p).tick(level, p, level.getRandom());
        });
        ImmersiveWeathering.LOGGER.info("Self test: lava next to water and diorite became {}", level.getBlockState(p));
        clear(level, base);

        // --- lightning on sand (vitrified sand / fulgurite growth) and fire next to wood (charring, soot)
        guarded("lightning growth", () -> {
            for (BlockPos q : BlockPos.betweenClosed(p.offset(-1, -1, -1), p.offset(1, 0, 1))) level.setBlock(q, Blocks.SAND.defaultBlockState(), 3);
            for (int i = 0; i < 20; i++) BlockGrowthHandler.tickBlock(TickSource.LIGHTNING, level.getBlockState(p), level, p);
        });
        ImmersiveWeathering.LOGGER.info("Self test: sand struck by lightning became {}", level.getBlockState(p));
        clear(level, base);
        guarded("fire ticks", () -> {
            level.setBlock(p, Blocks.OAK_LOG.defaultBlockState(), 3);
            level.setBlock(p.above(), Blocks.FIRE.defaultBlockState(), 3);
            for (int i = 0; i < 300; i++) {
                BlockState f = level.getBlockState(p.above());
                if (f.is(Blocks.FIRE)) f.tick(level, p.above(), level.getRandom());
                BlockGrowthHandler.tickBlock(TickSource.BLOCK_TICK, level.getBlockState(p.above()), level, p.above());
            }
        });
        ImmersiveWeathering.LOGGER.info("Self test: burning oak log ended as {} with {} above", level.getBlockState(p), level.getBlockState(p.above()));
        clear(level, base);

        guarded("ash", () -> {
            level.setBlock(p.below(), Blocks.STONE.defaultBlockState(), 3);
            com.ordana.immersive_weathering.blocks.AshLayerBlock.placeAsh(level, p, 2);
            check("ash placed (" + level.getBlockState(p) + ")", level.getBlockState(p).is(com.ordana.immersive_weathering.reg.ModBlocks.ASH_LAYER_BLOCK.get()));
            com.ordana.immersive_weathering.blocks.AshLayerBlock.placeAsh(level, p, 3);
            check("ash stacks", level.getBlockState(p).getValue(com.ordana.immersive_weathering.blocks.LayerBlock.LAYERS_8) == 5);
        });
        clear(level, base);

        // --- seasons (port addition): commands move the calendar, winter makes plains snowy
        guarded("seasons", () -> {
            var src = server.createCommandSourceStack().withSuppressedOutput();
            java.util.function.Consumer<String> cmd = s -> server.getCommands().performPrefixedCommand(src, s);
            var plains = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BIOME)
                    .getOrThrow(net.minecraft.world.level.biome.Biomes.PLAINS).value();
            BlockPos q = new BlockPos(1000, 64, 1000);
            cmd.accept("iw season on");
            cmd.accept("iw season set winter");
            check("set winter -> early winter (" + Seasons.month() + ")", Seasons.month() == 9);
            cmd.accept("iw season month 11");
            check("month 11 -> midwinter (" + Seasons.month() + ")", Seasons.month() == 10);
            check("plains snow in midwinter (" + plains.getTemperature(q, 63) + ")", plains.coldEnoughToSnow(q, 63));
            cmd.accept("iw season skip -2");
            check("skip -2 -> late autumn (" + Seasons.month() + ")", Seasons.month() == 8);
            cmd.accept("iw season skip 5");
            check("skip 5 wraps to mid spring (" + Seasons.month() + ")", Seasons.month() == 1);
            cmd.accept("iw season pause");
            cmd.accept("iw season skip 2");
            check("skip while paused -> early summer (" + Seasons.month() + ")", Seasons.month() == 3);
            cmd.accept("iw season resume");
            check("resume keeps month (" + Seasons.month() + ")", Seasons.month() == 3);
            cmd.accept("iw season length 3");
            check("length keeps month (" + Seasons.month() + ")", Seasons.month() == 3);
            check("plains rain in summer", !plains.coldEnoughToSnow(q, 63));
            cmd.accept("iw season off");
            check("off -> no offset", !Seasons.active() && Seasons.temperatureOffset() == 0);
            cmd.accept("iw season length 8");
            cmd.accept("iw season on");
            cmd.accept("iw season set spring");
        });

        if (failures.isEmpty()) {
            ImmersiveWeathering.LOGGER.info("SELF TEST PASSED ({} checks)", checks);
        } else {
            ImmersiveWeathering.LOGGER.error("SELF TEST FAILED: {} of {} checks", failures.size(), checks);
            failures.stream().limit(60).forEach(f -> ImmersiveWeathering.LOGGER.error("  self test failure: {}", f));
        }
    }

    private static void interact(ServerLevel level, FakePlayer player, BlockPos pos, BlockState target, ItemStack stack,
                                 String name, Supplier<Boolean> expected) {
        guarded(name + " (no exception)", () -> {
            level.setBlock(pos, target, 3);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            player.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 2.5);
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
            InteractionResult r = ModEvents.onBlockCLicked(stack, player, level, InteractionHand.MAIN_HAND, hit);
            check(name + " (result " + r + ", block " + level.getBlockState(pos) + ")", expected.get());
        });
    }

    private static void clear(ServerLevel level, BlockPos base) {
        for (BlockPos q : BlockPos.betweenClosed(base.offset(-2, 0, -2), base.offset(2, 5, 2))) {
            level.setBlock(q, Blocks.AIR.defaultBlockState(), 2);
        }
        var toRemove = new java.util.ArrayList<net.minecraft.world.entity.Entity>();
        for (var e : level.getAllEntities()) {
            if (e != null && !(e instanceof net.minecraft.world.entity.player.Player) && e.blockPosition().closerThan(base, 16)) toRemove.add(e);
        }
        toRemove.forEach(net.minecraft.world.entity.Entity::discard);
    }
}
