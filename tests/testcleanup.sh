#!/bin/sh

docker kill synbiohubdocker_synbiohub_1
docker kill synbiohubdocker_explorer_1
docker kill synbiohubdocker_autoheal_1
docker kill synbiohubdocker_virtuoso_1
docker rm $(docker ps -aq)
docker volume rm $(docker volume ls -q)
