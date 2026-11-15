package com.ordana.immersive_weathering;

import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.reg.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.FuelValueEvents;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common (client + dedicated server) entrypoint.
 * No client-only classes may be referenced from the "main" source set: everything client related
 * lives in the "client" source set and is started by its own entrypoint.
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
        ModSoundEvents.init();

        PlatHelper.addCommonSetup(ImmersiveWeathering::setup);

        //burn times (replaces Moonlight's old FuelBlockItem)
        FuelValueEvents.BUILD.register((builder, context) ->
                ModBlocks.FUELS.forEach((item, time) -> builder.add(item.get(), time)));
    }

    public static void setup() {
        ModCompostable.register();
    }
}
