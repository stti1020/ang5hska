#!/usr/bin/env sh

set -e

dockerConfigDir=./../../../configKafka/docker

docker-compose -f $dockerConfigDir/docker-compose.yaml up -d ca.orggermany.dltrouting.com peer0.orggermany.dltrouting.com couchdb.peer0.orggermany.dltrouting.com cli.peer0.orggermany.dltrouting.com zookeeper1