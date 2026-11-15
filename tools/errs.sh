#!/usr/bin/env bash
# compile and print compact error list: tools/errs.sh [task] [filter-regex]
cd "$(dirname "$0")/.." || exit 1
./gradlew ${1:-compileJava} --console=plain > build_log.txt 2>&1
perl -ne '
  if (/^(.*?\.java):(\d+): error: (.*)$/) { $f=$1; $l=$2; $m=$3; $f =~ s{.*immersive_weathering.}{}; $cur="$f:$l: $m"; $seen{$cur}++ or push @o,$cur; $last=$cur; $n=0; }
  elsif (defined $last && /^\s+(symbol|location):\s+(.*)$/) { $x{$last} .= " | $2" unless $done{$last.$1}++; }
  elsif (defined $last && $n++==0 && !/^\s+(symbol|location)/) { s/^\s+//; chomp; $code{$last} = $_ unless $code{$last}; }
  END { print scalar(@o)," errors\n"; for (@o) { print "$_", ($x{$_}//""), "\n    > ", ($code{$_}//""), "\n"; } }
' build_log.txt > errs.txt
head -1 errs.txt; grep -E "BUILD|FAILURE|What went wrong" -A3 build_log.txt | head -8
