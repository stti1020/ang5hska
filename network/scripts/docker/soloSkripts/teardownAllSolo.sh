#!/usr/bin/env sh

set -e

dockerConfigDir=./../../../configSolo/docker
dockerChainCodeContainerNameStart=dev-

# Shut down the Docker containers for the system tests.
docker-compose -f $dockerConfigDir/docker-compose.yaml kill && docker-compose -f $dockerConfigDir/docker-compose.yaml down

chainCodeContainerIds=$(docker ps -a -q -f name=$dockerChainCodeContainerNameStart)
if ! [ -z "$chainCodeContainerIds" ]; then
  docker rm -f $chainCodeContainerIds
fi

# remove chaincode docker images
chainCodeImageIds=$(docker images ${dockerChainCodeContainerNameStart}* -q)
if ! [ -z "$chainCodeImageIds" ]; then
  docker rmi $chainCodeImageIds
fi

# remove the local state
rm -f ~/.hfc-key-store/*
