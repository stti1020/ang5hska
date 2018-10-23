#!/usr/bin/env sh

set -e

dockerConfigDir=./../../../configSolo/docker

docker-compose -f $dockerConfigDir/docker-compose.yaml up -d ca.orgaustria.dltrouting.com peer0.orgaustria.dltrouting.com couchdb.peer0.orgaustria.dltrouting.com cli.peer0.orgaustria.dltrouting.com