package com.ordana.immersive_weathering.client;

import com.ordana.immersive_weathering.ImmersiveWeathering;
import com.ordana.immersive_weathering.blocks.LeafPileBlock;
import com.ordana.immersive_weathering.client.particles.ModParticleProviders;
import com.ordana.immersive_weathering.configs.ClientConfigs;
import com.ordana.immersive_weathering.network.LeafDecayPayload;
import com.ordana.immersive_weathering.reg.ModBlocks;
import com.ordana.immersive_weathering.reg.ModEntities;
import com.ordana.immersive_weathering.reg.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import com.ordana.immersive_weathering.util.VanillaTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Client-only entrypoint. Everything rendering related lives in the "client" source set,
 * so a dedicated server never loads it.
 * <p>
 * Block render layers (cutout / translucent) are no longer registered by code: since 26.1 the game
 * picks them from the transparency of the textures. Item tints live in the item model json.
 */
public class ImmersiveWeatheringClient implements ClientModInitializer {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void onInitializeClient() {
        //per player visual options
        ClientConfigs.init();

        //optional built-in resource pack (off by default)
        net.fabricmc.fabric.api.resource.v1.ResourceLoader.registerBuiltinPack(ImmersiveWeathering.res("visual_waxed_iron_items"),
                net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer(ImmersiveWeathering.MOD_ID).orElseThrow(),
                net.fabricmc.fabric.api.resource.v1.pack.PackActivationType.NORMAL);

        EntityRendererRegistry.register((net.minecraft.world.entity.EntityType) ModEntities.FALLING_ICICLE.get(), net.minecraft.client.renderer.entity.FallingBlockRenderer::new);
        EntityRendererRegistry.register((net.minecraft.world.entity.EntityType) ModEntities.FALLING_LAYER.get(), net.minecraft.client.renderer.entity.FallingBlockRenderer::new);
        EntityRendererRegistry.register((net.minecraft.world.entity.EntityType) ModEntities.FALLING_PROPAGULE.get(), net.minecraft.client.renderer.entity.FallingBlockRenderer::new);

        registerParticles();

        registerBlockColors();

        //server tells us leaves decayed somewhere nearby: purely visual, runs on the client thread
        //server config values replace the local ones while connected
        ClientPlayNetworking.registerGlobalReceiver(com.ordana.immersive_weathering.network.ConfigSyncPayload.TYPE, (payload, context) ->
                com.ordana.immersive_weathering.platform.config.ModConfigHolder.syncedConfigs().stream()
                        .filter(c -> c.getId().equals(payload.config())).forEach(c -> c.acceptSynced(payload.json())));
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                com.ordana.immersive_weathering.platform.config.ModConfigHolder.syncedConfigs()
                        .forEach(com.ordana.immersive_weathering.platform.config.ModConfigHolder::forceLoad));

        ClientPlayNetworking.registerGlobalReceiver(LeafDecayPayload.TYPE, (payload, context) -> {
            ClientLevel level = context.client().level;
            if (level != null) spawnLeafDecayEffects(level, payload.pos(), payload.state());
        });
    }

    private static void registerParticles() {
        ParticleProviderRegistry r = ParticleProviderRegistry.getInstance();
        Set<Supplier<SimpleParticleType>> done = new HashSet<>();

        r.register(ModParticles.EMBERSPARK.get(), ModParticleProviders.EmberProvider::new);
        r.register(ModParticles.SCRAPE_RUST.get(), ModParticleProviders.ScrapeRustProvider::new);
        r.register(ModParticles.MOSS.get(), ModParticleProviders.NormalGravityProvider::new);
        r.register(ModParticles.GRAVITY_AZALEA_FLOWER.get(), ModParticleProviders.NormalGravityProvider::new);
        done.addAll(List.of(ModParticles.EMBERSPARK, ModParticles.SCRAPE_RUST, ModParticles.MOSS, ModParticles.GRAVITY_AZALEA_FLOWER));

        //azalea flower and all the flower crown variants are falling petals
        for (Supplier<SimpleParticleType> p : ModParticles.ALL) {
            if (!done.contains(p)) r.register(p.get(), ModParticleProviders.LeafProvider::new);
        }
    }

    private static void registerBlockColors() {
        //tint index 0 is untinted (dirt / frost overlay), tint index 1 is the grass color
        BlockColorRegistry.register(List.of(BlockTintSources.constant(-1), BlockTintSources.grassBlock()),
                ModBlocks.GRASSY_PERMAFROST.get(),
                ModBlocks.GRASSY_SILT.get(),
                ModBlocks.GRASSY_EARTHEN_CLAY.get(),
                ModBlocks.GRASSY_SANDY_DIRT.get(),
                ModBlocks.ROOTED_GRASS_BLOCK.get(),
                ModBlocks.FROSTY_GRASS.get(),
                ModBlocks.FROSTY_FERN.get());

        BlockColorRegistry.register(List.of(BlockTintSources.water()), ModBlocks.PUDDLE.get());

        ModBlocks.LEAF_PILES.forEach((type, pile) ->
                BlockColorRegistry.register(List.of(new LeafPileTint(type)), pile));
    }

    /**
     * Leaf piles take the color of their leaves and turn brown with age.
     */
    private record LeafPileTint(VanillaTypes.Leaves type) implements BlockTintSource {
        private static final int BROWN = 0x7D5212;

        private BlockTintSource leaves() {
            return Minecraft.getInstance().getBlockColors().getTintSource(type.leaves.defaultBlockState(), 0);
        }

        private static int age(BlockState state, int original) {
            float percentage = state.getValue(LeafPileBlock.AGE) / 10f;
            if (percentage == 0) return original;
            return 0xFF000000 | (net.minecraft.util.ARGB.srgbLerp(percentage, original, BROWN) & 0xFFFFFF);
        }

        @Override
        public int color(BlockState state) {
            BlockTintSource l = leaves();
            return age(state, l == null ? -1 : l.color(type.leaves.defaultBlockState()));
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            BlockTintSource l = leaves();
            return age(state, l == null ? -1 : l.colorInWorld(type.leaves.defaultBlockState(), level, pos));
        }

        @Override
        public Set<Property<?>> relevantProperties() {
            return Set.of(LeafPileBlock.AGE);
        }
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
