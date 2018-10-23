package at.oenb.dlt.domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.security.PrivateKey;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;

import at.oenb.dltrouting.domain.DltRoutingHFClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DltOrganisation {

	private final HFCAClient caClient;

	private final DltUser admin;
	private final DltUser peerAdmin;
	private final Set<User> users = new HashSet<>();

	private final Set<Peer> peers = new HashSet<>();
	private final Set<Orderer> orderers = new HashSet<>();
	private final Set<EventHub> eventHubs = new HashSet<>();

	private DltConfig config;
	private DltOrganisationConfig organisationConfig;

	public DltOrganisation(DltConfig config, DltOrganisationConfig organisationConfig, DltRoutingHFClient dltClient) {
		this.config = config;
		this.organisationConfig = organisationConfig;

		// TODO create orderer and peer object from config object and make it work with
		// more peers/orderers
		List<OrdererConfig> ordererConfigs = this.config.getOrdererConfigs();
		List<PeerConfig> peerConfigs = this.config.getPeerConfigs();

		this.caClient = createCaClient();
		this.admin = createAdmin();
		this.peerAdmin = createPeerAdmin();
		createUser("userZ", "secretZ");

		initialize(dltClient, ordererConfigs, peerConfigs);
	}

	private void initialize(DltRoutingHFClient dltClient, List<OrdererConfig> ordererConfigs,
			List<PeerConfig> peerConfigs) {
		dltClient.setUserContext(peerAdmin);

		for (OrdererConfig ordererConfig : ordererConfigs) {
			this.orderers.add(createOrderer(dltClient, ordererConfig));
		}

		for (PeerConfig peerConfig : peerConfigs) {
			this.peers.add(createPeer(dltClient, peerConfig));
			this.eventHubs.add(createEventHub(dltClient, peerConfig));
		}
	}

	private HFCAClient createCaClient() {
		String absoluteCaCertFilePath = organisationConfig.getAbsoluteCaCertFilePath();
		Properties properties = new Properties();
		properties.setProperty(PropertyKeys.PEM_FILE, absoluteCaCertFilePath);
		properties.setProperty(PropertyKeys.ALLOW_ALL_HOST_NAMES, "true"); // TODO testing environment only NOT FOR
																			// PRODUCTION!

		HFCAClient hfCaClient;
		try {
			hfCaClient = HFCAClient.createNewInstance(organisationConfig.getCaUrl(), properties);
		} catch (MalformedURLException e) {
			throw new IllegalStateException(e);
		}
		hfCaClient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
		return hfCaClient;
	}

	private Peer createPeer(DltHFClient dltClient, PeerConfig peerConfig) {
		String absolutePeerCertFilePath = peerConfig.getAbsoluteCertFilePath();
		String peerName = peerConfig.getName();
		Properties properties = createProperties(absolutePeerCertFilePath, peerName);
		return dltClient.createPeer(peerName, peerConfig.getUrl(), properties);
	}

	private EventHub createEventHub(DltHFClient dltClient, PeerConfig peerConfig) {
		String absolutePeerCertFilePath = peerConfig.getAbsoluteCertFilePath();
		String eventHubName = peerConfig.getEventHubName();
		Properties properties = createProperties(absolutePeerCertFilePath, eventHubName);
		return dltClient.createEventHub(eventHubName, peerConfig.getEventHubUrl(), properties);
	}

	private Orderer createOrderer(DltHFClient dltClient, OrdererConfig ordererConfig) {
		String absolutOrdererCertFilePath = ordererConfig.getAbsoluteCertFilePath();
		String ordererName = ordererConfig.getName();
		Properties properties = createProperties(absolutOrdererCertFilePath, ordererName);
		return dltClient.createOrderer(ordererName, ordererConfig.getUrl(), properties);
	}

	private DltUser createPeerAdmin() {
		String peerAdminName = organisationConfig.getPeerAdminName();
		File peerKeyFile = organisationConfig.getPeerAdminKeyFile();
		File peerCertFile = organisationConfig.getPeerAdminCertFile();
		return createEnrolledDltUser(peerAdminName, peerKeyFile, peerCertFile);
	}

	private DltUser createAdmin() {
		return createEnrolledDltUser(config.getAdminUsername(), config.getAdminPassword(), false);
	}

	public DltUser createUser(String username, String secret) {
		DltUser newUser = createEnrolledDltUser(username, secret, true);
		this.users.add(newUser);
		return newUser;
	}

	private DltUser createEnrolledDltUser(String username, File keyFile, File certFile) {
		log.info("create enrolled user {} with key and cert files", username);
		PrivateKey privateKey;
		String certificate;
		try {
			privateKey = createPrivateKeyFromBytes(IOUtils.toByteArray(new FileInputStream(keyFile)));
			certificate = new String(IOUtils.toByteArray(new FileInputStream(certFile)), CharEncoding.UTF_8);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		UserEnrollment userEnrollment = new UserEnrollment(privateKey, certificate);
		return createDltUserWithEnrollment(username, userEnrollment);
	}

	private DltUser createEnrolledDltUser(String username, String secret, boolean register) {
		log.info("create enrolled user {} with secret", username);
		Enrollment userEnrollment = getUserEnrollment(username, secret);
		if (userEnrollment == null && register) {
			String registrationSecret = registerUser(username, secret);
			userEnrollment = getUserEnrollment(username, registrationSecret);
		}
		if (userEnrollment == null) {
			throw new IllegalStateException(
					String.format("User %s can't be created due to missing enrollment", username));
		}
		return createDltUserWithEnrollment(username, userEnrollment);
	}

	private Enrollment getUserEnrollment(String username, String secret) {
		try {
			log.info("enroll user {}", username);
			return getCAClient().enroll(username, secret);
		} catch (EnrollmentException e) {
			log.info("no enrollment existing for user {}", username);
			return null;
		} catch (InvalidArgumentException e) {
			throw new IllegalStateException(e);
		}
	}

	private String registerUser(String username, String secret) {
		String registrationSecret;
		try {
			log.info("register user {}", username);
			// TODO only works with org1.department1. WHY?
			RegistrationRequest registrationRequest = new RegistrationRequest(username, "org1.department1");
			registrationRequest.setSecret(secret);
			registrationSecret = getCAClient().register(registrationRequest, admin);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		if (!secret.equals(registrationSecret)) {
			throw new IllegalStateException(
					String.format("Secret and registration secret of user %s do not match", username));
		}
		return registrationSecret;
	}

	private DltUser createDltUserWithEnrollment(String username, Enrollment userEnrollment) {
		DltUser user = new DltUser(username, getOrganisationName());
		user.setEnrollment(userEnrollment);
		user.setMspId(getMspId());
		return user;
	}

	private PrivateKey createPrivateKeyFromBytes(byte[] data) {
		try (PEMParser pemParser = new PEMParser(new StringReader(new String(data)))) {
			return new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
					.getPrivateKey((PrivateKeyInfo) pemParser.readObject());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private Properties createProperties(String pemFilePath, String hostname) {
		Properties properties = new Properties();
		properties.put(PropertyKeys.PEM_FILE, pemFilePath);
		properties.put(PropertyKeys.HOSTNAME_OVERRIDE, hostname);
		properties.put(PropertyKeys.NEGOTIATION_TYPE, "TLS");
		properties.put(PropertyKeys.SSL_PROVIDER, "openSSL");
		properties.put(PropertyKeys.GRPC_KEEP_ALIVE_TIME, new Object[] { 5L, TimeUnit.MINUTES });
		properties.put(PropertyKeys.GRPC_KEEP_ALIVE_TIMEOUT, new Object[] { 8L, TimeUnit.SECONDS });
		properties.put(PropertyKeys.GRPC_MAX_INBOUND_MESSAGE_SIZE, 9000000);
		return properties;
	}

	private File getCheckedFile(String filePath, String type) {
		File file = new File(filePath);
		if (!file.exists() || !file.isFile()) {
			throw new IllegalStateException(String.format("Missing %s at path %s.", type, file.getAbsolutePath()));
		}
		return file;
	}

	private HFCAClient getCAClient() {
		return caClient;
	}

	private String getDomain() {
		return organisationConfig.getDomain();
	}

	public EventHub getEventHub() {
		// assertOne(eventHubs, "event hub");
		for (EventHub eventHub : eventHubs) {
			if (eventHub.getName().equalsIgnoreCase("peer0." + admin.getOrganization() + ".dltrouting.com"))
				return eventHub;
		}
		return null;
	}

	public String getMspId() {
		return organisationConfig.getMspId();
	}

	private String getOrganisationName() {
		return organisationConfig.getName();
	}

	public Orderer getOrdererOfOrg() {
		for (Orderer orderer : orderers) {
			if (orderer.getName().equalsIgnoreCase("host." + admin.getOrganization() + ".dltrouting.com"))
				return orderer;
		}
		throw new IllegalStateException();
	}

	public Peer getPeerOfOrg() {
		for (Peer peer : peers) {
			if (peer.getName().equalsIgnoreCase("peer0." + admin.getOrganization() + ".dltrouting.com"))
				return peer;
		}
		throw new IllegalStateException();
	}

	public Set<Peer> getPeersOfOtherOrg() {
		Set<Peer> peers = new HashSet<>(this.peers);
		peers.remove(getPeerOfOrg());
		return peers;
	}

	public DltUser getPeerAdmin() {
		return peerAdmin;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DltOrganisation [name=");
		builder.append(getOrganisationName());
		builder.append(", mspId=");
		builder.append(getMspId());
		builder.append(", domainName=");
		builder.append(getDomain());
		builder.append(", admin=");
		builder.append(admin);
		builder.append(", peerAdmin=");
		builder.append(peerAdmin);
		builder.append(", users=");
		builder.append(users);
		builder.append(", caClient=");
		builder.append(caClient);
		builder.append(", orderers=");
		builder.append(orderers);
		builder.append(", peerChannelNames=");
		builder.append(peers);
		builder.append(", eventHubs=");
		builder.append(eventHubs);
		builder.append("]");
		return builder.toString();
	}

}
