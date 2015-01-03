#!/usr/bin/perl

# Script for testing back-end of meme service

use strict;
use warnings;

use Bio::KBase::AuthToken;
use Bio::KBase::AuthUser;
use Bio::KBase::userandjobstate::Client;

my $user = "aktest";
my $pw = "1475rokegi";

my $auth_user = Bio::KBase::AuthUser->new();
my $token = Bio::KBase::AuthToken->new( user_id => $user, password => $pw);
$auth_user->get( token => $token->token );

if ($token->error_message){
	print $token->error_message."\n\n";
	exit(1);
};

my $auth_token = $token->token;

my $job_client = Bio::KBase::userandjobstate::Client->new("https://kbase.us/services/userandjobstate", "user_id", $user, "password", $pw);

my $deployment_dir = $ENV{'KB_TOP'}."/lib/jars/meme/";

my $command_line = "java -jar ".$deployment_dir."meme.jar";

my $ws = "AKtest";
my $sequence_set_id = "\"AKtest/mod_desulfovibrio\"";
my $meme_pspm_collection_id = "\"AKtest/kb|memepspmcollection.57\"";
my $meme_pspm_id = "\"kb|memepspm.115\"";
my $meme_run_result_id = "\"AKtest/kb|memerunresult.187\"";

my $test_command = "";

my @time = (localtime(time + 900))[0..5];
my $tdiff = (localtime)[2] - (gmtime)[2];
if ($tdiff > 12) {$tdiff-= 24;};
print gmtime."\n".localtime."\n\n";
my $timestamp = sprintf ("%d-%02d-%02dT%02d:%02d:%02d%+03d", $time[5] + 1900, $time[4] +1, $time[3], $time[2], $time[1], $time[0], $tdiff);
$timestamp = $timestamp."00";

my $progress = {
    "ptype"=>"task",
    "max"=>5
};


#1 help
$test_command = $command_line." --test";
print $test_command."\n\n";
system ($test_command);

#2 find_motifs_with_meme_job_from_ws
#my $job = $job_client->create_and_start_job($auth_token, "Test job started", "MEME server back-end test: find_motifs_with_meme_job_from_ws", $progress, $timestamp);
#$test_command = $command_line." --method find_motifs_with_meme_job_from_ws --job $job --ws $ws --query $sequence_set_id --mod oops --nmotifs 2 --minw 14 --maxw 28 --pal 1 --token \"$auth_token\"";
#print $test_command."\n\n";
#system ($test_command);

#3 (function removed from back-end) get_pspm_collection_from_meme_result_from_ws
#$job = $job_client->create_and_start_job($auth_token, "Test job started", "MEME server back-end test: get_pspm_collection_from_meme_result_from_ws", $progress, $timestamp);
#$test_command = $command_line." --method get_pspm_collection_from_meme_result_job_from_ws --job $job --ws $ws --query $meme_run_result_id --token \"$auth_token\"";
#print $test_command."\n\n";
#system ($test_command);

#4 compare_motifs_with_tomtom_by_collection_from_ws
#$job = $job_client->create_and_start_job($auth_token, "Test job started", "MEME server back-end test: compare_motifs_with_tomtom_by_collection_from_ws", $progress, $timestamp);
#$test_command = $command_line." --method compare_motifs_with_tomtom_job_by_collection_from_ws --job $job --ws $ws --query $meme_pspm_collection_id --target $meme_pspm_collection_id --thresh 0.000001 --evalue 1 --dist pearson --min_overlap 12 --internal 1 --token \"$auth_token\"";
#print $test_command."\n\n";
#system ($test_command);

#(function removed) 5 compare_motifs_with_tomtom_from_ws
#$job = $job_client->create_job();
#$test_command = $command_line." --method compare_motifs_with_tomtom_job_from_ws --job $job --ws $ws --query $meme_pspm_id --target $meme_pspm_collection_id --thresh 0.000001 --evalue 1 --dist pearson --min_overlap 12 --internal 1 --token \"$auth_token\"";
#print $test_command."\n\n";
#system ($test_command);

#(function removed) 6 find_sites_with_mast_from_ws
#$job = $job_client->create_job();
#$test_command = $command_line." --method find_sites_with_mast_job_from_ws --job $job --ws $ws --query $meme_pspm_id --target $sequence_set_id --thresh 0.001 --token \"$auth_token\"";
#print $test_command."\n\n";
#system ($test_command);

#7 find_sites_with_mast_by_collection_from_ws
#$job = $job_client->create_and_start_job($auth_token, "Test job started", "MEME server back-end test: find_sites_with_mast_by_collection_from_ws", $progress, $timestamp);
#$test_command = $command_line." --method find_sites_with_mast_job_by_collection_from_ws --job $job --ws $ws --query $meme_pspm_collection_id --target $sequence_set_id --thresh 0.001 --token \"$auth_token\"";
#print $test_command."\n\n";
#system ($test_command);

exit(0);
