#!/usr/bin/env sh

set -e

channelName=dltroutingchannel

# Join peer to the channel.
peer channel fetch config $channelName.block -c $channelName -o $ORDERER_ADDRESS --tls --cafile $ORDERER_CA_FILE
