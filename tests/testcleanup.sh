#!/bin/bash

source ./testutil.sh

# Tear down any leftover testsuiteproject stack from a previous run. Supplying
# the selected files is required by modern Compose; --remove-orphans also
# removes services left by a different store/search pairing under this project.
message "Removing any existing testsuiteproject containers and volumes"
COMPOSE_FILES=$(backend_compose_files) || exit 1
docker compose $COMPOSE_FILES -p testsuiteproject down --volumes --remove-orphans 2>/dev/null || true

for volume in \
    testsuiteproject_esdata \
    testsuiteproject_explorer \
    testsuiteproject_sbh \
    testsuiteproject_virtuoso-db \
    testsuiteproject_pgdata
do
    docker volume rm "$volume" 2>/dev/null || true
done
