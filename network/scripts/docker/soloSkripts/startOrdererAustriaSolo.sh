#!/usr/bin/env sh

set -e

dockerConfigDir=./../../../configSolo/docker

docker-compose -f $dockerConfigDir/docker-compose.yaml up -d ordereraustria.dltrouting.com