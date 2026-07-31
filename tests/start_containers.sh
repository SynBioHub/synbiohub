#!/bin/bash

# Optional first argument selects the triplestore backend (virtuoso | sboldb);
# it overrides SBH_TRIPLESTORE.
if [[ -n "$1" ]]; then
    export SBH_TRIPLESTORE="$1"
fi

source ./testutil.sh

if [[ "$SBH_DOCKER_DIR" == "./synbiohub-docker" ]]; then
    message "pulling synbiohub/synbiohub-docker"
    if cd synbiohub-docker; then
        git checkout snapshot;
        git pull;
        cd ..;
    else
        # Clone the compose files when no explicit checkout was supplied.
        git clone --single-branch --branch snapshot https://github.com/synbiohub/synbiohub-docker
    fi
else
    message "Using SynBioHub Docker checkout: $SBH_DOCKER_DIR"
fi

COMPOSE_FILES=$(backend_compose_files) || exit 1
for compose_file in $(echo "$COMPOSE_FILES" | sed 's/-f //g'); do
    if [[ ! -f "$compose_file" ]]; then
        message "Required compose file not found: $compose_file"
        message "Update the synbiohub-docker checkout, or set SBH_DOCKER_DIR to the comparison branch checkout."
        exit 1
    fi
done

message "Cleaning old test containers if they exist"
bash ./testcleanup.sh


bash ./start_containers_persist.sh "$SBH_TRIPLESTORE"
