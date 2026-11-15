#!/usr/bin/perl
# Lists instance methods in classes that have no @Override on the previous line,
# to spot overrides that silently died after signature changes.
use strict; use warnings;
for my $file (@ARGV) {
    open(my $fh, '<', $file) or next; my @a = <$fh>; close $fh;
    my $isInterface = grep { /^public interface / } @a;
    for my $i (0 .. $#a) {
        my $l = $a[$i];
        next unless $l =~ /^    (public|protected) (?!static|class|record|enum|interface|abstract)([\w<>\[\]?, .]+) (\w+)\(/;
        my $name = $3;
        next if $l =~ /^\s*(public|protected) \w+\(/;    # constructor
        my $prev = '';
        for (my $j = $i - 1; $j >= 0 && $j >= $i - 3; $j--) { $prev .= $a[$j]; last if $a[$j] =~ /^\s*$/ or $a[$j] =~ /[;}]\s*$/; }
        next if $prev =~ /\@Override/;
        (my $short = $file) =~ s{.*/immersive_weathering/}{};
        my $sig = $l; $sig =~ s/^\s+//; $sig =~ s/\s*\{\s*$//; chomp $sig;
        print "$short: $sig\n";
    }
}
