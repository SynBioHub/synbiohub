#!/bin/bash

docker kill testsuiteproject_synbiohub_1
docker kill testsuiteproject_explorer_1
docker kill testsuiteproject_autoheal_1
docker kill testsuiteproject_virtuoso_1

docker rm --volumes testsuiteproject_synbiohub_1
docker rm --volumes testsuiteproject_explorer_1
docker rm --volumes testsuiteproject_autoheal_1
docker rm --volumes testsuiteproject_virtuoso_1
