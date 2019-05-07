#!/bin/bash


cd tests

source ./testutil.sh

message "SBOLTestRunner"



message "pulling mehersam/SynBioHubRunner"
if cd SynBioHubRunner; then
    git pull;
    cd ..;
else
    git clone --recurse-submodules https://github.com/mehersam/SynBioHubRunner;
fi


message "Setting up SynBioHubRunner"
cp Emulator_Settings.txt SynBioHubRunner/src/resources/Emulator_Settings.txt
cd SynBioHubRunner
mvn package
cd ../

message "Building TestRunner"
cd SBOLTestRunner
mvn package
cd ../

message "Running SBOLTestRunner"

rm -rf Timing Emulated Retrieved Compared
mkdir Timing Emulated Retrieved Compared
java -jar SBOLTestRunner/target/SBOLTestRunner-0.0.1-SNAPSHOT-withDependencies.jar "java -jar SynBioHubRunner/target/SBHEmulator-0.0.1-SNAPSHOT-withDependencies.jar" "Compared/" "Retrieved/" "-e" "Emulated/"


exitcode=$?
if [ $exitcode -ne 0 ]; then
    python3 print_error_log.py "$@"
    message "Exiting with code $exitcode."
    exit $exitcode
fi
