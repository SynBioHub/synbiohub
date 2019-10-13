#!/bin/bash

cd tests
source ./testutil.sh

message "Running synbiohub test suite."

# Clone the SBOLTestRunner for necessary files
message "pulling mehersam/SBOLTestRunner"
if cd SBOLTestRunner; then
    git pull;
    cd ..;
else
    git clone --recurse-submodules https://github.com/mehersam/SBOLTestRunner;
fi


bash ./start_containers.sh
python3 -c "from first_time_setup import TestSetup; ts = TestSetup(); ts.test_post()"

bash ./run_sboltestrunner.sh
exitcode=$?
if [ $exitcode -ne 0 ]; then
    message "Exiting with code $exitcode."
    exit $exitcode
fi

bash ./stop_containers.sh


message "finished running tests"
