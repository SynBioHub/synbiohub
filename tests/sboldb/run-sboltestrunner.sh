#!/bin/bash
#
# Run the Java SBOLTestRunner round-trip conformance suite against the
# sbol-db-backed SynBioHub stack.
#
# The suite submits every SBOL2 file in SBOLTestSuite to SynBioHub,
# retrieves it back, and compares the round-trip. It exercises the
# graph-store write and recursive-fetch paths that sbol-db serves.
#
# Prerequisites:
#   - The sbol-db stack is up with SynBioHub on http://localhost:7777
#     (run `tests/sboldb/test-sboldb.sh --no-test` first, or a full run).
#   - Docker (used to build the jars in a Java 8 + Maven container; the host
#     only needs a JRE to run them).
#
# Flags:
#   --rebuild   force a rebuild of the emulator and test-runner jars

set -u

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TESTS_DIR="$(cd "$HERE/.." && pwd)"
URL="http://localhost:7777"
MVN_IMAGE="maven:3-eclipse-temurin-8"

REBUILD=0
[[ "${1:-}" == "--rebuild" ]] && REBUILD=1

msg() { echo "[sboltestrunner] $1"; }

cd "$TESTS_DIR" || exit 1

if ! curl -sf -o /dev/null "$URL/setup" && ! curl -sf -o /dev/null "$URL"; then
    msg "ERROR: SynBioHub is not reachable at $URL. Bring the stack up first."
    exit 1
fi

# Clone the emulator and test runner if absent. SBOLTestRunner carries the
# SBOLTestSuite data as a submodule.
if [[ ! -d SynBioHubRunner ]]; then
    msg "Cloning SynBioHubRunner"
    git clone --recurse-submodules https://github.com/mehersam/SynBioHubRunner
fi
if [[ ! -d SBOLTestRunner ]]; then
    msg "Cloning SBOLTestRunner"
    git clone --recurse-submodules https://github.com/mehersam/SBOLTestRunner
fi

# The emulator reads its target instance from this settings file, baked in
# at build time.
cp Emulator_Settings.txt SynBioHubRunner/src/resources/Emulator_Settings.txt

EMULATOR_JAR=SynBioHubRunner/target/SBHEmulator-0.0.1-SNAPSHOT-withDependencies.jar
RUNNER_JAR=SBOLTestRunner/target/SBOLTestRunner-0.0.1-SNAPSHOT-withDependencies.jar

build_jar() {
    local dir="$1"
    msg "Building $dir (Java 8 + Maven container)"
    docker run --rm -v "$TESTS_DIR":/work -v sbhrunner-m2:/root/.m2 \
        -w "/work/$dir" "$MVN_IMAGE" mvn -B package -DskipTests
}

if [[ "$REBUILD" -eq 1 || ! -f "$EMULATOR_JAR" ]]; then build_jar SynBioHubRunner; fi
if [[ "$REBUILD" -eq 1 || ! -f "$RUNNER_JAR" ]]; then build_jar SBOLTestRunner; fi

msg "Running SBOLTestRunner against $URL"
rm -rf Timing Emulated Retrieved Compared
mkdir Timing Emulated Retrieved Compared

java -jar "$RUNNER_JAR" "java -jar $EMULATOR_JAR" "Compared/" "Retrieved/" "-e" "Emulated/"
exitcode=$?

msg "SBOLTestRunner exited with code $exitcode"
exit "$exitcode"
