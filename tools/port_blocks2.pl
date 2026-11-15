#!/usr/bin/perl
# Second one-off migration pass (manual-pattern fixes). Usage: perl tools/port_blocks2.pl <files>
use strict; use warnings;
undef $/;
while (my $file = shift @ARGV) {
    open(my $fh, '<', $file) or die "$file: $!"; my $s = <$fh>; close $fh;
    my $o = $s;

    # fallOn: distance is a double now
    $s =~ s/public void fallOn\(Level (\w+), BlockState (\w+), BlockPos (\w+), Entity (\w+), float (\w+)\)/public void fallOn(Level $1, BlockState $2, BlockPos $3, Entity $4, double $5)/g;

    # game rules live on the server level
    $s =~ s/(?<![\w.)])(\w+)\.getGameRules\(\)\.get\(GameRules\./((ServerLevel) $1).getGameRules().get(GameRules./g;

    # biome temperature helpers take the sea level
    $s =~ s/(\w+)\.getBiome\((\w+)\)\.value\(\)\.coldEnoughToSnow\((\w+)\)/$1.getBiome($2).value().coldEnoughToSnow($3, $1.getSeaLevel())/g;

    # enchantments are data driven: look the holder up
    $s =~ s/EnchantmentHelper\.getEnchantmentLevel\(Enchantments\.(\w+), \(LivingEntity\) (\w+)\)/WeatheringHelper.getEnchantmentLevel(Enchantments.$1, (LivingEntity) $2)/g;
    $s =~ s/Enchantments\.FALL_PROTECTION/Enchantments.FEATHER_FALLING/g;
    $s =~ s/!EnchantmentHelper\.hasFrostWalker\(\(LivingEntity\) (\w+)\)/!$1.isSteppingCarefully()/g;

    # multiface blocks no longer declare getSpreader
    $s =~ s/\@Override(\s+public MultifaceSpreader getSpreader\(\))/$1/g if $s !~ /extends MultifaceSpreadeableBlock/;

    # light helpers
    $s =~ s/LightEngine\.getLightBlockInto\(\w+, (\w+), \w+, (\w+), \w+, Direction\.UP, (\w+)\.getLightDampening\(\)\)/LightEngine.getLightBlockInto($1, $2, Direction.UP, $3.getLightDampening())/g;
    $s =~ s/\w+\.getMaxLightLevel\(\)/15/g;

    # registry access
    $s =~ s/\.registryOrThrow\((\w+\.\w+)\)\.getHolder\(/.lookupOrThrow($1).get(/g;
    $s =~ s/\.registryOrThrow\(/.lookupOrThrow(/g;

    # liquid container
    $s =~ s/public boolean canPlaceLiquid\(BlockGetter (\w+), BlockPos (\w+), BlockState (\w+), Fluid (\w+)\)/public boolean canPlaceLiquid(\@Nullable LivingEntity user, BlockGetter $1, BlockPos $2, BlockState $3, Fluid $4)/g;

    # constructor argument order of vanilla blocks
    $s =~ s/super\(properties, (BlockSetType\.\w+|type)\);/super($1, properties);/g;

    # imports
    my %need = (
        'ServerLevel'      => 'net.minecraft.server.level.ServerLevel',
        'WeatheringHelper' => 'com.ordana.immersive_weathering.util.WeatheringHelper',
        'LivingEntity'     => 'net.minecraft.world.entity.LivingEntity',
        'Nullable'         => 'org.jetbrains.annotations.Nullable',
        'GameRules'        => 'net.minecraft.world.level.gamerules.GameRules',
    );
    for my $cls (sort keys %need) {
        my $fq = $need{$cls};
        next unless $s =~ /\b\Q$cls\E\b/;
        next if $s =~ /import [\w.]*\.\Q$cls\E;/;
        next if $s =~ /(class|interface) \Q$cls\E\b/;
        my ($pkg) = $fq =~ /^(.*)\.\w+$/;
        next if $s =~ /import \Q$pkg\E\.\*;/;
        next if $s =~ /^package \Q$pkg\E;/m;
        $s =~ s/^(package [\w.]+;\n)/$1\nimport $fq;/m;
    }

    if ($s ne $o) { open(my $out, '>', $file) or die; print $out $s; close $out; print "patched $file\n"; }
}
