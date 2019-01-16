#!/bin/bash

cd tests

source ./testutil.sh

message "Running synbiohub test suite."
message "Cleaning old test containers if they exist"

bash ./testcleanup.sh


# Clone the necessary repositories
message "pulling mehersam/SBOLTestRunner"
if cd SBOLTestRunner; then
    git pull;
    cd ..;
else
    git clone --recurse-submodules https://github.com/mehersam/SBOLTestRunner;
fi


message "pulling mehersam/SynBioHubRunner"
if cd SBOLTestRunner; then
    git pull;
    cd ..;
else
    git clone --recurse-submodules https://github.com/mehersam/SBOLTestRunner;
fi


#message pulling mhersam/SBOLTestRunner
#if cd SynBioHubRunner
#git clone --recurse-submodules https://github.com/mehersam/SynBioHubRunner

message "pulling synbiohub/synbiohub-docker"
if cd synbiohub-docker; then
    git pull;
    cd ..;
else
    # clone the synbiohub docker compose file in order to run docker containers
    git clone https://github.com/synbiohub/synbiohub-docker;
fi

message "Building SBOLTestRunner"
cd SBOLTestRunner
mvn package
cd ..

message "Starting SynBioHub from Containers"
docker-compose -f ./synbiohub-docker/docker-compose.yml -p testsuiteproject up -d
while [[ "$(docker inspect testsuiteproject_synbiohub_1 | jq .[0].State.Health.Status)" != "\"healthy\"" ]]
do
    sleep 5
    message "Waiting for synbiohub container to be healthy."
done

message "Started successfully"

if [[ "$@" == "-stopafterstart" ]]
then
    message "Exiting early."
    exit 0
fi

message "Running test suite."

# run the set up script

python3 test_suite.py "$@"
exitcode=$?
if [ $exitcode -ne 0 ]; then
    message "Exiting with code $exitcode."
    exit $exitcode
fi

message "Running SBOLTestRunner"



# stop the containers
message "Stopping containers"
docker stop testsuiteproject_synbiohub_1
docker stop testsuiteproject_explorer_1
docker stop testsuiteproject_autoheal_1
docker stop testsuiteproject_virtuoso_1


message "finished running tests"
