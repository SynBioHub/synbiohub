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

# Clone the SBOLTestRunner for necessary files
message "pulling mehersam/SBOLTestRunner"
if cd SBOLTestRunner; then
    git pull;
    cd ..;
else
    git clone --recurse-submodules https://github.com/mehersam/SBOLTestRunner;
fi

#clone libSBOLj
message "pulling libSBOLj"
if cd libSBOLj; then
    git pull;
    cd ..;
else
    git clone https://github.com/SynBioDex/libSBOLj;
    cd libSBOLj;
    git submodule update --init --recursive;
    mvn package;
    cd ..;
fi

#!/bin/sh


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

# now stop containers and run just persistance tests
message "Persistance test"
bash ./stop_containers.sh
bash ./start_containers_persist.sh

python3 test_docker_persist.py "$@"
exitcode=$?
if [ $exitcode -ne 0 ]; then
    message "Exiting with code $exitcode."
    exit $exitcode
fi


# stop after test suite if the command line option is present
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

bash ./stop_containers.sh


message "finished running tests"
