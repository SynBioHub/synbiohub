#!/bin/bash
#
# Run SynBioHub's Python test suite against sbol-db (instead of Virtuoso).
#
# Brings up the sbol-db-backed stack (tests/sboldb/docker-compose.yml),
# waits for SynBioHub to report healthy, then runs the same test_suite.py
# the Virtuoso path uses. Containers are left running afterwards for
# inspection unless --down is given.
#
# Flags:
#   --down          tear the stack down (and remove volumes) after the run
#   --no-test       bring the stack up and wait for health, then stop
#   --reset <args>  forwarded to test_suite.py (e.g. --resetgetrequests browse)
#
# The image sbol-db:harness must exist (built from the sbol-db repo with
# `docker build -t sbol-db:harness .`).

set -u

PROJECT=sboldbproject
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TESTS_DIR="$(cd "$HERE/.." && pwd)"
COMPOSE="$HERE/docker-compose.yml"
VENV="$TESTS_DIR/venv"

DOWN=0
NO_TEST=0
PASSTHROUGH=()
while [[ $# -gt 0 ]]; do
    case "$1" in
        --down) DOWN=1; shift ;;
        --no-test) NO_TEST=1; shift ;;
        *) PASSTHROUGH+=("$1"); shift ;;
    esac
done

msg() { echo "[sboldb harness] $1"; }

compose() { docker compose -p "$PROJECT" -f "$COMPOSE" "$@"; }

msg "Cleaning any prior sbol-db stack"
compose down -v --remove-orphans >/dev/null 2>&1

msg "Starting sbol-db-backed SynBioHub stack"
compose up -d

msg "Waiting for SynBioHub to become healthy"
sbh_cid="$(compose ps -q synbiohub)"
if [[ -z "$sbh_cid" ]]; then
    msg "ERROR: synbiohub container not found"
    compose ps
    exit 1
fi

deadline=$(( SECONDS + 600 ))
while true; do
    status="$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$sbh_cid" 2>/dev/null)"
    if [[ "$status" == "healthy" ]]; then break; fi
    if [[ "$status" == "exited" || "$status" == "dead" ]]; then
        msg "ERROR: synbiohub container is $status"
        docker logs --tail 80 "$sbh_cid"
        exit 1
    fi
    if (( SECONDS >= deadline )); then
        msg "ERROR: timed out waiting for synbiohub health (last status: $status)"
        docker logs --tail 80 "$sbh_cid"
        exit 1
    fi
    sleep 5
done
msg "SynBioHub is healthy"

# The error-log helper in test_functions.py docker-cp's from this container.
SBH_CONTAINER="$(docker inspect -f '{{.Name}}' "$sbh_cid" | sed 's#^/##')"
export SBH_TEST_CONTAINER="$SBH_CONTAINER"

# SynBioHub's very first request after startup can render transiently; prime
# it so the suite doesn't race the warmup.
msg "Warming up SynBioHub"
for _ in 1 2 3; do curl -s -o /dev/null "http://localhost:7777/setup" || true; sleep 2; done

exitcode=0
if [[ "$NO_TEST" -eq 0 ]]; then
    msg "Running test_suite.py against sbol-db"
    # shellcheck disable=SC1091
    source "$VENV/bin/activate"
    ( cd "$TESTS_DIR" && python3 -u test_suite.py ${PASSTHROUGH[@]+"${PASSTHROUGH[@]}"} )
    exitcode=$?
    msg "test_suite.py exited with code $exitcode"
fi

if [[ "$DOWN" -eq 1 ]]; then
    msg "Tearing down stack"
    compose down -v --remove-orphans
else
    msg "Stack left running (project: $PROJECT). Tear down with:"
    msg "  docker compose -p $PROJECT -f $COMPOSE down -v"
fi

exit "$exitcode"
