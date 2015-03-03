Module MEME version 1.0 (created Aug 2013)

This module provides a set of functions for work with regulatory motifs. These functions integrate capabilities of three predictive tools into KBase:
- MEME is a tool for discovering motifs in a group of related DNA or protein sequences
- TOMTOM is a tool for comparison of an input DNA motif to the elements of a database of known motifs
- MAST is a tool for searching biological sequence databases for occurencies of known motifs

To install MEME service, run commands:
make
make deploy

Deployment of MEME service requires typecomp (dev-prototype branch) and java type generator (dev branch).
Default installation of MEME server will use AWE service for three functions that return job object ID: find_motifs_with_meme_job_from_ws,
compare_motifs_with_tomtom_job_from_ws, find_sites_with_mast_job_from_ws. All other functions will run on the host where MEME server runs.

To prevent MEME from running  AWE client, change DEPLOY_AWE = true to DEPLOY_AWE = false in us.kbase.meme.MemeServerConfig source file.
URL of AWE client can be changed in Makefile. Run "make", "make deploy" and then restart the service to apply changes.

To install MEME service back-end for AWE client, clone meme.git repository on the host running AWE client and run command 'make deploy-worker'. This command will work with master branches of typecomp and java type generator as well.

Requirements:
MEME service requires installation of MEME Suite 4.8.1 with patches 1-5 on servers running MEME server and AWE client.
MEME installation script can be found in bootstrap.git repo: ./kb_meme/build.meme

Dependencies for MEME Suite:
MEME Suite requires 'csh' package (apt-get install csh)


