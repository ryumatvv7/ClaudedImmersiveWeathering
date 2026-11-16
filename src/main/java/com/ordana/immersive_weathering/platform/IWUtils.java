package com.ordana.immersive_weathering.platform;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * Small helpers that replace Moonlight's Utils / PlatHelper.
 * Particle methods work on both sides: on the server they are sent to nearby players.
 */
public final class IWUtils {

    public static Identifier getID(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block);
    }

    public static Identifier getID(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    public static ItemStack createFilledResult(ItemStack stack, Player player, ItemStack result) {
        return ItemUtils.createFilledResult(stack, player, result);
    }

    public static JsonElement deserializeJson(InputStream stream) {
        try (Reader r = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(r);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isAreaLoaded(LevelReader level, BlockPos pos, int radius) {
        return level.hasChunksAt(pos.offset(-radius, -radius, -radius), pos.offset(radius, radius, radius));
    }

    public static int getFlammability(BlockState state) {
        var entry = FlammableBlockRegistry.getDefaultInstance().get(state.getBlock());
        return entry == null ? 0 : entry.getBurnOdds();
    }

    private static void particle(Level level, ParticleOptions p, double x, double y, double z, double dx, double dy, double dz) {
        if (level instanceof ServerLevel sl) {
            sl.sendParticles(p, x, y, z, 0, dx, dy, dz, 1);
        } else {
            level.addParticle(p, x, y, z, dx, dy, dz);
        }
    }

    public static void spawnParticlesOnBlockFaces(Level level, BlockPos pos, ParticleOptions p, IntProvider perFace) {
        RandomSource random = level.getRandom();
        Vec3 c = Vec3.atCenterOf(pos);
        for (Direction face : Direction.values()) {
            int count = perFace.sample(random);
            for (int i = 0; i < count; i++) {
                int sx = face.getStepX(), sy = face.getStepY(), sz = face.getStepZ();
                double x = c.x + (sx == 0 ? Mth.nextDouble(random, -0.5, 0.5) : sx * 0.55);
                double y = c.y + (sy == 0 ? Mth.nextDouble(random, -0.5, 0.5) : sy * 0.55);
                double z = c.z + (sz == 0 ? Mth.nextDouble(random, -0.5, 0.5) : sz * 0.55);
                double dx = sx == 0 ? Mth.nextDouble(random, -0.5, 0.5) : 0;
                double dy = sy == 0 ? Mth.nextDouble(random, -0.5, 0.5) : 0;
                double dz = sz == 0 ? Mth.nextDouble(random, -0.5, 0.5) : 0;
                particle(level, p, x, y, z, dx, dy, dz);
            }
        }
    }

    public static void spawnParticlesAlongAxis(Direction.Axis axis, Level level, BlockPos pos, double radius,
                                               ParticleOptions p, UniformInt count) {
        Vec3 c = Vec3.atCenterOf(pos);
        RandomSource random = level.getRandom();
        boolean ax = axis == Direction.Axis.X, ay = axis == Direction.Axis.Y, az = axis == Direction.Axis.Z;
        int n = count.sample(random);
        for (int i = 0; i < n; i++) {
            particle(level, p,
                    c.x + Mth.nextDouble(random, -1, 1) * (ax ? 0.5 : radius),
                    c.y + Mth.nextDouble(random, -1, 1) * (ay ? 0.5 : radius),
                    c.z + Mth.nextDouble(random, -1, 1) * (az ? 0.5 : radius),
                    ax ? Mth.nextDouble(random, -1, 1) : 0,
                    ay ? Mth.nextDouble(random, -1, 1) : 0,
                    az ? Mth.nextDouble(random, -1, 1) : 0);
        }
    }

    public static void spawnParticleBelow(Level level, BlockPos pos, RandomSource random, ParticleOptions p) {
        particle(level, p, pos.getX() + random.nextDouble(), pos.getY() - 0.05, pos.getZ() + random.nextDouble(), 0, 0, 0);
    }

    private IWUtils() {
    }
}
