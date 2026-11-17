package com.ordana.immersive_weathering.util;

import com.ordana.immersive_weathering.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.function.Supplier;

/**
 * Port addition: seasons. The season is derived from the overworld day (synced to clients) plus config values
 * (also synced), so server and client agree without extra packets. Winter lowers biome temperatures: temperate
 * biomes get snow instead of rain and water freezes; spring melts it again.
 */
public final class Seasons {

    public enum Season {SPRING, SUMMER, AUTUMN, WINTER}

    public static final String[] MONTHS = {"early spring", "mid spring", "late spring", "early summer", "midsummer",
            "late summer", "early autumn", "mid autumn", "late autumn", "early winter", "midwinter", "late winter"};

    //temperature added to every biome, per month. Plains are 0.8, forests 0.7; snow needs < 0.15
    private static final float[] TEMP = {-0.3f, -0.1f, 0f, 0.1f, 0.2f, 0.1f, 0f, -0.2f, -0.4f, -0.6f, -0.75f, -0.55f};

    //growth multipliers per season: spring, summer, autumn, winter
    private static final double[] VEGETATION = {1.5, 1.2, 0.6, 0.15};
    private static final double[] FROST = {0.5, 0.1, 0.8, 2.0};
    private static final double[] LEAVES = {0.5, 1.0, 3.0, 0.2};
    private static final double[] MOSS = {1.5, 0.8, 1.3, 0.4};
    private static final double[] WOOD = {1.2, 0.8, 1.3, 0.4};
    private static final double[] CROPS = {1.0, 1.0, 0.8, 0.2};

    //updated every tick from the overworld (server) and the client level (client)
    private static volatile int month = 0;
    private static volatile boolean active = false;

    public static long seasonDay(Level level) {
        int paused = CommonConfigs.SEASON_FROZEN_DAY.get();
        if (paused >= 0) return paused;
        return Math.floorDiv(level.getOverworldClockTime(), 24000L) + CommonConfigs.SEASON_OFFSET.get();
    }

    public static int monthOf(long day) {
        return (int) Math.floorMod(Math.floorDiv(day, (long) CommonConfigs.SEASON_MONTH_DAYS.get()), 12L);
    }

    public static void update(Level level) {
        month = monthOf(seasonDay(level));
        active = CommonConfigs.SEASONS.get();
    }

    public static boolean active() {
        return active;
    }

    public static int month() {
        return month;
    }

    public static Season season() {
        return Season.values()[month / 3];
    }

    public static float temperatureOffset() {
        return active ? TEMP[month] : 0f;
    }

    /** Extra growth multiplier for a "speed" config category. */
    public static double factor(Supplier<Double> category) {
        if (!active) return 1;
        int s = month / 3;
        if (category == CommonConfigs.GROWTH_VEGETATION || category == CommonConfigs.GROWTH_WEEDS) return VEGETATION[s];
        if (category == CommonConfigs.GROWTH_FROST) return FROST[s];
        if (category == CommonConfigs.GROWTH_LEAVES) return LEAVES[s];
        if (category == CommonConfigs.MOSS_SPEED) return MOSS[s];
        if (category == CommonConfigs.WOOD_SPEED) return WOOD[s];
        return 1;
    }

    public static double speed(Supplier<Double> category) {
        return category.get() * factor(category);
    }

    public static double cropFactor() {
        return active ? CROPS[month / 3] : 1;
    }

    /** Called for every ticked chunk: once it is warm again, snow and ice in temperate biomes melt. */
    public static void chunkTick(ServerLevel level, LevelChunk chunk) {
        if (!active || month >= 9 || level.dimension() != Level.OVERWORLD) return; //no melting in winter
        RandomSource r = level.getRandom();
        if (r.nextInt(month <= 2 ? 6 : 12) != 0) return;
        int x = chunk.getPos().getMinBlockX() + r.nextInt(16), z = chunk.getPos().getMinBlockZ() + r.nextInt(16);
        BlockPos top = new BlockPos(x, chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, x & 15, z & 15) + 1, z);
        Holder<Biome> biome = level.getBiome(top);
        if (!biome.value().warmEnoughToRain(top, level.getSeaLevel())) return; //still cold here: keep it
        BlockState s = level.getBlockState(top);
        if (s.is(Blocks.SNOW)) {
            int layers = s.getValue(SnowLayerBlock.LAYERS);
            level.setBlock(top, layers > 1 ? s.setValue(SnowLayerBlock.LAYERS, layers - 1) : Blocks.AIR.defaultBlockState(), 3);
            return;
        }
        BlockPos below = top.below();
        BlockState b = level.getBlockState(below);
        if (b.is(Blocks.SNOW)) level.setBlock(below, Blocks.AIR.defaultBlockState(), 3);
        else if (b.is(Blocks.ICE)) level.setBlock(below, Blocks.WATER.defaultBlockState(), 3);
    }

    private Seasons() {
    }
}
