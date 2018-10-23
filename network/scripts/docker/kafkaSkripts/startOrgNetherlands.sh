#!/usr/bin/env sh

set -e

dockerConfigDir=./../../../configKafka/docker

docker-compose -f $dockerConfigDir/docker-compose.yaml up -d ca.orgnetherlands.dltrouting.com peer0.orgnetherlands.dltrouting.com couchdb.peer0.orgnetherlands.dltrouting.com cli.peer0.orgnetherlands.dltrouting.com zookeeper2