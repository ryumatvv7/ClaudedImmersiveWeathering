#!/usr/bin/perl
# One-off mechanical signature migration for block classes (1.20.1 -> 26.1.2).
# Usage: perl -0pi tools/port_blocks.pl <files>
use strict; use warnings;
undef $/;
while (my $file = shift @ARGV) {
    open(my $fh, '<', $file) or die "$file: $!"; my $s = <$fh>; close $fh;
    my $o = $s;

    # --- use -> useItemOn (class overrides only; interface default methods keep their own "use" helper)
    $s =~ s/public InteractionResult use\(BlockState (\w+), Level (\w+), BlockPos (\w+), Player (\w+), InteractionHand (\w+), BlockHitResult (\w+)\)(\s*\{)(?=(?:(?!\n    \}).)*?(?<![\w.])super\.use\(|(?:(?!\n    \}).)*?\w+\.super\.use\(|)/__USE__($1,$2,$3,$4,$5,$6)$7/sg;
    # only convert when inside a class (file declares "class"), interfaces keep helper
    if ($s =~ /\bpublic\s+(?:abstract\s+)?class\b/) {
        $s =~ s/\@Override(\s+)__USE__\((\w+),(\w+),(\w+),(\w+),(\w+),(\w+)\)/\@Override$1protected InteractionResult useItemOn(ItemStack heldStack, BlockState $2, Level $3, BlockPos $4, Player $5, InteractionHand $6, BlockHitResult $7)/g;
        $s =~ s/(?<![\w.])super\.use\((\w+), (\w+), (\w+), (\w+), (\w+), (\w+)\)/super.useItemOn(heldStack, $1, $2, $3, $4, $5, $6)/g;
    }
    $s =~ s/__USE__\((\w+),(\w+),(\w+),(\w+),(\w+),(\w+)\)/public InteractionResult use(BlockState $1, Level $2, BlockPos $3, Player $4, InteractionHand $5, BlockHitResult $6)/g;

    # --- bonemeal
    $s =~ s/isValidBonemealTarget\((\w+) (\w+), BlockPos (\w+), BlockState (\w+), boolean \w+\)/isValidBonemealTarget($1 $2, BlockPos $3, BlockState $4)/g;

    # --- neighborChanged: 5th param is an Orientation now
    $s =~ s/neighborChanged\(BlockState (\w+), Level (\w+), BlockPos (\w+), Block (\w+), BlockPos (\w+), boolean (\w+)\)/neighborChanged(BlockState $1, Level $2, BlockPos $3, Block $4, \@Nullable Orientation $5, boolean $6)/g;

    # --- entityInside
    $s =~ s/entityInside\(BlockState (\w+), Level (\w+), BlockPos (\w+), Entity (\w+)\)/entityInside(BlockState $1, Level $2, BlockPos $3, Entity $4, InsideBlockEffectApplier effectApplier, boolean isPrecise)/g;
    $s =~ s/super\.entityInside\((\w+), (\w+), (\w+), (\w+)\)/super.entityInside($1, $2, $3, $4, effectApplier, isPrecise)/g;

    # --- updateShape
    $s =~ s/updateShape\(BlockState (\w+), Direction (\w+), BlockState (\w+), LevelAccessor (\w+), BlockPos (\w+), BlockPos (\w+)\)/updateShape(BlockState $1, LevelReader $4, ScheduledTickAccess tickAccess, BlockPos $5, Direction $2, BlockPos $6, BlockState $3, RandomSource shapeRandom)/g;
    $s =~ s/super\.updateShape\((\w+), (\w+), (\w+), (\w+), (\w+), (\w+)\)/super.updateShape($1, $4, tickAccess, $5, $2, $6, $3, shapeRandom)/g;

    # --- misc signatures
    $s =~ s/getCloneItemStack\(BlockGetter (\w+), BlockPos (\w+), BlockState (\w+)\)/getCloneItemStack(LevelReader $1, BlockPos $2, BlockState $3, boolean includeData)/g;
    $s =~ s/super\.getCloneItemStack\((\w+), (\w+), (\w+)\)/super.getCloneItemStack($1, $2, $3, includeData)/g;
    $s =~ s/isPathfindable\(BlockState (\w+), BlockGetter \w+, BlockPos \w+, PathComputationType (\w+)\)/isPathfindable(BlockState $1, PathComputationType $2)/g;
    $s =~ s/super\.isPathfindable\((\w+), \w+, \w+, (\w+)\)/super.isPathfindable($1, $2)/g;
    $s =~ s/propagatesSkylightDown\(BlockState (\w+), BlockGetter \w+, BlockPos \w+\)/propagatesSkylightDown(BlockState $1)/g;
    $s =~ s/\bgetLightBlock\(BlockState (\w+), BlockGetter \w+, BlockPos \w+\)/getLightDampening(BlockState $1)/g;
    $s =~ s/(\w+)\.getLightBlock\(\w+, \w+\)/$1.getLightDampening()/g;
    $s =~ s/(\w+)\.dimensionType\(\)\.ultraWarm\(\)/$1.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, pos)/g;
    $s =~ s/interactWithProjectile\(/lightableInteractWithEntity(/g;

    # --- moonlight client particle helper -> vanilla common helper
    $s =~ s/ParticleUtil\.spawnParticlesOnBlockFaces\((\w+), (\w+), ([\w.()]+), (UniformInt\.of\(\d+, \d+\)), [-\d.f]+, [-\d.f]+, (?:true|false)\)/ParticleUtils.spawnParticlesOnBlockFaces($1, $2, $3, $4)/g;
    $s =~ s/import net\.mehvahdjukaar\.moonlight\.api\.client\.util\.ParticleUtil;\n//g;

    # --- imports for whatever got introduced
    my %need = (
        'Orientation'              => 'net.minecraft.world.level.redstone.Orientation',
        'InsideBlockEffectApplier' => 'net.minecraft.world.entity.InsideBlockEffectApplier',
        'ScheduledTickAccess'      => 'net.minecraft.world.level.ScheduledTickAccess',
        'LevelReader'              => 'net.minecraft.world.level.LevelReader',
        'RandomSource'             => 'net.minecraft.util.RandomSource',
        'ItemStack'                => 'net.minecraft.world.item.ItemStack',
        'EnvironmentAttributes'    => 'net.minecraft.world.attribute.EnvironmentAttributes',
        'ParticleUtils'            => 'net.minecraft.util.ParticleUtils',
        'Nullable'                 => 'org.jetbrains.annotations.Nullable',
    );
    for my $cls (sort keys %need) {
        my $fq = $need{$cls};
        next unless $s =~ /\b\Q$cls\E\b/;
        next if $s =~ /import [\w.]*\.\Q$cls\E;/;
        my ($pkg) = $fq =~ /^(.*)\.\w+$/;
        next if $s =~ /import \Q$pkg\E\.\*;/;
        $s =~ s/(\npackage [\w.]+;\n)/$1\nimport $fq;/;
    }

    if ($s ne $o) { open(my $out, '>', $file) or die; print $out $s; close $out; print "patched $file\n"; }
}
