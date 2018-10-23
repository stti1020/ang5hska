package at.oenb.dlt.domain;

import java.io.Serializable;
import java.security.PrivateKey;

import org.hyperledger.fabric.sdk.Enrollment;

public class UserEnrollment implements Enrollment, Serializable {

	private static final long serialVersionUID = 1;
	private final PrivateKey privateKey;
	private final String certificate;

	public UserEnrollment(PrivateKey privateKey, String certificate) {
		this.certificate = certificate;
		this.privateKey = privateKey;
	}

	@Override
	public String getCert() {
		return certificate;
	}

	@Override
	public PrivateKey getKey() {
		return privateKey;
	}
}