#!/bin/bash

TAG=$(git tag --points-at HEAD)

if [ -z "$TAG" ];
then
    TAG="snapshot"
else
    docker tag synbiohub/synbiohub:snapshot-standalone \
               synbiohub/synbiohub:$TAG-standalone
fi

echo "Pushing synbiohub/synbiohub:$TAG-standalone"

docker login -u "$DOCKER_USERNAME" -p "$DOCKER_PASSWORD"
docker push synbiohub/synbiohub:$TAG-standalone
