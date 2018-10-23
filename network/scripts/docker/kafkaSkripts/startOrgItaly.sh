#!/usr/bin/env sh

set -e

dockerConfigDir=./../../../configKafka/docker

docker-compose -f $dockerConfigDir/docker-compose.yaml up -d ca.orgitaly.dltrouting.com peer0.orgitaly.dltrouting.com couchdb.peer0.orgitaly.dltrouting.com cli.peer0.orgitaly.dltrouting.com 