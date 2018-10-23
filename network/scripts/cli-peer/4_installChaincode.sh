#!/usr/bin/env sh

set -e

# Install chain code on peer
peer chaincode install -n dltrouting -v 1.0 -p github.com/hyperledger/fabric/chaincode/dltrouting