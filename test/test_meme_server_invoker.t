#!/usr/bin/perl

# Script for testing back-end of meme service

use strict;
use warnings;


my $deployment_dir = $ENV{'KB_TOP'}."/lib/jars/meme/";
my $command_line = "java -jar ".$deployment_dir."meme.jar";
my $test_command = $command_line." --test";
print $test_command."\n\n";
system ($test_command);

exit(0);
