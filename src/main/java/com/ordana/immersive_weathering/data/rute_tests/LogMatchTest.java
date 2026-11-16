package com.ordana.immersive_weathering.data.rute_tests;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.ordana.immersive_weathering.reg.ModRuleTests;
import com.ordana.immersive_weathering.util.WeatheringHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;

public class LogMatchTest extends RuleTest {

    public static final LogMatchTest INSTANCE = new LogMatchTest();

    public static final Codec<LogMatchTest> CODEC = MapCodec.unit(() -> INSTANCE).codec();

    @Override
    public boolean test(BlockState state, net.minecraft.core.BlockPos pos, RandomSource random) {
        return WeatheringHelper.isLog(state);
    }

    @Override
    protected RuleTestType<LogMatchTest> getType() {
        return ModRuleTests.LOG_TEST.get();
    }

}
