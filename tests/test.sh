#!/bin/sh

if cd synbiohub-docker; then
    git pull;
    cd ..;
else
    # clone the synbiohub docker compose file in order to run docker containers
    git clone https://github.com/synbiohub/synbiohub-docker;
fi


docker-compose -f ./synbiohub-docker/docker-compose.yml up
