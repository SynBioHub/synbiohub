#!/bin/bash

source ./testutil.sh

message "Starting SynBioHub from Containers"
docker-compose -f ./synbiohub-docker/docker-compose.yml -f ./synbiohub-docker/docker-compose.explorer.yml  -p testsuiteproject up -d
while [[ "$(docker inspect testsuiteproject_synbiohub_1 | jq .[0].State.Health.Status)" != "\"healthy\"" ]]
do
    sleep 5
    message "Waiting for synbiohub container to be healthy."
done

message "Started successfully"

