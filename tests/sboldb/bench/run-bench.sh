#!/bin/bash
#
# Benchmark Virtuoso against sbol-db on SynBioHub's triplestore workloads.
#
# Brings up the side-by-side bench stack (Virtuoso + sbol-db + Postgres),
# waits for both triplestores to answer, then runs bench.py, which loads the
# same SBOL corpus into each backend and times ingest and the realized
# SynBioHub read queries. Results land in results/bench-<host>.json.
#
# Flags:
#   --down   tear the stack down (and drop volumes) after the run
#   <other>  forwarded to bench.py (e.g. --iterations 100)

set -eu

PROJECT=sboldbbench
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TESTS_DIR="$(cd "$HERE/../.." && pwd)"
COMPOSE="$HERE/docker-compose.yml"
VENV="$TESTS_DIR/venv"
CORPUS="$TESTS_DIR/Emulated"
RESULTS="$HERE/results"

DOWN=0
PASSTHROUGH=()
while [[ $# -gt 0 ]]; do
    case "$1" in
        --down) DOWN=1; shift ;;
        *) PASSTHROUGH+=("$1"); shift ;;
    esac
done

msg() { echo "[bench harness] $1"; }
compose() { docker compose -p "$PROJECT" -f "$COMPOSE" "$@"; }

if [[ ! -d "$CORPUS" ]]; then
    msg "ERROR: corpus not found at $CORPUS"
    msg "Generate it first with tests/sboldb/run-sboltestrunner.sh"
    exit 1
fi

mkdir -p "$RESULTS"

msg "Cleaning any prior bench stack"
compose down -v --remove-orphans >/dev/null 2>&1 || true

msg "Starting Virtuoso + sbol-db bench stack"
compose up -d

# shellcheck disable=SC1091
source "$VENV/bin/activate"

OUT="${BENCH_OUT:-$RESULTS/bench-$(hostname -s).json}"
msg "Running bench.py (results -> $OUT)"
python3 -u "$HERE/bench.py" \
    --corpus "$CORPUS" \
    --out "$OUT" \
    ${PASSTHROUGH[@]+"${PASSTHROUGH[@]}"}

if [[ -z "${BENCH_NO_REPORT:-}" ]]; then
    msg "Rendering LaTeX fragments"
    python3 -u "$HERE/gen_report.py" "$OUT" --outdir "$HERE/out"
fi

if [[ "$DOWN" -eq 1 ]]; then
    msg "Tearing down bench stack"
    compose down -v --remove-orphans
else
    msg "Stack left running (project: $PROJECT). Tear down with:"
    msg "  docker compose -p $PROJECT -f $COMPOSE down -v"
fi
