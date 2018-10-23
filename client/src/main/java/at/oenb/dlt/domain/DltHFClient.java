package at.oenb.dlt.domain;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.protos.peer.Query.ChaincodeInfo;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.hyperledger.fabric.sdk.BlockInfo;
import org.hyperledger.fabric.sdk.BlockchainInfo;
import org.hyperledger.fabric.sdk.ChaincodeEndorsementPolicy;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.ChannelConfiguration;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.InstallProposalRequest;
import org.hyperledger.fabric.sdk.InstantiateProposalRequest;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.SDKUtils;
import org.hyperledger.fabric.sdk.TransactionInfo;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.TxReadWriteSetInfo;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.TransactionEventException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import lombok.extern.slf4j.Slf4j;

/**
 * Wrapper around HFClient calls.
 * <p>
 * To reduce complexity of the ledger calls and catch all kinds of checked
 * exceptions.
 */
@Slf4j
public abstract class DltHFClient {

	protected static final int TIMEOUT_SECONDS = 10;
	protected static final String[] EMPTY_ARGS = {};

	private final HFClient hfClient;
	private boolean verbose = false;

	public DltHFClient() {
		this.hfClient = HFClient.createNewInstance();
		try {
			hfClient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public void setUserContext(User user) {
		try {
			hfClient.setUserContext(user);
		} catch (InvalidArgumentException e) {
			throw new IllegalStateException();
		}
	}

	protected EventHub createEventHub(String eventHubName, String eventHubLocation, Properties properties) {
		try {
			return hfClient.newEventHub(eventHubName, eventHubLocation, properties);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	protected Orderer createOrderer(String ordererName, String ordererLocation, Properties properties) {
		try {
			return hfClient.newOrderer(ordererName, ordererLocation, properties);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	protected Peer createPeer(String peerName, String peerLocation, Properties properties) {
		try {
			return hfClient.newPeer(peerName, peerLocation, properties);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	protected Channel createOrReconstructChannel(String channelName, File channelConfigurationFile,
			DltOrganisation dltOrganisation) {

		try {
			try {
				return createChannel(channelName, dltOrganisation, channelConfigurationFile);
			} catch (Exception e) {
				log.error("Error at trying reconstruction", e);
				return reconstructChannel(channelName, dltOrganisation);
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	protected Set<String> queryPeerChannels(Peer peer) {
		try {
			return hfClient.queryChannels(peer);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	protected Channel createChannel(String channelName, DltOrganisation dltOrganisation,
			File channelConfigurationFile) {
		Orderer orderer = dltOrganisation.getOrdererOfOrg();
		Peer peer = dltOrganisation.getPeerOfOrg();
		EventHub eventHub = dltOrganisation.getEventHub();
		User peerAdmin = dltOrganisation.getPeerAdmin();

		try {
			ChannelConfiguration channelConfig = new ChannelConfiguration(channelConfigurationFile);
			byte[] channelConfigurationSignature = hfClient.getChannelConfigurationSignature(channelConfig, peerAdmin);

			Channel channel = hfClient.newChannel(channelName, orderer, channelConfig, channelConfigurationSignature);
			channel.joinPeer(peer);
			channel.addEventHub(eventHub);

			// for (Peer orgPeer : dltOrganisation.getPeersOfOtherOrg()) {
			// channel.addPeer(orgPeer);
			// }

			channel.initialize();
			return channel;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	protected Channel reconstructChannel(String channelName, DltOrganisation dltOrganisation) {

		Peer peer = dltOrganisation.getPeerOfOrg();
		Set<String> peerChannels = queryPeerChannels(peer);

		try {
			Channel channel = hfClient.newChannel(channelName);
			channel.addOrderer(dltOrganisation.getOrdererOfOrg());
			channel.addEventHub(dltOrganisation.getEventHub());

			if (peerChannels.contains(channelName)) {
				log.info("Channel {} exists for peer {}", channelName, peer.getName());
				channel.addPeer(peer);
			} else {
				log.info("Channel {} does not exist for peer {}", channelName, peer.getName());
				channel.joinPeer(peer);
			}

			// for (Peer orgPeer : dltOrganisation.getPeersOfOtherOrg()) {
			// channel.addPeer(orgPeer);
			// }

			channel.initialize();
			return channel;

		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	protected void installAndInstantiateChaincode(Channel channel, Peer peer, ChaincodeID chaincodeId,
			File chaincodeDirectory, File endorsementPolicyFile) {
		if (!isChaincodeInstalled(peer, chaincodeId)) {
			log.info(format("Peer %s is missing chaincode: %s, path:%s, version: %s", peer.getName(),
					chaincodeId.getName(), chaincodeId.getPath(), chaincodeId.getVersion()));
			installChaincode(peer, chaincodeId, chaincodeDirectory);
			try {
				log.info(
						"nachdem isChaincodeInstantiated immer false returned, wenn man nach dem installChaincode zu schnell nachfrägt, warten wir hier mal...");
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
		}

		if (!isChaincodeInstantiated(channel, peer, chaincodeId)) {
			log.info(format("Peer %s is missing instantiated chaincode: %s, path:%s, version: %s", peer.getName(),
					chaincodeId.getName(), chaincodeId.getPath(), chaincodeId.getVersion()));
			instantiateChaincode(channel, peer, chaincodeId, endorsementPolicyFile);
		}
	}

	protected boolean isChaincodeInstalled(Peer peer, ChaincodeID chaincodeId) {
		log.info("Checking installed chaincode {} on peer {}", toChaincodeString(chaincodeId), peer.getName());
		List<ChaincodeInfo> installedChaincodes;
		try {
			installedChaincodes = hfClient.queryInstalledChaincodes(peer);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		return installedChaincodes.stream()
				.anyMatch(installedChaincode -> equalsChaincode(installedChaincode, chaincodeId));
	}

	protected void installChaincode(Peer peer, ChaincodeID chaincodeId, File chaincodeDirectory) {
		log.info("Install chaincode {} on peer {}", toChaincodeString(chaincodeId), peer.getName());
		InstallProposalRequest installProposalRequest = hfClient.newInstallProposalRequest();
		installProposalRequest.setChaincodeID(chaincodeId);

		try {
			installProposalRequest.setChaincodeSourceLocation(chaincodeDirectory);
			installProposalRequest.setChaincodeVersion(chaincodeId.getVersion());
			Collection<ProposalResponse> responses = hfClient.sendInstallProposal(installProposalRequest,
					Collections.singletonList(peer));
			SDKUtils.getProposalConsistencySets(responses);
			validate(responses);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected boolean isChaincodeInstantiated(Channel channel, Peer peer, ChaincodeID chaincodeId) {
		log.info("Checking instantiated chaincode {} on channel {} via peer {}", toChaincodeString(chaincodeId),
				channel.getName(), peer.getName());
		List<ChaincodeInfo> instantiatedChaincodes;
		try {
			instantiatedChaincodes = channel.queryInstantiatedChaincodes(peer);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		return instantiatedChaincodes.stream()
				.anyMatch(instantiatedChaincode -> equalsChaincode(instantiatedChaincode, chaincodeId));
	}

	protected void instantiateChaincode(Channel channel, Peer peer, ChaincodeID chaincodeId,
			File endorsementPolicyFile) {
		log.info("Instantiate chaincode {} on channel {}", toChaincodeString(chaincodeId), channel.getName());

		try {
			InstantiateProposalRequest instantiateProposalRequest = hfClient.newInstantiationProposalRequest();
			instantiateProposalRequest.setProposalWaitTime(100_000);
			instantiateProposalRequest.setChaincodeID(chaincodeId);
			instantiateProposalRequest.setArgs(new String[] {});
			instantiateProposalRequest.setTransientMap(Collections.emptyMap());

			ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
			chaincodeEndorsementPolicy.fromYamlFile(endorsementPolicyFile);
			instantiateProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);

			Collection<ProposalResponse> responses = channel.sendInstantiationProposal(instantiateProposalRequest,
					Arrays.asList(peer));
			validate(responses);

			channel.sendTransaction(responses).thenApply(transactionEvent -> {
				if (!transactionEvent.isValid()) {
					throw new IllegalStateException();
				}
				return null;
			}).get(180, TimeUnit.SECONDS);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String queryByChaincode(Channel channel, ChaincodeID chaincodeId, String functionName) {
		return queryByChaincode(channel, chaincodeId, functionName, EMPTY_ARGS);
	}

	protected String queryByChaincode(Channel channel, ChaincodeID chaincodeId, String functionName,
			String[] functionArguments) {
		log.info("query chaincode {} on channel {} for function {} with arguments {}", toChaincodeString(chaincodeId),
				channel.getName(), functionName, functionArguments);
		QueryByChaincodeRequest queryByChaincodeRequest = hfClient.newQueryProposalRequest();
		queryByChaincodeRequest.setArgs(functionArguments);
		queryByChaincodeRequest.setFcn(functionName);
		queryByChaincodeRequest.setChaincodeID(chaincodeId);

		Collection<ProposalResponse> queryProposals = queryByChaincodeHandleException(channel, queryByChaincodeRequest);

		StringBuilder response = new StringBuilder();
		for (ProposalResponse proposalResponse : queryProposals) {
			response.append(proposalResponse.getProposalResponse().getResponse().getPayload().toStringUtf8());
		}
		String string = response.toString();
		log.info("received response {}", string);
		return string;
	}

	private Collection<ProposalResponse> queryByChaincodeHandleException(Channel channel,
			QueryByChaincodeRequest queryByChaincodeRequest) {
		try {
			Collection<ProposalResponse> queryProposals = channel.queryByChaincode(queryByChaincodeRequest);
			validateProposalResponse(queryProposals);
			return queryProposals;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	protected TransactionEvent sendBlocked(Channel channel, ChaincodeID chaincodeId, String functionName) {
		return sendBlocked(channel, chaincodeId, functionName, EMPTY_ARGS);
	}

	protected TransactionEvent sendBlocked(Channel channel, ChaincodeID chaincodeId, String functionName,
			String[] functionArguments) {
		try {
			return send(channel, chaincodeId, functionName, functionArguments).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	protected CompletableFuture<TransactionEvent> send(Channel channel, ChaincodeID chaincodeId, String functionName,
			String[] functionArguments) {
		return verbose ? sendInternal(channel, chaincodeId, functionName, functionArguments)
				: sendInternalVerbose(channel, chaincodeId, functionName, functionArguments);
	}

	private CompletableFuture<TransactionEvent> sendInternal(Channel channel, ChaincodeID chaincodeId,
			String functionName, String[] functionArguments) {
		log.info("send");
		Collection<ProposalResponse> transactionProposalResponse = sendTransactionProposals(channel, chaincodeId,
				functionName, functionArguments);
		return sendTransaction(transactionProposalResponse, channel);
	}

	private CompletableFuture<TransactionEvent> sendInternalVerbose(Channel channel, ChaincodeID chaincodeId,
			String functionName, String[] functionArguments) {
		log.info("send - verbose");
		Collection<ProposalResponse> transactionProposalResponse = sendTransactionProposals(channel, chaincodeId,
				functionName, functionArguments);
		return sendTransaction(transactionProposalResponse, channel).thenApply(this::printTransactionEventInfo)
				.thenApply(transactionEvent -> printBlockChainInfo(channel, transactionEvent));
	}

	protected Collection<ProposalResponse> sendTransactionProposals(Channel channel, ChaincodeID chaincodeId,
			String functionName, String[] functionArguments) {
		log.info(
				"sending transaction proposals to chaincode {} of all peers on channel {} for function {} with arguments {}",
				toChaincodeString(chaincodeId), channel.getName(), functionName, functionArguments);

		TransactionProposalRequest transactionProposalRequest = hfClient.newTransactionProposalRequest();
		transactionProposalRequest.setChaincodeID(chaincodeId);
		transactionProposalRequest.setFcn(functionName);
		transactionProposalRequest.setProposalWaitTime(5_000l);
		transactionProposalRequest.setArgs(functionArguments);

		Collection<ProposalResponse> transactionProposalResponses;
		try {
			transactionProposalResponses = channel.sendTransactionProposal(transactionProposalRequest,
					channel.getPeers());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		validateProposalResponse(transactionProposalResponses);
		return transactionProposalResponses;
	}

	protected CompletableFuture<TransactionEvent> sendTransaction(
			Collection<ProposalResponse> transactionProposalResponses, Channel channel) {
		log.info("sending transaction proposal responses of channel {} to orderers", channel.getName());
		return channel.sendTransaction(transactionProposalResponses);
	}

	protected Void handleTransactionException(Throwable throwable) {
		throw new IllegalStateException(extractTransactionExceptionMessage(throwable));
	}

	protected String extractTransactionExceptionMessage(Throwable throwable) {
		return Optional.ofNullable(extractTransactionEventFromException(throwable))
				.map(transactionEvent -> format("Transaction with transaction ID %s failed. %s",
						transactionEvent.getTransactionID(), throwable.getMessage()))
				.orElse(format("Test failed with %s exception %s", throwable.getClass().getName(),
						throwable.getMessage()));
	}

	protected TransactionEvent extractTransactionEventFromException(Throwable throwable) {
		return (throwable instanceof TransactionEventException)
				? ((TransactionEventException) throwable).getTransactionEvent()
				: null;
	}

	protected TransactionEvent printTransactionEventInfo(TransactionEvent transactionEvent) {
		if (!transactionEvent.isValid()) {
			log.info("Transaction is not valid");
			throw new IllegalStateException();
		}
		log.info("Finished transaction with transaction id {}", transactionEvent.getTransactionID());
		return transactionEvent;
	}

	protected TransactionEvent printBlockChainInfo(Channel channel, TransactionEvent transactionEvent) {
		try {
			String transactionId = transactionEvent.getTransactionID();
			log.info("Printing block chain info for transaction {} on channel {}", transactionId, channel.getName());

			// Channel queries
			BlockchainInfo channelInfo = channel.queryBlockchainInfo();
			log.info("Channel info for : " + channel.getName());
			log.info("Channel height: " + channelInfo.getHeight());
			String chainCurrentHash = Hex.encodeHexString(channelInfo.getCurrentBlockHash());
			String chainPreviousHash = Hex.encodeHexString(channelInfo.getPreviousBlockHash());
			log.info("Chain current block hash: " + chainCurrentHash);
			log.info("Chainl previous block hash: " + chainPreviousHash);

			// Query by block number. Should return latest block, i.e. block number 2
			BlockInfo returnedBlock = channel.queryBlockByNumber(channelInfo.getHeight() - 1);
			String previousHash = Hex.encodeHexString(returnedBlock.getPreviousHash());
			log.info("queryBlockByNumber returned correct block with blockNumber " + returnedBlock.getBlockNumber()
					+ "  previous_hash " + previousHash);
			assertEquals(channelInfo.getHeight() - 1, returnedBlock.getBlockNumber());
			assertEquals(chainPreviousHash, previousHash);

			// Query by block hash. Using latest block's previous hash so should return
			// block number 1
			byte[] hashQuery = returnedBlock.getPreviousHash();
			returnedBlock = channel.queryBlockByHash(hashQuery);
			log.info("queryBlockByHash returned block with blockNumber " + returnedBlock.getBlockNumber());
			assertEquals(channelInfo.getHeight() - 2, returnedBlock.getBlockNumber());

			// Query block by TxID. Since it's the last TxID, should be block 2
			returnedBlock = channel.queryBlockByTransactionID(transactionId);
			log.info("queryBlockByTxID returned block with blockNumber " + returnedBlock.getBlockNumber());
			assertEquals(channelInfo.getHeight() - 1, returnedBlock.getBlockNumber());

			// query transaction by ID
			TransactionInfo txInfo = channel.queryTransactionByID(transactionId);
			log.info("QueryTransactionByID returned TransactionInfo: txID " + txInfo.getTransactionID()
					+ " validation code " + txInfo.getValidationCode().getNumber());

			log.info("Printing block chain info for transaction {} on channel {} done", transactionId,
					channel.getName());

		} catch (Exception e) {
			log.error("Caught exception while printing blockchain info", e);
		}

		return transactionEvent;
	}

	private void validate(Collection<ProposalResponse> responses) {
		Set<ProposalResponse> successResponses = responses.stream()
				.filter(response -> response.getStatus() == ProposalResponse.Status.SUCCESS).collect(toSet());
		if (responses.size() != successResponses.size()
				&& !responses.iterator().next().getMessage().contains("chaincode exists")) {
			throw new IllegalStateException("Not enough endorsers for install");
		}
	}

	private void validateProposalResponse(Collection<ProposalResponse> transactionProposalResponses) {
		for (ProposalResponse proposalResponse : transactionProposalResponses) {
			try {
				TxReadWriteSetInfo txReadWriteSetInfo = proposalResponse.getChaincodeActionResponseReadWriteSetInfo();
				int nsRwsetCount = txReadWriteSetInfo.getNsRwsetCount();
				for (int i = 0; i < nsRwsetCount; i++) {
					log.info(String.format("proposalResponse %s",
							txReadWriteSetInfo.getNsRwsetInfo(i).getRwset().toString()));
				}
			} catch (Exception e) {
				log.info("no valid proposalResponse");
			}
		}
		String joinedErrorMessage = transactionProposalResponses.stream().filter(ProposalResponse::isInvalid)
				.map(proposalResponse -> proposalResponse.getPeer().getName() + ": " + proposalResponse.getMessage())
				.collect(Collectors.joining());
		if (!joinedErrorMessage.isEmpty()) {
			throw new IllegalStateException(joinedErrorMessage);
		}
	}

	private void assertEquals(Object obj1, Object obj2) {
		if (!obj1.equals(obj2)) {
			throw new IllegalArgumentException(String.format("%s != %s", obj1, obj2));
		}
	}

	private String toChaincodeString(ChaincodeID chaincodeId) {
		return String.format("[name: %s, version: %s,  path: %s]", chaincodeId.getName(), chaincodeId.getVersion(),
				chaincodeId.getPath());
	}

	private boolean equalsChaincode(ChaincodeInfo chaincodeInfo, ChaincodeID chaincodeId) {
		ChaincodeID chaincodeIdFromInfo = ChaincodeID.newBuilder().setName(chaincodeInfo.getName())
				.setVersion(chaincodeInfo.getVersion()).setPath(chaincodeInfo.getPath()).build();
		return equalsChaincode(chaincodeIdFromInfo, chaincodeId);
	}

	private boolean equalsChaincode(ChaincodeID chaincodeIdOne, ChaincodeID chaincodeIdTwo) {
		return chaincodeIdOne.getName().equals(chaincodeIdTwo.getName())
				&& chaincodeIdOne.getPath().equals(chaincodeIdTwo.getPath())
				&& chaincodeIdOne.getVersion().equals(chaincodeIdTwo.getVersion());
	}
}
