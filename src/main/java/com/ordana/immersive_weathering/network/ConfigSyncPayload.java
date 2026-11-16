package com.ordana.immersive_weathering.network;

import com.ordana.immersive_weathering.ImmersiveWeathering;
import com.ordana.immersive_weathering.platform.config.ModConfigHolder;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server -> client: values of a COMMON_SYNCED config, sent when a player joins.
 */
public record ConfigSyncPayload(Identifier config, String json) implements CustomPacketPayload {

    public static final Type<ConfigSyncPayload> TYPE = new Type<>(ImmersiveWeathering.res("config_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigSyncPayload> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, ConfigSyncPayload::config,
            ByteBufCodecs.stringUtf8(1 << 20), ConfigSyncPayload::json,
            ConfigSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().registerLarge(TYPE, CODEC, 1 << 20);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sendTo(handler.getPlayer()));
    }

    public static void sendTo(ServerPlayer player) {
        if (!ServerPlayNetworking.canSend(player, TYPE)) return;
        for (ModConfigHolder c : ModConfigHolder.syncedConfigs()) {
            ServerPlayNetworking.send(player, new ConfigSyncPayload(c.getId(), c.toSyncString()));
        }
    }
}
