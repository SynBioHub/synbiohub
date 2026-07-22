#!/bin/bash

source ./testutil.sh

# Tear down any leftover testsuiteproject stack from a previous run, whichever
# triplestore it used. `down` finds the containers by project label, so it works
# without knowing the compose file; the explicit volume removals below are a
# best-effort fallback covering both backends' named volumes.
message "Removing any existing testsuiteproject containers and volumes"
docker compose -p testsuiteproject down --volumes --remove-orphans 2>/dev/null || true

for volume in \
    testsuiteproject_esdata \
    testsuiteproject_explorer \
    testsuiteproject_sbh \
    testsuiteproject_virtuoso-db \
    testsuiteproject_pgdata
do
    docker volume rm "$volume" 2>/dev/null || true
done
