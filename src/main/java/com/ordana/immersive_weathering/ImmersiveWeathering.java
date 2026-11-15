package com.ordana.immersive_weathering;

import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.data.block_growths.BlockGrowthHandler;
import com.ordana.immersive_weathering.data.fluid_generators.FluidGeneratorsHandler;
import com.ordana.immersive_weathering.data.fluid_generators.ModFluidGenerators;
import com.ordana.immersive_weathering.data.position_tests.ModPositionRuleTests;
import com.ordana.immersive_weathering.events.ModEvents;
import com.ordana.immersive_weathering.events.ModLootInjects;
import com.ordana.immersive_weathering.network.LeafDecayPayload;
import com.ordana.immersive_weathering.reg.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelValueEvents;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.mehvahdjukaar.moonlight.api.events.IFireConsumeBlockEvent;
import net.mehvahdjukaar.moonlight.api.events.ILightningStruckBlockEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common (client + dedicated server) entrypoint.
 * No client-only classes may be referenced from the "main" source set: everything client related
 * lives in the "client" source set and is started by its own entrypoint.
 * <p>
 * All gameplay logic (weathering, growths, melting...) runs on the logical server only.
 */
public class ImmersiveWeathering implements ModInitializer {

    public static final String MOD_ID = "immersive_weathering";

    public static final Logger LOGGER = LoggerFactory.getLogger("Immersive Weathering");

    public static Identifier res(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }

    @Override
    public void onInitialize() {
        commonInit();
    }

    public static void commonInit() {
        //server (synced) config. Values are sent from the server to every client by Moonlight
        CommonConfigs.init();

        ModCreativeTab.init();

        ModBlocks.init();
        ModItems.init();
        ModEntities.init();
        ModParticles.init();
        ModRuleTests.init();
        ModSoundEvents.init();

        //network: payload types have to be known on both sides
        LeafDecayPayload.register();

        //events
        MoonlightEventsHelper.addListener(ModEvents::onFireConsume, IFireConsumeBlockEvent.class);
        MoonlightEventsHelper.addListener(ModEvents::onLightningHit, ILightningStruckBlockEvent.class);
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) ->
                ModEvents.onBlockCLicked(player.getItemInHand(hand), player, level, hand, hitResult));

        RegHelper.addLootTableInjects(ModLootInjects::onLootInject);

        //data driven systems, reloaded with datapacks on the server
        PlatHelper.addServerReloadListener(BlockGrowthHandler::new, res("block_growths"));
        PlatHelper.addServerReloadListener(FluidGeneratorsHandler::new, res("fluid_generators"));

        PlatHelper.addCommonSetup(ImmersiveWeathering::setup);

        //dev only smoke test, does nothing unless -Diw.selfTest=true
        com.ordana.immersive_weathering.util.DevSelfTest.register();

        //burn times (replaces Moonlight's old FuelBlockItem)
        FuelValueEvents.BUILD.register((builder, context) ->
                ModBlocks.FUELS.forEach((item, time) -> builder.add(item.get(), time)));
    }

    public static void setup() {
        //dev only: force-applies every mixin so broken injection points fail at startup instead of in game
        if (Boolean.getBoolean("iw.mixinAudit")) {
            org.spongepowered.asm.mixin.MixinEnvironment.getCurrentEnvironment().audit();
            LOGGER.info("Mixin audit passed");
        }
        ModPositionRuleTests.register();
        ModFluidGenerators.register();
        ModCompostable.register();

        ModWaxables.getValues().forEach(OxidizableBlocksRegistry::registerWaxable);

        FlammableBlockRegistry flammable = FlammableBlockRegistry.getDefaultInstance();
        if (CommonConfigs.FLAMMABLE_COBWEBS.get()) {
            flammable.add(Blocks.COBWEB, 100, 100);
            flammable.add(Blocks.WHEAT, 100, 100);
        }
        //leaf piles register their own flammability; weeds only had Forge-only methods in the original
        flammable.add(ModBlocks.WEEDS.get(), 60, 100);
    }
}
