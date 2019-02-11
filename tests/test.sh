#!/bin/bash

cd tests

source ./testutil.sh

# first, if it was run with help, just run the test script with help
if [[ "$@" == "--help" || "$@" == "-h" ]]
then
    python3 test_suite.py "$@"
    exit 0
fi

message "Running synbiohub test suite."

bash ./start_containers.sh

for var in "$@"
do
    if [[ $var == "--stopafterstart" ]]
    then
	echo "Exiting after starting up test server."
	exit 1
    fi
done


message "Running test suite."

# run the set up script

python3 test_suite.py "$@"
exitcode=$?
if [ $exitcode -ne 0 ]; then
    message "Exiting with code $exitcode."
    exit $exitcode
fi

for var in "$@"
do
    if [[ $var == "--stopaftertestsuite" ]]
    then
	echo "Stopping after test suite ran."
	exit 0
    fi
done

bash ./run_sboltestrunner.sh
exitcode=$?
if [ $exitcode -ne 0 ]; then
    message "Exiting with code $exitcode."
    exit $exitcode
fi

# stop the containers
message "Stopping containers"
docker stop testsuiteproject_synbiohub_1
docker stop testsuiteproject_explorer_1
docker stop testsuiteproject_autoheal_1
docker stop testsuiteproject_virtuoso_1


message "finished running tests"
