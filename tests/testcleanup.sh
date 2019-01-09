#!/bin/bash

docker kill testsuiteproject_synbiohub_1
docker kill testsuiteproject_explorer_1
docker kill testsuiteproject_autoheal_1
docker kill testsuiteproject_virtuoso_1

docker rm testsuiteproject_synbiohub_1
docker rm testsuiteproject_explorer_1
docker rm testsuiteproject_autoheal_1
docker rm testsuiteproject_virtuoso_1


docker volume rm testsuiteproject_esdata
docker volume rm testsuiteproject_explorer
docker volume rm testsuiteproject_sbh
docker volume rm testsuiteproject_virtuoso-db
