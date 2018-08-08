#!/bin/bash

# Start a container with the old image
docker run \
    -p 7787:7777 \
    -v synbiohub-test:/mnt \
    --name synbiohub-old \
    -d synbiohub/synbiohub:1.2.0

# tear down the container when we exit
trap "docker kill synbiohub-old; docker rm synbiohub-old" EXIT

# Wait for it to come online...
RESULT=300

while [[ "$RESULT" -ne 302 ]]
do 
    RESULT=$(curl -X POST \
                --write-out "%{http_code}" \
                --silent \
                --output /dev/null \
                -F "userName=testuser" \
                -F "userFullName=Test User" \
                -F "userEmail=test@user.synbiohub" \
                -F "userPassword=test" \
                -F "userPasswordConfirm=test" \
                -F "instanceName=Test Synbiohub" \
                -F "instanceURL=http://localhost:7787/" \
                -F "color=#D25627" \
                -F "frontPageText=text" \
                -F "virtuosoINI=/etc/virtuoso-opensource-7/virtuoso.ini" \
                -F "virtuosoDB=/var/lib/virtuoso-opensource-7/db" \
                -F "allowPublicSignup=true" \
                http://localhost:7787/setup)
    sleep 5
done

docker exec synbiohub-old cp /opt/synbiohub/synbiohub.sqlite /mnt/data/synbiohub.sqlite
docker exec synbiohub-old chown synbiohub /mnt/data/synbiohub.sqlite
docker exec synbiohub-old chgrp synbiohub /mnt/data/synbiohub.sqlite

docker kill synbiohub-old
docker rm synbiohub-old

docker run \
    -p 7797:7777 \
    -v synbiohub-test:/mnt \
    --name synbiohub-new \
    -d synbiohub/synbiohub:snapshot

trap "docker kill synbiohub-new; docker rm synbiohub-new; docker volume rm synbiohub-test" EXIT

RESULT=000
while [[ "$RESULT" -eq 000 ]]
do 
    RESULT=$(curl -X POST \
                --write-out "%{http_code}" \
                --silent \
                --verbose \
                -H "Expect: " \ 
                -F "email=testuser" \
                -F "password=test" \
                http://localhost:7797/login)
    echo "$RESULT"
    sleep 5
done

if [[ "$RESULT" -eq 200 ]] 
then
    echo "Successful!"
else
    echo "Failed!"
fi
