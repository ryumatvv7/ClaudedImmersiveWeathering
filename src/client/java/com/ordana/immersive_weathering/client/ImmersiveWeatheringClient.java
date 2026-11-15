package com.ordana.immersive_weathering.client;

import com.ordana.immersive_weathering.reg.ModEntities;
import com.ordana.immersive_weathering.reg.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.mehvahdjukaar.moonlight.api.client.renderer.FallingBlockRendererGeneric;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.core.particles.SimpleParticleType;

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
        EntityRendererRegistry.register(ModEntities.FALLING_ICICLE.get(), FallingBlockRendererGeneric::new);
        EntityRendererRegistry.register(ModEntities.FALLING_LAYER.get(), FallingBlockRendererGeneric::new);
        EntityRendererRegistry.register(ModEntities.FALLING_PROPAGULE.get(), FallingBlockRendererGeneric::new);

        //TODO stage 4: port the custom particle classes (ember, leaf, gravity). For now every mod particle
        // uses a simple vanilla behaviour with its own sprites so nothing is missing on the client
        for (Supplier<SimpleParticleType> p : ModParticles.ALL) {
            ParticleProviderRegistry.getInstance().register(p.get(), FlameParticle.Provider::new);
        }
    }
}
