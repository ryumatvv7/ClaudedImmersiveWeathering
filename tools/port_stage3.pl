#!/usr/bin/perl
# One-off fixes for the data driven systems (block growths, fluid generators, position tests).
use strict; use warnings;
my $root = 'src/main/java/com/ordana/immersive_weathering';

sub patch {
    my ($rel, $code) = @_;
    my $file = "$root/$rel";
    local $/; open(my $fh, '<', $file) or die "$file: $!"; my $s = <$fh>; close $fh;
    my $o = $s;
    local $_ = $s; $code->(); $s = $_;
    if ($s ne $o) { open(my $out, '>', $file) or die; print $out $s; close $out; }
    else { print "UNCHANGED $rel\n"; }
}
sub add_import {
    my ($fq) = @_;
    return if /import \Q$fq\E;/;
    s/^(package [\w.]+;\n)/$1\nimport $fq;/m;
}
my $RL = 'com.ordana.immersive_weathering.util.RegistryJsonReloadListener';

# --- dispatch codecs
patch('data/position_tests/IPositionRuleTest.java', sub {
    s/\.dispatch\("type", IPositionRuleTest::getType, IPositionRuleTest\.Type::codec\)/.dispatch("type", IPositionRuleTest::getType, t -> RegistryJsonReloadListener.asMapCodec(t.codec()))/;
    add_import($RL);
});
patch('data/block_growths/area_condition/AreaCondition.java', sub {
    s/\.dispatch\("type", AreaCondition::getType, AreaConditionType::codec\)/.dispatch("type", AreaCondition::getType, t -> RegistryJsonReloadListener.asMapCodec(t.codec()))/;
    add_import($RL);
});
patch('data/fluid_generators/IFluidGenerator.java', sub {
    s/Type\.CODEC\.dispatch\("type", IFluidGenerator::getType, Type::codec\)/Type.CODEC.dispatch("type", IFluidGenerator::getType, t -> RegistryJsonReloadListener.asMapCodec(t.codec()))/;
    add_import($RL);
});
patch('data/block_growths/growths/builtin/BuiltinBlockGrowth.java', sub {
    s/Codec<BuiltinBlockGrowth> codec = RecordCodecBuilder\.create\(/MapCodec<BuiltinBlockGrowth> codec = RecordCodecBuilder.mapCodec(/;
    add_import('com.mojang.serialization.MapCodec');
});
patch('data/fluid_generators/builtin/BurnMossGenerator.java', sub {
    s/Codec\.unit\(\(\) -> INSTANCE\)/MapCodec.unit(() -> INSTANCE).codec()/;
    add_import('com.mojang.serialization.MapCodec');
});

# --- block pair: the vanilla block state codec does the same job
patch('data/block_growths/BlockPair.java', sub {
    s/    private static final Codec<Block> BLOCK_CODEC = .*?\n    \);\n//s;
    s/BlockStateAccessor\.getCodec\(BLOCK_CODEC, Block::defaultBlockState\)\.stable\(\)/BlockState.CODEC/;
    s/\n    public static class BlockStateAccessor extends BlockState \{.*?\n    \}\n(?=\})//s;
});

# --- weighted lists
for my $f ('data/block_growths/growths/ConfigurableBlockGrowth.java', 'data/block_growths/growths/builtin/LightningGrowth.java') {
    patch($f, sub {
        s/import net\.minecraft\.util\.random\.SimpleWeightedRandomList;/import net.minecraft.util.random.WeightedList;/;
        s/import net\.minecraft\.util\.random\.WeightedEntry;/import net.minecraft.util.random.Weighted;/;
        s/SimpleWeightedRandomList\.wrappedCodec\(/WeightedList.codec(/g;
        s/\bSimpleWeightedRandomList\b/WeightedList/g;
        s/WeightedEntry\.Wrapper::getData/Weighted::value/g;
        s/\.getRandomValue\(/.getRandom(/g;
    });
}

# --- reload listeners
for my $pair (['data/block_growths/BlockGrowthHandler.java', 'BlockGrowthHandler', 'block_growths'],
              ['data/fluid_generators/FluidGeneratorsHandler.java', 'FluidGeneratorsHandler', 'fluid_generators']) {
    my ($f, $cls, $dir) = @$pair;
    patch($f, sub {
        s/import net\.mehvahdjukaar\.moonlight\.api\.misc\.RegistryAccessJsonReloadListener;/import $RL;/;
        s/extends RegistryAccessJsonReloadListener/extends RegistryJsonReloadListener/;
        s/    public static final \Q$cls\E RELOAD_INSTANCE = new \Q$cls\E\(\);\n//;
        s/    public \Q$cls\E\(\) \{\n        super\(GSON, "\Q$dir\E"\);\n    \}/    public $cls(HolderLookup.Provider registries) {\n        super(registries, "$dir");\n    }/;
        s/public void parse\(Map<Identifier, JsonElement> (\w+), RegistryAccess (\w+)\)/public void parse(Map<Identifier, JsonElement> $1, HolderLookup.Provider $2)/;
        add_import('net.minecraft.core.HolderLookup');
    });
}
patch('data/block_growths/BlockGrowthHandler.java', sub {
    s/biome\.getPrecipitationAt\(targetPos\)/biome.getPrecipitationAt(targetPos, level.getSeaLevel())/;
});

# --- tags from the builtin registry
for my $f ('data/block_growths/growths/builtin/LightningGrowth.java', 'data/block_growths/growths/builtin/SandGrowth.java') {
    patch($f, sub {
        s/BuiltInRegistries\.BLOCK\.getTag\(([\w.]+)\)\.get\(\)\.stream\(\)\.forEach\(h -> blocks\.add\(h\.value\(\)\)\);/BuiltInRegistries.BLOCK.getTagOrEmpty($1).forEach(h -> blocks.add(h.value()));/g;
    });
}

# --- misc
patch('data/block_growths/growths/builtin/IceGrowth.java', sub { s/biome\.coldEnoughToSnow\(pos\)/biome.coldEnoughToSnow(pos, level.getSeaLevel())/; });
patch('data/position_tests/PrecipitationTest.java', sub {
    s/coldEnoughToSnow\(pos\.relative\(d\)\)/coldEnoughToSnow(pos.relative(d), level.getSeaLevel())/;
    s/warmEnoughToRain\(pos\.relative\(d\)\)/warmEnoughToRain(pos.relative(d), level.getSeaLevel())/;
});
patch('data/block_growths/growths/builtin/FireSootGrowth.java', sub { s/GameRules\.FIRE_TICK/GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER/; });
