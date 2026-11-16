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
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
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
        //server (synced) config. Values are sent to every client when it joins (ConfigSyncPayload)
        CommonConfigs.init();

        ModBlocks.init();
        ModItems.init();
        ModEntities.init();
        ModParticles.init();
        ModRuleTests.init();
        ModFeatures.init();
        ModSoundEvents.init();

        //network: payload types have to be known on both sides
        LeafDecayPayload.register();
        com.ordana.immersive_weathering.network.ConfigSyncPayload.register();
        com.ordana.immersive_weathering.commands.IWCommand.register();

        //events
        //fire and lightning events come from FireMixin / LightningBoltMixin
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) ->
                ModEvents.onBlockCLicked(player.getItemInHand(hand), player, level, hand, hitResult));

        ModLootInjects.register();

        //data driven systems, reloaded with datapacks on the server
        var serverData = net.fabricmc.fabric.api.resource.v1.ResourceLoader.get(net.minecraft.server.packs.PackType.SERVER_DATA);
        serverData.registerReloadListener(res("block_growths"), new BlockGrowthHandler());
        serverData.registerReloadListener(res("fluid_generators"), new FluidGeneratorsHandler());

        //everything is queued above: register it now, then do what needs registered objects
        com.ordana.immersive_weathering.platform.Reg.flush();
        ModBlocks.afterRegistration();
        ModItems.afterRegistration();
        ModCreativeTab.init();
        setup();

        //dev only smoke test, does nothing unless -Diw.selfTest=true
        com.ordana.immersive_weathering.util.DevSelfTest.register();
        //composting chances and burn times (item components)
        ModCompostable.register();
    }

    public static void setup() {
        //dev only: force-applies every mixin so broken injection points fail at startup instead of in game
        if (Boolean.getBoolean("iw.mixinAudit")) {
            org.spongepowered.asm.mixin.MixinEnvironment.getCurrentEnvironment().audit();
            LOGGER.info("Mixin audit passed");
        }
        ModPositionRuleTests.register();
        ModFluidGenerators.register();
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
