#!/usr/bin/perl
# mode "add": puts "@Override /*chk*/" before unannotated instance methods in the given files.
# mode "strip": removes the ones javac rejected (read from errs.txt) and drops the /*chk*/ markers.
use strict; use warnings;
my $mode = shift;
if ($mode eq 'add') {
    for my $file (@ARGV) {
        open(my $fh, '<', $file) or next; my @a = <$fh>; close $fh;
        next if grep { /^public interface / } @a;
        my $changed = 0;
        for my $i (0 .. $#a) {
            my $l = $a[$i];
            next unless $l =~ /^    (public|protected) (?!static|class|record|enum|interface|abstract)([\w<>\[\]?, .]+) (\w+)\(/;
            my $prev = '';
            for (my $j = $i - 1; $j >= 0 && $j >= $i - 3; $j--) { $prev .= $a[$j]; last if $a[$j] =~ /^\s*$/ or $a[$j] =~ /[;}]\s*$/; }
            next if $prev =~ /\@Override/;
            $a[$i] = "    \@Override /*chk*/\n" . $l; $changed = 1;
        }
        if ($changed) { open(my $out, '>', $file) or die; print $out @a; close $out; }
    }
} else {
    my $root = 'src/main/java/com/ordana/immersive_weathering';
    my %bad;
    open(my $e, '<', 'errs.txt') or die;
    while (<$e>) { if (/^(\S+?\.java):(\d+): method does not override/) { my ($f, $l) = ($1, $2); $f =~ s{\\}{/}g; $bad{"$root/$f"}{$l} = 1; } }
    close $e;
    for my $file (@ARGV) {
        open(my $fh, '<', $file) or next; my @a = <$fh>; close $fh;
        my @o; my $n = 0; my $dead = 0;
        for my $l (@a) {
            $n++;
            if ($l =~ m{^\s*\@Override /\*chk\*/\s*$}) {
                if ($bad{$file}{$n}) { $dead++; push @o, "    //PORT-CHECK: no longer overrides anything in 26.1.2\n"; }
                else { push @o, "    \@Override\n"; }
            } else { push @o, $l; }
        }
        open(my $out, '>', $file) or die; print $out @o; close $out;
        print "$file: $dead dead\n" if $dead;
    }
}
