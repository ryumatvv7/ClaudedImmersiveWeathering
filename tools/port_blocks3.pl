#!/usr/bin/perl
# Third one-off pass: file specific fixes. Run from project root.
use strict; use warnings;
my $root = 'src/main/java/com/ordana/immersive_weathering';

sub patch {
    my ($rel, $code) = @_;
    my $file = "$root/$rel";
    local $/; open(my $fh, '<', $file) or die "$file: $!"; my $s = <$fh>; close $fh;
    my $o = $s;
    local $_ = $s; $code->(); $s = $_;
    if ($s ne $o) { open(my $out, '>', $file) or die; print $out $s; close $out; print "patched $rel\n"; }
    else { print "UNCHANGED $rel\n"; }
}
sub add_import {
    my ($fq) = @_;
    return if /import \Q$fq\E;/;
    s/^(package [\w.]+;\n)/$1\nimport $fq;/m;
}

# --- soil "use" signatures whose bodies were already converted
for my $f (qw(EarthenClayBlock EarthenClayBlockGrassy LoamBlock LoamBlockGrassy SandyDirtBlock SandyDirtBlockGrassy SiltBlock SiltBlockGrassy)) {
    patch("blocks/soil_types/$f.java", sub {
        s/public InteractionResult use\(BlockState (\w+), Level (\w+), BlockPos (\w+), Player (\w+), InteractionHand (\w+), BlockHitResult (\w+)\)/protected InteractionResult useItemOn(ItemStack heldStack, BlockState $1, Level $2, BlockPos $3, Player $4, InteractionHand $5, BlockHitResult $6)/;
        add_import('net.minecraft.world.item.ItemStack');
    });
}

# --- falling blocks need a codec and a dust color
my $dust = "\n    \@Override\n    public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {\n        return state.getMapColor(level, pos).col;\n    }\n";
sub codec { my ($cls) = @_; return "\n    public static final MapCodec<$cls> CODEC = simpleCodec(${cls}::new);\n\n    \@Override\n    protected MapCodec<? extends FallingBlock> codec() {\n        return CODEC;\n    }\n"; }
for my $pair (['blocks/LayerBlock.java', 'LayerBlock'], ['blocks/soil_types/BaseSoilBlockFallable.java', 'BaseSoilBlockFallable'],
              ['blocks/soil_types/PermafrostBlock.java', 'PermafrostBlock'], ['blocks/soil_types/SandyDirtBlock.java', 'SandyDirtBlock']) {
    my ($f, $cls) = @$pair;
    patch($f, sub {
        my $add = codec($cls) . ($cls eq 'SandyDirtBlock' ? '' : $dust);
        s/(public class \Q$cls\E [^{]*\{\n)/$1$add/;
        add_import('com.mojang.serialization.MapCodec');
        add_import('net.minecraft.world.level.BlockGetter');
        add_import('net.minecraft.world.level.block.FallingBlock');
        add_import('net.minecraft.core.BlockPos');
    });
}

# --- vanilla-like bonemeal on soils
for my $f ('blocks/soil_types/BaseSoilBlock.java', 'blocks/soil_types/BaseSoilBlockFallable.java') {
    patch($f, sub {
        s/            if \(blockState2\.isAir\(\)\) \{\n                Holder holder;.*?\n            \}\n        \}\n    \}/            if (blockState2.isAir() && !level.isOutsideBuildHeight(blockPos2)) {\n                if (random.nextInt(8) == 0) {\n                    List<ConfiguredFeature<?, ?>> list = level.getBiome(blockPos2).value().getGenerationSettings().getBoneMealFeatures();\n                    if (!list.isEmpty()) {\n                        Util.getRandom(list, random).place(level, level.getChunkSource().getGenerator(), random, blockPos2);\n                    }\n                } else if (optional.isPresent()) {\n                    optional.get().value().place(level, level.getChunkSource().getGenerator(), random, blockPos2);\n                }\n            }\n        }\n    }/s;
        s/import net\.minecraft\.world\.level\.levelgen\.feature\.configurations\.RandomPatchConfiguration;\n//;
        add_import('net.minecraft.util.Util');
    });
}

patch('blocks/soil_types/RootedGrassBlock.java', sub { s/\bcanBeGrass\(/SpreadingSnowyBlock.canStayAlive(/g; add_import('net.minecraft.world.level.block.SpreadingSnowyBlock'); });
patch('blocks/soil_types/NulchBlock.java', sub { s/level\.scheduleTick\(currentPos, Fluids\.LAVA,/tickAccess.scheduleTick(currentPos, Fluids.LAVA,/; });

patch('blocks/rusty/Rustable.java', sub { s/default void applyChangeOverTime\(/default void changeOverTime(/; });

# --- propagule
patch('blocks/ModPropaguleBlock.java', sub {
    s/super\(properties\);/super(TreeGrower.MANGROVE, properties);/;
    s/import javax\.swing\.text\.html\.BlockView;\n//;
    s/level\.scheduleTick\(pos, Fluids\.WATER,/tickAccess.scheduleTick(pos, Fluids.WATER,/;
    s/level\.removeBlock\(pos, true\);/return Blocks.AIR.defaultBlockState();/;
    add_import('net.minecraft.world.level.block.grower.TreeGrower');
});

# --- icicle
patch('blocks/IcicleBlock.java', sub {
    s/if \(!level\.isClientSide\(\) && projectile\.mayInteract\(level, blockpos\)/if (level instanceof ServerLevel serverLevel && projectile.mayInteract(serverLevel, blockpos)/;
    s/level\.scheduleTick\(pos, Fluids\.WATER,/tickAccess.scheduleTick(pos, Fluids.WATER,/;
    s/level\.getBlockTicks\(\)\.hasScheduledTick\(pos, this\)\) \{\n                return state;/tickAccess.getBlockTicks().hasScheduledTick(pos, this)) {\n                return state;/;
    s/level\.scheduleTick\(pos, this, 2\);/tickAccess.scheduleTick(pos, this, 2);/;
    s/state\.getOffset\(level, pos\)/state.getOffset(pos)/;
});

# --- fulgurite
patch('blocks/FulguriteBlock.java', sub {
    s/    \@Override\n    public void onRemove\(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved\) \{.*?\n    \}\n/    \@Override\n    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {\n        if (state.getValue(POWERED)) {\n            this.updateNeighbors(state, level, pos);\n        }\n        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);\n    }\n/s;
    # channeling is a data driven enchantment now: fulgurite is in the #minecraft:lightning_rods block tag instead
    s/    \@Override\n    public void onProjectileHit\(Level level, BlockState state, BlockHitResult hit, Projectile projectile\) \{.*?\n    \}\n\n    \}\n/    \/\/Channeling tridents: handled by the vanilla data driven enchantment, see tag minecraft:lightning_rods\n/s;
    add_import('net.minecraft.server.level.ServerLevel');
});

# --- helper imports
patch('util/WeatheringHelper.java', sub {
    add_import('net.minecraft.core.registries.Registries');
    add_import('net.minecraft.resources.ResourceKey');
    add_import('net.minecraft.world.entity.LivingEntity');
    add_import('net.minecraft.world.item.enchantment.Enchantment');
    add_import('net.minecraft.world.item.enchantment.EnchantmentHelper');
});
