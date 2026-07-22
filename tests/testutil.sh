function message {
    echo "[synbiohub test] $1"
}

# Triplestore backend the test stack runs against: "virtuoso" (default) or
# "sboldb". Set with the SBH_TRIPLESTORE environment variable, or pass it as the
# first argument to start_containers.sh / start_containers_persist.sh. The only
# difference between the two is which docker-compose file describes the stack, so
# the same SynBioHub image and the same fixtures gate both backends.
SBH_TRIPLESTORE="${SBH_TRIPLESTORE:-virtuoso}"

# Echo the docker-compose -f flags for the selected triplestore. The compose
# files live in the synbiohub-docker checkout that start_containers.sh clones.
function triplestore_compose_files {
    case "$SBH_TRIPLESTORE" in
        virtuoso)
            echo "-f ./synbiohub-docker/docker-compose.yml -f ./synbiohub-docker/docker-compose.explorer.yml"
            ;;
        sboldb)
            echo "-f ./synbiohub-docker/docker-compose.sboldb.yml"
            ;;
        *)
            message "Unknown SBH_TRIPLESTORE '$SBH_TRIPLESTORE' (expected virtuoso or sboldb)." 1>&2
            return 1
            ;;
    esac
}
