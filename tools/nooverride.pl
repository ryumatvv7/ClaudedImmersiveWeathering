#!/usr/bin/perl
# prints the signature following each "does not override" error from errs.txt
use strict; use warnings;
my $root = shift // 'src/main/java/com/ordana/immersive_weathering';
open(my $e, '<', 'errs.txt') or die;
while (<$e>) {
    next unless /^(\S+?\.java):(\d+): method does not override/;
    my ($f, $l) = ($1, $2);
    $f =~ s{\\}{/}g;
    open(my $fh, '<', "$root/$f") or next;
    my @a = <$fh>; close $fh;
    my $x = $a[$l] // '';
    $x .= $a[$l + 1] if $x !~ /\(/;
    $x =~ s/\s+/ /g;
    print "$f:$l:$x\n";
}
