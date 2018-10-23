#!/usr/bin/env sh

set -e

channelName=dltroutingchannel

# Query chain code on channel
peer chaincode query -C $channelName -n dltrouting -c '{"Args":["getBankByKey","COKSDE30XXX"]}'
peer chaincode query -C $channelName -n dltrouting -c '{"Args":["getBankByKey","DAAEDED0XXX"]}'
peer chaincode query -C $channelName -n dltrouting -c '{"Args":["getBankByKey","INV0ATW0"]}'