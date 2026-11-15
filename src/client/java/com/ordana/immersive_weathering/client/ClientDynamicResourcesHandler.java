package com.ordana.immersive_weathering.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ordana.immersive_weathering.ImmersiveWeathering;
import com.ordana.immersive_weathering.reg.ModBlocks;
import com.ordana.immersive_weathering.reg.ModItems;
import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.StaticResource;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicClientResourceProvider;
import net.mehvahdjukaar.moonlight.api.resources.pack.PackGenerationStrategy;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceGenTask;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceSink;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Blocks;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Generates the client assets of leaf piles and bark for every wood / leaves type that has no hand made
 * assets in the mod (that is: types added by other mods), using the oak files as templates.
 * <p>
 * Rewritten for the port on top of the public Moonlight dynamic pack API. Simplified compared to the
 * original: the "heavy" leaf pile uses the plain leaves texture and the bark icon uses the log texture
 * (the original recolored templates with palette helpers that changed in the new Moonlight).
 */
public class ClientDynamicResourcesHandler extends DynamicClientResourceProvider {

    private static final String[] HEIGHTS = {"1", "2", "4", "6", "8", "10", "12", "14", "16"};

    public static void init() {
        RegHelper.registerDynamicResourceProvider(new ClientDynamicResourcesHandler());
    }

    private ClientDynamicResourcesHandler() {
        super(ImmersiveWeathering.res("generated_pack"), PackGenerationStrategy.REGEN_ON_EVERY_RELOAD);
    }

    @Override
    protected Collection<String> gatherSupportedNamespaces() {
        return List.of(ImmersiveWeathering.MOD_ID);
    }

    public static final Predicate<String> LOOKS_LIKE_LEAF_TEXTURE = s -> {
        s = Identifier.parse(s).getPath();
        return !s.contains("_bushy") && !s.contains("_snow") && !s.contains("_overlay");
    };

    @Override
    protected void regenerateDynamicAssets(Consumer<ResourceGenTask> executor) {
        executor.accept(this::generateLeafPiles);
        executor.accept(this::generateBark);
    }

    private void generateLeafPiles(ResourceManager manager, ResourceSink sink) {
        StaticResource blockState = StaticResource.getOrLog(manager, ResType.BLOCKSTATES.getPath(ImmersiveWeathering.res("oak_leaf_pile")));
        StaticResource itemModel = StaticResource.getOrLog(manager, ResType.ITEM_MODELS.getPath(ImmersiveWeathering.res("oak_leaf_pile")));
        if (blockState == null || itemModel == null) return;

        ModBlocks.LEAF_PILES.forEach((type, pile) -> {
            Identifier pileId = Utils.getID(pile);
            //hand made assets exist (vanilla types): nothing to do
            if (sink.alreadyHasAssetAtLocation(manager, pileId, ResType.BLOCKSTATES)) return;
            String id = pileId.getPath();
            try {
                Identifier leaves;
                try {
                    leaves = RPUtils.findFirstBlockTextureLocation(manager, type.leaves, LOOKS_LIKE_LEAF_TEXTURE);
                } catch (Exception e) {
                    ImmersiveWeathering.LOGGER.warn("Failed to find texture for Leaf Pile {}, using oak one instead", pile);
                    leaves = RPUtils.findFirstBlockTextureLocation(manager, Blocks.OAK_LEAVES, s -> true);
                }
                String texture = leaves.toString();

                sink.addSimilarJsonResource(manager, blockState, "oak_leaf_pile", id);
                sink.addSimilarJsonResource(manager, itemModel, s -> retexture(s, texture).replace("oak_leaf_pile", id),
                        s -> s.replace("oak_leaf_pile", id));
                for (String h : HEIGHTS) {
                    StaticResource model = StaticResource.getOrLog(manager,
                            ResType.BLOCK_MODELS.getPath(ImmersiveWeathering.res("leaf_piles/oak_leaf_pile_height" + h)));
                    if (model != null) {
                        sink.addSimilarJsonResource(manager, model, s -> retexture(s, texture).replace("oak_leaf_pile", id),
                                s -> s.replace("oak_leaf_pile", id));
                    }
                }

                //item definition, with the same tint the leaves item uses
                JsonObject model = new JsonObject();
                model.addProperty("type", "minecraft:model");
                model.addProperty("model", ImmersiveWeathering.MOD_ID + ":item/" + id);
                JsonElement tints = findLeavesItemTints(manager, Utils.getID(type.leaves));
                if (tints != null) model.add("tints", tints);
                JsonObject def = new JsonObject();
                def.add("model", model);
                sink.addItemModelDefinition(pileId, def);
            } catch (Exception ex) {
                ImmersiveWeathering.LOGGER.error("Failed to generate Leaf Pile assets for {}", pile, ex);
            }
        });
    }

    private static String retexture(String json, String texture) {
        return json.replaceAll("immersive_weathering:block/(light|medium|heavy)_oak_leaves", texture);
    }

    private static JsonElement findLeavesItemTints(ResourceManager manager, Identifier leavesId) {
        try {
            var res = manager.getResource(ResType.ITEMS.getPath(leavesId));
            if (res.isEmpty()) return null;
            try (var stream = res.get().open()) {
                JsonObject json = RPUtils.deserializeJson(stream);
                JsonObject m = json.getAsJsonObject("model");
                return m != null && m.has("tints") ? m.get("tints") : null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private void generateBark(ResourceManager manager, ResourceSink sink) {
        ModItems.BARK.forEach((type, bark) -> {
            Identifier barkId = Utils.getID(bark);
            if (sink.alreadyHasAssetAtLocation(manager, barkId, ResType.ITEMS)) return;
            try {
                Identifier log = RPUtils.findFirstBlockTextureLocation(manager, type.log, s -> !s.contains("top"));
                JsonObject textures = new JsonObject();
                textures.addProperty("layer0", log.toString());
                JsonObject itemModel = new JsonObject();
                itemModel.addProperty("parent", "minecraft:item/generated");
                itemModel.add("textures", textures);
                sink.addItemModel(barkId, itemModel);

                JsonObject model = new JsonObject();
                model.addProperty("type", "minecraft:model");
                model.addProperty("model", barkId.getNamespace() + ":item/" + barkId.getPath());
                JsonObject def = new JsonObject();
                def.add("model", model);
                sink.addItemModelDefinition(barkId, def);
            } catch (Exception ex) {
                ImmersiveWeathering.LOGGER.error("Failed to generate Bark assets for {}", bark, ex);
            }
        });
    }

    @Override
    protected void addDynamicTranslations(AfterLanguageLoadEvent lang) {
        ModItems.BARK.forEach((type, bark) ->
                LangBuilder.addDynamicEntry(lang, "item.immersive_weathering.bark", type, bark));
        ModBlocks.LEAF_PILES.forEach((type, leaf) ->
                LangBuilder.addDynamicEntry(lang, "block.immersive_weathering.leaf_pile", type, leaf));
    }
}
