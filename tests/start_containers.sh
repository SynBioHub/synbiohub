#!/bin/bash

# Optional first argument selects the triplestore backend (virtuoso | sboldb);
# it overrides SBH_TRIPLESTORE.
if [[ -n "$1" ]]; then
    export SBH_TRIPLESTORE="$1"
fi

source ./testutil.sh

message "Cleaning old test containers if they exist"

bash ./testcleanup.sh


message "pulling synbiohub/synbiohub-docker"
if cd synbiohub-docker; then
    git checkout snapshot;
    git pull;
    cd ..;
else
    # clone the synbiohub docker compose file in order to run docker containers
    git clone --single-branch --branch snapshot https://github.com/synbiohub/synbiohub-docker
fi

if [[ "$SBH_TRIPLESTORE" == "sboldb" && ! -f ./synbiohub-docker/docker-compose.sboldb.yml ]]; then
    message "docker-compose.sboldb.yml not found in the synbiohub-docker checkout."
    message "It lives in the synbiohub-docker repo; update that checkout to a revision that has it."
    exit 1
fi


bash ./start_containers_persist.sh "$SBH_TRIPLESTORE"
