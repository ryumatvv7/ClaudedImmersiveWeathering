package com.ordana.immersive_weathering.dynamicpack;

import com.ordana.immersive_weathering.ImmersiveWeathering;
import com.ordana.immersive_weathering.reg.ModBlocks;
import com.ordana.immersive_weathering.reg.ModItems;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.SimpleTagBuilder;
import net.mehvahdjukaar.moonlight.api.resources.StaticResource;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicServerResourceProvider;
import net.mehvahdjukaar.moonlight.api.resources.pack.PackGenerationStrategy;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceGenTask;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceSink;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Server data for dynamically registered leaf piles and bark: tags for all of them, and loot table + recipe
 * (from the oak templates) for the ones without hand made data, i.e. types added by other mods.
 * Runs on the logical server, so it works the same on a dedicated server and in single player.
 */
public class ServerDynamicResourcesHandler extends DynamicServerResourceProvider {

    public static void init() {
        RegHelper.registerDynamicResourceProvider(new ServerDynamicResourcesHandler());
    }

    private ServerDynamicResourcesHandler() {
        super(ImmersiveWeathering.res("generated_pack"), PackGenerationStrategy.REGEN_ON_EVERY_RELOAD);
    }

    @Override
    protected Collection<String> gatherSupportedNamespaces() {
        return List.of(ImmersiveWeathering.MOD_ID);
    }

    @Override
    protected void regenerateDynamicAssets(Consumer<ResourceGenTask> executor) {
        executor.accept(this::generate);
    }

    private void generate(ResourceManager manager, ResourceSink sink) {
        SimpleTagBuilder piles = SimpleTagBuilder.of(ImmersiveWeathering.res("leaf_piles"));
        ModBlocks.LEAF_PILES.values().forEach(b -> piles.add(Utils.getID(b)));
        sink.addTag(piles, Registries.BLOCK);

        SimpleTagBuilder bark = SimpleTagBuilder.of(ImmersiveWeathering.res("bark"));
        ModItems.BARK.values().forEach(i -> bark.add(Utils.getID(i)));
        sink.addTag(bark, Registries.ITEM);

        StaticResource lootTable = StaticResource.getOrLog(manager, ResType.BLOCK_LOOT_TABLES.getPath(ImmersiveWeathering.res("oak_leaf_pile")));
        StaticResource recipe = StaticResource.getOrLog(manager, ResType.RECIPES.getPath(ImmersiveWeathering.res("oak_leaf_pile")));

        ModBlocks.LEAF_PILES.forEach((type, pile) -> {
            Identifier pileId = Utils.getID(pile);
            String id = pileId.getPath();
            String leavesId = Utils.getID(type.leaves).toString();
            try {
                //addSimilarJsonResource skips files that already exist (vanilla types have hand made ones)
                if (lootTable != null) {
                    sink.addSimilarJsonResource(manager, lootTable,
                            s -> s.replace("oak_leaf_pile", id).replace("minecraft:oak_leaves", leavesId),
                            s -> s.replace("oak_leaf_pile", id));
                }
                if (recipe != null) {
                    sink.addSimilarJsonResource(manager, recipe,
                            s -> s.replace("oak_leaf_pile", id).replace("minecraft:oak_leaves", leavesId),
                            s -> s.replace("oak_leaf_pile", id));
                }
            } catch (Exception ex) {
                ImmersiveWeathering.LOGGER.error("Failed to generate Leaf Pile data for {}", pile, ex);
            }
        });
    }
}
