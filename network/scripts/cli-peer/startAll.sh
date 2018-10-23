#!/usr/bin/env sh

set -e

./1_createChannel.sh
./1_fetchChannel.sh
./2_joinChannel.sh
./3_updateAnchorPeer.sh
./4_installChaincode.sh
./5_instantiateChaincode.sh
#./6_invokeChainCode.sh
#./7_queryChainCode.sh
