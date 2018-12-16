#!/bin/bash

docker kill testproject_synbiohub_1
docker kill testproject_explorer_1
docker kill testproject_autoheal_1
docker kill testproject_virtuoso_1

docker rm --volumes testproject_synbiohub_1
docker rm --volumes testproject_explorer_1
docker rm --volumes testproject_autoheal_1
docker rm --volumes testproject_virtuoso_1
