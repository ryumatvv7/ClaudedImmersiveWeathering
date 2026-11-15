package com.ordana.immersive_weathering;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common (client + dedicated server) entrypoint.
 * No client-only classes may be referenced from the "main" source set.
 */
public class ImmersiveWeathering implements ModInitializer {

    public static final String MOD_ID = "immersive_weathering";

    public static final Logger LOGGER = LoggerFactory.getLogger("Immersive Weathering");

    public static Identifier res(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Immersive Weathering (26.1.2 Fabric port) initializing");
    }
}
