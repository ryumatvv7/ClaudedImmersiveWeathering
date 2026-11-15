package com.ordana.immersive_weathering.client;

import com.ordana.immersive_weathering.configs.ClientConfigs;
import com.ordana.immersive_weathering.network.LeafDecayPayload;
import com.ordana.immersive_weathering.reg.ModEntities;
import com.ordana.immersive_weathering.reg.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.mehvahdjukaar.moonlight.api.client.renderer.FallingBlockRendererGeneric;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * Client-only entrypoint. Everything rendering related lives in the "client" source set,
 * so a dedicated server never loads it.
 * <p>
 * Block render layers (cutout / translucent) are no longer registered by code: since 26.1 the game
 * picks them from the transparency of the textures.
 */
public class ImmersiveWeatheringClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        //per player visual options
        ClientConfigs.init();

        EntityRendererRegistry.register(ModEntities.FALLING_ICICLE.get(), FallingBlockRendererGeneric::new);
        EntityRendererRegistry.register(ModEntities.FALLING_LAYER.get(), FallingBlockRendererGeneric::new);
        EntityRendererRegistry.register(ModEntities.FALLING_PROPAGULE.get(), FallingBlockRendererGeneric::new);

        //TODO stage 4: port the custom particle classes (ember, leaf, gravity). For now every mod particle
        // uses a simple vanilla behaviour with its own sprites so nothing is missing on the client
        for (Supplier<SimpleParticleType> p : ModParticles.ALL) {
            ParticleProviderRegistry.getInstance().register(p.get(), FlameParticle.Provider::new);
        }

        //server tells us leaves decayed somewhere nearby: purely visual, runs on the client thread
        ClientPlayNetworking.registerGlobalReceiver(LeafDecayPayload.TYPE, (payload, context) -> {
            ClientLevel level = context.client().level;
            if (level != null) spawnLeafDecayEffects(level, payload.pos(), payload.state());
        });
    }

    private static void spawnLeafDecayEffects(ClientLevel level, BlockPos pos, BlockState state) {
        if (ClientConfigs.LEAF_DECAY_PARTICLES.get()) {
            var leafParticle = new BlockParticleOption(ParticleTypes.BLOCK, state);
            for (int i = 0; i < 20; i++) {
                double x = pos.getX() + level.getRandom().nextDouble();
                double y = pos.getY() - 0.05;
                double z = pos.getZ() + level.getRandom().nextDouble();
                level.addParticle(leafParticle, x, y, z, 0.0, 0.0, 0.0);
            }
        }
        if (ClientConfigs.LEAF_DECAY_SOUND.get()) {
            level.playLocalSound(pos, SoundEvents.AZALEA_LEAVES_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f, false);
        }
    }
}
