#!/bin/bash

source ./testutil.sh

# stop the containers
message "Stopping containers"
docker stop testsuiteproject_synbiohub_1
docker stop testsuiteproject_explorer_1
docker stop testsuiteproject_autoheal_1
docker stop testsuiteproject_virtuoso_1



