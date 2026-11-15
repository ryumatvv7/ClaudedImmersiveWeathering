package com.ordana.immersive_weathering.network;

import com.ordana.immersive_weathering.ImmersiveWeathering;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Server -> client: leaves at a position decayed, show particles / play the sound.
 * Replaces the old Moonlight channel message (SendCustomParticlesPacket). The payload is plain data and
 * lives in the common source set; the handler that touches client classes is registered from the
 * client entrypoint only.
 */
public record LeafDecayPayload(BlockPos pos, int stateId) implements CustomPacketPayload {

    public static final Type<LeafDecayPayload> TYPE = new Type<>(ImmersiveWeathering.res("leaf_decay"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LeafDecayPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, LeafDecayPayload::pos,
            ByteBufCodecs.VAR_INT, LeafDecayPayload::stateId,
            LeafDecayPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public BlockState state() {
        return Block.stateById(stateId);
    }

    //called on both sides from the common entrypoint
    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
    }

    public static void sendAround(ServerLevel level, BlockPos pos, BlockState state, double range) {
        LeafDecayPayload payload = new LeafDecayPayload(pos, Block.getId(state));
        for (ServerPlayer player : PlayerLookup.around(level, pos, range)) {
            if (ServerPlayNetworking.canSend(player, TYPE)) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }
}
