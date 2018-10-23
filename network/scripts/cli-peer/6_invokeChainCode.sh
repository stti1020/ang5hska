#!/usr/bin/env sh

set -e

channelName=dltroutingchannel

# Invoke chain code on channel
# Write Bank
peer chaincode invoke -o $ORDERER_ADDRESS --tls $CORE_PEER_TLS_ENABLED --cafile $ORDERER_CA_FILE -C $channelName -n dltrouting -v 1.0 -c '{"Args":["writeBank", "COKSDE30XXX", "KREISSPARKASSE KOELN"]}'
peer chaincode invoke -o $ORDERER_ADDRESS --tls $CORE_PEER_TLS_ENABLED --cafile $ORDERER_CA_FILE -C $channelName -n dltrouting -v 1.0 -c '{"Args":["writeBank", "DAAEDED0XXX", "Deutsche Apotheker- und Aerztebank"]}'
peer chaincode invoke -o $ORDERER_ADDRESS --tls $CORE_PEER_TLS_ENABLED --cafile $ORDERER_CA_FILE -C $channelName -n dltrouting -v 1.0 -c '{"Args":["writeBank", "INV0ATW0", "Kommunalkredit Austria AG"]}'