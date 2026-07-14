#!/bin/bash

# Optional first argument selects the triplestore backend (virtuoso | sboldb);
# it overrides SBH_TRIPLESTORE. testutil.sh reads SBH_TRIPLESTORE, so export it
# before sourcing.
if [[ -n "$1" ]]; then
    export SBH_TRIPLESTORE="$1"
fi

source ./testutil.sh

COMPOSE_FILES=$(triplestore_compose_files) || exit 1

message "Starting SynBioHub from Containers (triplestore: $SBH_TRIPLESTORE)"
docker compose $COMPOSE_FILES -p testsuiteproject --compatibility up -d
while [[ "$(docker inspect testsuiteproject_synbiohub_1 | jq .[0].State.Health.Status)" != "\"healthy\"" ]]
do
    sleep 5
    message "Waiting for synbiohub container to be healthy."
done

message "Started successfully"
