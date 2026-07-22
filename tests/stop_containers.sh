#!/bin/bash

source ./testutil.sh

COMPOSE_FILES=$(triplestore_compose_files) || exit 1

# Stop (do not remove) the containers, leaving volumes intact so the persistence
# phase can restart the same stack with its data.
message "Stopping containers (triplestore: $SBH_TRIPLESTORE)"
docker compose $COMPOSE_FILES -p testsuiteproject stop
