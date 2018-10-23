#!/usr/bin/env sh

set -e

channelName=dltroutingchannel

# Create the channel
peer channel create -o $ORDERER_ADDRESS -c $channelName -f $CHANNEL_TX_FILE --tls $CORE_PEER_TLS_ENABLED --cafile $ORDERER_CA_FILE
