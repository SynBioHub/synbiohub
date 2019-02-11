#!/bin/bash

source ./testutil.sh

message "Cleaning old test containers if they exist"

bash ./testcleanup.sh


message "pulling synbiohub/synbiohub-docker"
if cd synbiohub-docker; then
    git checkout snapshot;
    git pull;
    cd ..;
else
    # clone the synbiohub docker compose file in order to run docker containers
    git clone --single-branch --branch snapshot https://github.com/synbiohub/synbiohub-docker
fi


message "Starting SynBioHub from Containers"
docker-compose -f ./synbiohub-docker/docker-compose.yml -p testsuiteproject up -d
while [[ "$(docker inspect testsuiteproject_synbiohub_1 | jq .[0].State.Health.Status)" != "\"healthy\"" ]]
do
    sleep 5
    message "Waiting for synbiohub container to be healthy."
done

message "Started successfully"

