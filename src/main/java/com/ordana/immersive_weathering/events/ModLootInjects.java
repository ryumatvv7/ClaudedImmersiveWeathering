package com.ordana.immersive_weathering.events;

import com.ordana.immersive_weathering.ImmersiveWeathering;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.HashMap;
import java.util.Map;

/**
 * Extra loot rolled together with some vanilla tables (replaces Moonlight's loot injects).
 * The inject tables are normal data files in data/immersive_weathering/loot_table/injects, so datapacks can edit them.
 */
public class ModLootInjects {

    private static final Map<Identifier, ResourceKey<LootTable>> INJECTS = new HashMap<>();

    static {
        String[] pairs = {
            "entities/blaze", "injects/blaze_ash",
            "entities/pig", "injects/pig_tallow",
            "entities/hoglin", "injects/hoglin_tallow",
            "chests/buried_treasure", "injects/buried_treasure",
            "chests/shipwreck_supply", "injects/shipwreck_supply",
            "chests/shipwreck_treasure", "injects/shipwreck_treasure",
            "gameplay/hero_of_the_village/farmer_gift", "injects/farmer_gift",
            "gameplay/hero_of_the_village/mason_gift", "injects/mason_gift",
            "gameplay/hero_of_the_village/toolsmith_gift", "injects/toolsmith_gift",
            "gameplay/hero_of_the_village/weaponsmith_gift", "injects/weaponsmith_gift"
        };
        for (int i = 0; i < pairs.length; i += 2) {
            INJECTS.put(Identifier.withDefaultNamespace(pairs[i]),
                    ResourceKey.create(Registries.LOOT_TABLE, ImmersiveWeathering.res(pairs[i + 1])));
        }
    }

    public static void register() {
        LootTableEvents.MODIFY_DROPS.register((table, context, drops) -> table.unwrapKey().ifPresent(key -> {
            ResourceKey<LootTable> inject = INJECTS.get(key.identifier());
            if (inject != null) {
                context.getLevel().getServer().reloadableRegistries().getLootTable(inject)
                        .getRandomItemsRaw(context, drops::add);
            }
        }));
    }
}
