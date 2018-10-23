#!/usr/bin/env sh

set -e

dockerConfigDir=./../../../configKafka/docker

docker-compose -f $dockerConfigDir/docker-compose.yaml up -d kafka2