function message {
    echo "[synbiohub test] $1"
}

# Triplestore backend the test stack runs against: "virtuoso" (default) or
# "sboldb". SBH_SEARCH_BACKEND independently selects "sbol-explorer",
# "sbol-db", or "none". The historical full suite stays store-only by default;
# the two named search-backend matrix rows opt in and run the focused
# indexed-HTML contract in place of the ordinary search bundle.
SBH_TRIPLESTORE="${SBH_TRIPLESTORE:-virtuoso}"
SBH_SEARCH_BACKEND="${SBH_SEARCH_BACKEND:-none}"
SBH_DOCKER_DIR="${SBH_DOCKER_DIR:-./synbiohub-docker}"

# Echo the Docker Compose files for the independently selected store/search
# roles. The overlays set Explorer configuration at container startup and turn
# fallback off, so a failed search backend cannot be hidden by the store.
function backend_compose_files {
    case "$SBH_TRIPLESTORE:$SBH_SEARCH_BACKEND" in
        virtuoso:none)
            echo "-f $SBH_DOCKER_DIR/docker-compose.yml"
            ;;
        virtuoso:sbol-explorer)
            echo "-f $SBH_DOCKER_DIR/docker-compose.yml -f $SBH_DOCKER_DIR/docker-compose.explorer.yml"
            ;;
        sboldb:none)
            echo "-f $SBH_DOCKER_DIR/docker-compose.sboldb.yml"
            ;;
        sboldb:sbol-explorer)
            echo "-f $SBH_DOCKER_DIR/docker-compose.sboldb.yml -f $SBH_DOCKER_DIR/docker-compose.sboldb-store-only.yml -f $SBH_DOCKER_DIR/docker-compose.explorer.yml"
            ;;
        sboldb:sbol-db)
            echo "-f $SBH_DOCKER_DIR/docker-compose.sboldb.yml -f $SBH_DOCKER_DIR/docker-compose.sboldb-search.yml"
            ;;
        *)
            message "Unsupported backend pair store='$SBH_TRIPLESTORE' search='$SBH_SEARCH_BACKEND'." 1>&2
            message "Expected virtuoso:(none|sbol-explorer) or sboldb:(none|sbol-explorer|sbol-db)." 1>&2
            return 1
            ;;
    esac
}
