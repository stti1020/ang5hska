#!/usr/bin/env sh

set -e

channelName=dltroutingchannel

# Instantiate chain code on channel
peer chaincode instantiate -o $ORDERER_ADDRESS --tls $CORE_PEER_TLS_ENABLED --cafile $ORDERER_CA_FILE -C $channelName -n dltrouting -v 1.0 -c '{"Args":["init"]}' -P "OR ('OrgAustriaMSP.member', 'OrgGermanyMSP.member', 'OrgItalyMSP.member', 'OrgNetherlandsMSP.member')" 

