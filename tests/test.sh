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
docker-compose -f ./synbiohub-docker/docker-compose.yml -p testproject up &
while [[ "$(docker logs --tail 1 testproject_synbiohub_1)" != "Resuming 0 job(s)" ]]
do
    sleep 5
done

message "Started successfully"

# wait for logs of other containers
sleep 30s

# create a new user
bash ./createuser.sh

# exit gracefully
kill -INT $!

message "finished running tests"
