package com.ordana.immersive_weathering.util;

import com.mojang.serialization.Codec;
import com.ordana.immersive_weathering.ImmersiveWeathering;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;

/**
 * Port addition: varnished wood doesn't rot or grow moss. The varnished positions are stored per chunk
 * (saved with the world), so any wooden block can be protected without extra "varnished" block variants.
 */
public final class Varnish {

    private static final AttachmentType<List<Long>> VARNISHED = AttachmentRegistry.create(
            ImmersiveWeathering.res("varnished"), b -> b.persistent(Codec.LONG.listOf()));

    public static void init() {
        //breaking a block removes its varnish (so a new block placed there isn't protected)
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> remove(level, pos));
    }

    public static boolean isVarnished(Level level, BlockPos pos) {
        List<Long> l = level.getChunkAt(pos).getAttached(VARNISHED);
        return l != null && l.contains(pos.asLong());
    }

    /** @return false if it was already varnished */
    public static boolean add(Level level, BlockPos pos) {
        LevelChunk c = level.getChunkAt(pos);
        List<Long> l = c.getAttached(VARNISHED);
        long key = pos.asLong();
        if (l != null && l.contains(key)) return false;
        List<Long> n = l == null ? new ArrayList<>() : new ArrayList<>(l);
        n.add(key);
        c.setAttached(VARNISHED, List.copyOf(n));
        c.markUnsaved();
        return true;
    }

    public static void remove(Level level, BlockPos pos) {
        LevelChunk c = level.getChunkAt(pos);
        List<Long> l = c.getAttached(VARNISHED);
        if (l == null || !l.contains(pos.asLong())) return;
        List<Long> n = new ArrayList<>(l);
        n.remove(pos.asLong());
        if (n.isEmpty()) c.removeAttached(VARNISHED);
        else c.setAttached(VARNISHED, List.copyOf(n));
        c.markUnsaved();
    }

    private Varnish() {
    }
}
