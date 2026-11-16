package com.ordana.immersive_weathering.util;

import com.mojang.serialization.Codec;
import com.ordana.immersive_weathering.ImmersiveWeathering;
import com.ordana.immersive_weathering.blocks.cracked.Crackable;
import com.ordana.immersive_weathering.blocks.mossy.Mossable;
import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.reg.ModBlocks;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Port addition: abandonment. Every chunk remembers when players were last nearby. Random ticks don't run
 * in unloaded chunks, so when a chunk nobody visited for a while is loaded again, it "catches up" at once:
 * stone gets mossy and cracked, ivy climbs sunlit walls, cobwebs fill dark corners, exposed wood rots.
 */
public final class Abandonment {

    private static final AttachmentType<Long> LAST_VISIT = AttachmentRegistry.create(
            ImmersiveWeathering.res("last_visit"), b -> b.persistent(Codec.LONG));

    private static final int VISIT_RADIUS = 3; //chunks around a player that count as visited
    private static final int STEPS_PER_DAY = 40;
    private static final int CHUNKS_PER_TICK = 2;

    private record Pending(ServerLevel level, ChunkPos pos) {
    }

    private static final Deque<Pending> QUEUE = new ArrayDeque<>();

    public static void init() {
        //block changes are deferred to the server tick: changing blocks while a chunk loads isn't safe
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) -> {
            if (CommonConfigs.ABANDONMENT.get() && chunk.hasAttached(LAST_VISIT)) QUEUE.add(new Pending(level, chunk.getPos()));
        });
        ServerTickEvents.END_SERVER_TICK.register(Abandonment::tick);
    }

    private static void tick(MinecraftServer server) {
        for (int i = 0; i < CHUNKS_PER_TICK && !QUEUE.isEmpty(); i++) {
            Pending p = QUEUE.poll();
            LevelChunk c = p.level().getChunkSource().getChunkNow(p.pos().x(), p.pos().z());
            if (c != null) catchUp(p.level(), c);
        }
        if (server.getTickCount() % 100 != 0) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.isSpectator()) continue;
            ServerLevel level = player.level();
            ChunkPos cp = player.chunkPosition();
            long now = level.getGameTime();
            for (int dx = -VISIT_RADIUS; dx <= VISIT_RADIUS; dx++)
                for (int dz = -VISIT_RADIUS; dz <= VISIT_RADIUS; dz++) {
                    LevelChunk c = level.getChunkSource().getChunkNow(cp.x() + dx, cp.z() + dz);
                    if (c != null) markVisited(c, now);
                }
        }
    }

    private static void markVisited(LevelChunk c, long now) {
        Long old = c.getAttached(LAST_VISIT);
        if (old == null || old != now) {
            c.setAttached(LAST_VISIT, now);
            c.markUnsaved();
        }
    }

    private static void catchUp(ServerLevel level, LevelChunk c) {
        Long last = c.getAttached(LAST_VISIT);
        if (last == null) return;
        long now = level.getGameTime();
        int days = (int) ((now - last) / 24000L);
        markVisited(c, now); //don't decay twice for the same absence
        int threshold = CommonConfigs.ABANDONMENT_DAYS.get();
        if (days < threshold) return;
        decay(level, c, days - threshold + 1);
    }

    /** Applies the decay of {@code days} abandoned days to the chunk. Returns the number of changed blocks. */
    public static int decay(ServerLevel level, LevelChunk c, int days) {
        RandomSource r = level.getRandom();
        int steps = (int) Math.min(CommonConfigs.ABANDONMENT_MAX.get(), (long) days * STEPS_PER_DAY);
        int changed = 0;
        int minX = c.getPos().getMinBlockX(), minZ = c.getPos().getMinBlockZ();
        int tries = steps * 6;
        for (int t = 0; t < tries && changed < steps; t++) {
            int x = minX + r.nextInt(16), z = minZ + r.nextInt(16);
            int top = c.getHeight(Heightmap.Types.WORLD_SURFACE, x & 15, z & 15) + 1;
            int bottom = Math.max(level.getMinY(), top - 48);
            BlockPos pos = new BlockPos(x, bottom + r.nextInt(Math.max(1, top - bottom + 1)), z);
            if (decayAt(level, pos, r)) changed++;
        }
        return changed;
    }

    private static boolean decayAt(ServerLevel level, BlockPos pos, RandomSource r) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return fillAir(level, pos, r);
        if (!exposed(level, pos) || Varnish.isVarnished(level, pos)) return false;
        Block b = state.getBlock();

        if (b == Blocks.STONE) return false; //natural terrain stays as it is
        boolean canMoss = Mossable.MOSS_LEVEL_INCREASES.get().containsKey(b);
        boolean canCrack = Crackable.CRACK_LEVEL_INCREASES.get().containsKey(b);
        if (canMoss || canCrack) {
            BlockState n = canMoss && (!canCrack || r.nextBoolean())
                    ? Mossable.getMossyBlock(state) : Crackable.getCrackedBlock(state);
            return set(level, pos, n);
        }
        if (CommonConfigs.WOOD_ROT.get() && WoodWeathering.isWeatherableWood(b) && r.nextFloat() < 0.4f) {
            BlockState n = WoodWeathering.getRotten(state);
            return n != null && set(level, pos, n);
        }
        return false;
    }

    //ivy on sunlit walls, moss on floors and walls, cobwebs in dark corners
    private static boolean fillAir(ServerLevel level, BlockPos pos, RandomSource r) {
        int solid = 0;
        Direction support = null;
        for (Direction d : Direction.values()) {
            BlockPos o = pos.relative(d);
            BlockState s = level.getBlockState(o);
            if (s.isFaceSturdy(level, o, d.getOpposite())) {
                solid++;
                if (isBuilt(s) && !Varnish.isVarnished(level, o) && (support == null || r.nextInt(3) == 0)) support = d;
            }
        }
        if (support == null) return false;
        boolean sky = level.canSeeSky(pos);
        if (!sky && solid >= 3 && level.getBrightness(LightLayer.BLOCK, pos) < 4 && level.getBrightness(LightLayer.SKY, pos) < 8
                && r.nextFloat() < 0.35f) {
            return set(level, pos, Blocks.COBWEB.defaultBlockState());
        }
        Block face = sky && support.getAxis().isHorizontal() ? ModBlocks.IVY.get() : ModBlocks.MOSS.get();
        BlockState s = face.defaultBlockState().setValue(MultifaceBlock.getFaceProperty(support), true);
        return s.canSurvive(level, pos) && set(level, pos, s);
    }

    //only things players (or structures) build get overgrown, not natural terrain
    private static boolean isBuilt(BlockState s) {
        Block b = s.getBlock();
        if (b == Blocks.STONE || b == ModBlocks.MOSSY_STONE.get()) return false;
        return Mossable.MOSS_LEVEL_INCREASES.get().containsKey(b) || Mossable.MOSS_LEVEL_DECREASES.get().containsKey(b)
                || Crackable.CRACK_LEVEL_INCREASES.get().containsKey(b) || Crackable.CRACK_LEVEL_INCREASES.get().containsValue(b)
                || WoodWeathering.isWeatherableWood(b) || WoodWeathering.isRotten(b);
    }

    private static boolean exposed(ServerLevel level, BlockPos pos) {
        for (Direction d : Direction.values()) if (level.getBlockState(pos.relative(d)).isAir()) return true;
        return false;
    }

    private static boolean set(ServerLevel level, BlockPos pos, BlockState s) {
        if (s == null || s == level.getBlockState(pos)) return false;
        return level.setBlock(pos, s, Block.UPDATE_CLIENTS);
    }

    /** For /iw abandon: pretend the chunk was left alone for {@code days} days. */
    public static int age(ServerLevel level, BlockPos pos, int days) {
        LevelChunk c = level.getChunkAt(pos);
        markVisited(c, level.getGameTime());
        return decay(level, c, days);
    }

    private Abandonment() {
    }
}
