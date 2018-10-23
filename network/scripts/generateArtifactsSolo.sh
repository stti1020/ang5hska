#!/usr/bin/env sh

hlConfigDir=./../configSolo/hyperledger
cryptoConfigFile=$hlConfigDir/crypto-config.yaml

artifactsDir=./../artifactsSolo

cryptoArtifactsDir=$artifactsDir/crypto-config
ordererArtifactsDir=$artifactsDir/orderer
channelArtifactsDir=$artifactsDir/channel

channelName=dltroutingchannel
ordererProfile=DLTRoutingOrdererGenesis
channelProfile=DLTRoutingOrgsChannel
organisationMSPs="OrgAustriaMSP OrgGermanyMSP OrgItalyMSP OrgNetherlandsMSP"

export FABRIC_CFG_PATH=$hlConfigDir

set -e


echo ""
echo "##########################################################"
echo "# Generate Crypto Artifacts - cryptogen tool             #"
echo "##########################################################"
echo ""

if [ -d $cryptoArtifactsDir ]; then
  echo "# Delete crypto artifacts dir"
  rm -Rf $cryptoArtifactsDir
fi
if [ ! -d $cryptoArtifactsDir ]; then
  echo "# Create crypto artifacts dir"
  mkdir -p $cryptoArtifactsDir
fi
echo ""

cryptogen generate --config=$cryptoConfigFile --output=$cryptoArtifactsDir


echo ""
echo ""
echo ""
echo "##########################################################"
echo "# Generate Orderer & Channel Artifacts - cryptogen tool  #"
echo "##########################################################"
echo ""

if [ -d $ordererArtifactsDir ]; then
  echo "# Delete orderer artifacts dir"
  rm -Rf $ordererArtifactsDir
fi
if [ ! -d $ordererArtifactsDir ]; then
  echo "# Create orderer artifacts dir"
  mkdir -p $ordererArtifactsDir
fi
echo ""
if [ -d $channelArtifactsDir ]; then
  echo "# Delete channel artifacts dir"
  rm -Rf $channelArtifactsDir
fi
if [ ! -d $channelArtifactsDir ]; then
  echo "# Create channel artifacts dir"
  mkdir -p $channelArtifactsDir
fi
echo ""

echo "# Generate orderer genesis Block"
# Note: For some unknown reason (at least for now) the block file can't be
# named orderer.genesis.block or the orderer will fail to launch!
configtxgen -profile $ordererProfile -outputBlock $ordererArtifactsDir/genesis.block

echo ""
echo "# Generate channel configuration transaction"
configtxgen -profile $channelProfile -outputCreateChannelTx $channelArtifactsDir/channel.tx -channelID $channelName

for orgMSP in $organisationMSPs; do
  echo ""
  echo "# Generate anchor peer update for $orgMSP"
  configtxgen -profile $channelProfile -outputAnchorPeersUpdate $channelArtifactsDir/${orgMSP}anchors.tx -channelID $channelName -asOrg $orgMSP
done


