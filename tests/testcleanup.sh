#!/bin/bash

# TODO: change from synbiohubdocker to testproject

docker kill synbiohubdocker_synbiohub_1
docker kill synbiohubdocker_explorer_1
docker kill synbiohubdocker_autoheal_1
docker kill synbiohubdocker_virtuoso_1

docker rm --volumes synbiohubdocker_synbiohub_1
docker rm --volumes synbiohubdocker_explorer_1
docker rm --volumes synbiohubdocker_autoheal_1
docker rm --volumes synbiohubdocker_virtuoso_1
