#!/usr/bin/env sh

set -e

channelName=dltroutingchannel

# Join peer to the channel.
peer channel join -b $channelName.block
