#!/usr/bin/perl
# One-off fixes, part 2 of stage 3.
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

patch('data/block_growths/growths/ConfigurableBlockGrowth.java', sub {
    s/e\.getData\(\)/e.value()/g;
    s/e\.getWeight\(\)\.asInt\(\)/e.weight()/g;
});
patch('data/block_growths/growths/builtin/LightningGrowth.java', sub {
    s/list\.getRandom\(level\.getRandom\(\)\)\.get\(\)\.getData\(\)/list.getRandomOrThrow(level.getRandom())/;
});
patch('data/block_growths/growths/builtin/LeavesGrowth.java', sub {
    s/NetworkHandler\.CHANNEL\.sendToAllClientPlayersInRange\(level, pos, 32,\s*new SendCustomParticlesPacket\(SendCustomParticlesPacket\.EventType\.DECAY_LEAVES,\s*pos, Block\.getId\(state\)\)\);/LeafDecayPayload.sendAround(level, pos, state, 32);/s;
    s/import com\.ordana\.immersive_weathering\.network\.NetworkHandler;\n//;
    s/import com\.ordana\.immersive_weathering\.network\.SendCustomParticlesPacket;/import com.ordana.immersive_weathering.network.LeafDecayPayload;/;
});
patch('data/position_tests/HeightTest.java', sub {
    s/int sampledY = this\.height\.sample\(level\.getRandom\(\), new DummyWorldGenerationContext\(level\)\);/if (!(level instanceof ServerLevel serverLevel)) return false;\n        int sampledY = this.height.sample(level.getRandom(),\n                new WorldGenerationContext(serverLevel.getChunkSource().getGenerator(), serverLevel));/;
    add_import('net.minecraft.server.level.ServerLevel');
    add_import('net.minecraft.world.level.levelgen.WorldGenerationContext');
});
patch('data/position_tests/EntityTest.java', sub {
    s/IntProvider\.codec\(0, 100\)/IntProviders.codec(0, 100)/;
    add_import('net.minecraft.util.valueproviders.IntProviders');
});
for my $f ('data/rute_tests/BurnableTest.java', 'data/rute_tests/LogMatchTest.java') {
    patch($f, sub {
        s/Codec\.unit\(\(\) -> INSTANCE\)/MapCodec.unit(() -> INSTANCE).codec()/;
        add_import('com.mojang.serialization.MapCodec');
    });
}
patch('data/rute_tests/BlockPropertyTest.java', sub {
    s/state\.getValues\(\)\.isEmpty\(\)/state.getProperties().isEmpty()/;
    s/Codec<PropPredicate> c1 = RecordCodecBuilder\.create\(/MapCodec<PropPredicate> c1 = RecordCodecBuilder.mapCodec(/;
    s/return DataResult\.success\(c\);/return DataResult.success(RegistryJsonReloadListener.asMapCodec(c));/;
    add_import('com.mojang.serialization.MapCodec');
    add_import('com.ordana.immersive_weathering.util.RegistryJsonReloadListener');
});
patch('reg/ModRuleTests.java', sub {
    s/        return RegHelper\.register\(ImmersiveWeathering\.res\(name\), \(\) -> \(\) -> codec, Registries\.RULE_TEST\);/        MapCodec<P> mapCodec = RegistryJsonReloadListener.asMapCodec(codec);\n        RuleTestType<P> type = Registry.register(BuiltInRegistries.RULE_TEST, ImmersiveWeathering.res(name), () -> mapCodec);\n        return () -> type;/;
    add_import('com.mojang.serialization.MapCodec');
    add_import('com.ordana.immersive_weathering.util.RegistryJsonReloadListener');
    add_import('net.minecraft.core.Registry');
    add_import('net.minecraft.core.registries.BuiltInRegistries');
});
patch('events/ModEvents.java', sub {
    s/SoundEvents\.SHIELD_BREAK,/SoundEvents.SHIELD_BREAK.value(),/;
    s/item instanceof PickaxeItem/stack.is(ItemTags.PICKAXES)/;
    add_import('net.minecraft.tags.ItemTags');
});
patch('mixins/FungusBlockMixin.java', sub {
    s/\bFungusBlock\b(?!Mixin)/NetherFungusBlock/g;
});
patch('mixins/SweetBerryBushMixin.java', sub {
    s/target = "Lnet\/minecraft\/world\/entity\/Entity;hurt\(Lnet\/minecraft\/world\/damagesource\/DamageSource;F\)Z"/target = "Lnet\/minecraft\/world\/entity\/Entity;hurtServer(Lnet\/minecraft\/server\/level\/ServerLevel;Lnet\/minecraft\/world\/damagesource\/DamageSource;F)Z"/;
    s/private boolean removeBlock\(Entity entity, DamageSource source, float amount\)/private boolean removeBlock(Entity entity, ServerLevel level, DamageSource source, float amount)/;
    s/return entity\.hurt\(source, amount\);/return entity.hurtServer(level, source, amount);/;
    add_import('net.minecraft.server.level.ServerLevel');
});
