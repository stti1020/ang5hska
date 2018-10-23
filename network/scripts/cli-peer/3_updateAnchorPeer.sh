#!/usr/bin/env sh

set -e

channelName=dltroutingchannel

# Update anchor peer of organisation
peer channel update -o $ORDERER_ADDRESS -c $channelName -f /etc/hyperledger/channel/${CORE_PEER_LOCALMSPID}anchors.tx --tls $CORE_PEER_TLS_ENABLED --cafile $ORDERER_CA_FILE
