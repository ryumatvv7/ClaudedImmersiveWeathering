package com.ordana.immersive_weathering.data.rute_tests;

import com.ordana.immersive_weathering.platform.IWUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.ordana.immersive_weathering.reg.ModRuleTests;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;

public class BurnableTest extends RuleTest {

    private static final BurnableTest INSTANCE = new BurnableTest();

    public static final Codec<BurnableTest> CODEC = MapCodec.unit(() -> INSTANCE).codec();

    public boolean test(BlockState state, net.minecraft.core.BlockPos pos, RandomSource random) {
        //hack since we don't have world and pos. hopefully mods aren't using those lol
        try {
            return IWUtils.getFlammability(state) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    protected RuleTestType<BurnableTest> getType() {
        return ModRuleTests.BURNABLE_TEST.get();
    }

}
