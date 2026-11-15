#!/usr/bin/perl
# Generates reg/VanillaBlockReplacements.java from the old BlocksMixin (upstream), which redirected
# "new Block(...)" calls inside Blocks.<clinit>. Those constructors calls are gone in 26.1.
use strict; use warnings;
undef $/;
open(my $in, '<', 'upstream/common/src/main/java/com/ordana/immersive_weathering/mixins/BlocksMixin.java') or die;
my $src = <$in>; close $in; $src =~ s{}{}g;
my @rows;
while ($src =~ /stringValue=(\w+)".*?\n    private static (\w+) \w+\(([^)]*)\) \{\n\s*return (.*?);\n    \}/sg) {
    my ($name, $params, $expr) = ($1, $3, $4);
    $expr =~ s/\b(settings|properties)\b(?=\))/p/;
    $expr =~ s/\(\) -> (PRISMARINE_BRICKS|END_STONE_BRICKS)\b/() -> Blocks.$1/;
    $expr =~ s/\(\) -> ModItems\.(\w+)\.get\(\)/() -> ModItems.$1.get()/g;
    push @rows, "        R.put(\"$name\", p -> $expr);";
}
my $body = join("\n", @rows);
open(my $out, '>', 'src/main/java/com/ordana/immersive_weathering/reg/VanillaBlockReplacements.java') or die;
print $out <<"JAVA";
package com.ordana.immersive_weathering.reg;

import com.ordana.immersive_weathering.blocks.ModPropaguleBlock;
import com.ordana.immersive_weathering.blocks.cracked.*;
import com.ordana.immersive_weathering.blocks.mossy.*;
import com.ordana.immersive_weathering.blocks.rusty.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Vanilla blocks that get replaced by weathering-aware classes (crackable bricks, mossable stone, rustable iron...).
 * <p>
 * The original mod redirected the constructor calls inside {\@code Blocks.<clinit>}. Since vanilla now
 * registers blocks through factories, a single hook on {\@code Blocks.register} swaps the factory instead
 * (see BlocksMixin). Generated from the original mixin by tools/gen_block_replacements.pl.
 */
public class VanillaBlockReplacements {

    private static final Map<String, Function<BlockBehaviour.Properties, Block>> R = new HashMap<>();

    static {
$body
    }

    public static Function<BlockBehaviour.Properties, Block> get(ResourceKey<Block> id, Function<BlockBehaviour.Properties, Block> original) {
        if (!id.identifier().getNamespace().equals("minecraft")) return original;
        Function<BlockBehaviour.Properties, Block> replacement = R.get(id.identifier().getPath());
        return replacement != null ? replacement : original;
    }
}
JAVA
close $out;
print scalar(@rows), " replacements\n";
