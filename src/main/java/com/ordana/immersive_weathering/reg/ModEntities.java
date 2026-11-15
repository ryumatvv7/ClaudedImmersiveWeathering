package com.ordana.immersive_weathering.reg;

import com.ordana.immersive_weathering.ImmersiveWeathering;
import com.ordana.immersive_weathering.entities.FallingIcicleEntity;
import com.ordana.immersive_weathering.entities.FallingLayerEntity;
import com.ordana.immersive_weathering.entities.FallingPropaguleEntity;
import com.ordana.immersive_weathering.entities.IcicleBlockEntity;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class ModEntities {

    public static void init() {
    }

    //entities
    public static Supplier<EntityType<FallingIcicleEntity>> FALLING_ICICLE = RegHelper.registerEntityType(
            ImmersiveWeathering.res("falling_icicle"),
            EntityType.Builder.<FallingIcicleEntity>of(FallingIcicleEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F).clientTrackingRange(10).updateInterval(20));

    public static Supplier<EntityType<FallingLayerEntity>> FALLING_LAYER = RegHelper.registerEntityType(
            ImmersiveWeathering.res("falling_layer"),
            EntityType.Builder.<FallingLayerEntity>of(FallingLayerEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F).clientTrackingRange(10).updateInterval(20));

    public static Supplier<EntityType<FallingPropaguleEntity>> FALLING_PROPAGULE = RegHelper.registerEntityType(
            ImmersiveWeathering.res("falling_propagule"),
            EntityType.Builder.<FallingPropaguleEntity>of(FallingPropaguleEntity::new, MobCategory.MISC)
                    .sized(0.28F, 0.98F).clientTrackingRange(10).updateInterval(20));


    //block entities

    public static final Supplier<BlockEntityType<IcicleBlockEntity>> ICICLE_TILE = RegHelper.registerBlockEntityType(
            ImmersiveWeathering.res("icicle"),
            () -> PlatHelper.newBlockEntityType(IcicleBlockEntity::new, ModBlocks.ICICLE.get()));


}
