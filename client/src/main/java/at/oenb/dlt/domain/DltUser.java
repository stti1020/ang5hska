package at.oenb.dlt.domain;

import java.io.Serializable;
import java.util.Set;

import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

public class DltUser implements User, Serializable {
	public String getOrganization() {
		return organization;
	}

	private static final long serialVersionUID = 8077132186383604355L;

	private final String name;
	private final String organization;

	private Set<String> roles;
	private String account;
	private String affiliation;
	private String enrollmentSecret;

	private String mspId;
	private Enrollment enrollment;

	public DltUser(String name, String organization) {
		this.name = name;
		this.organization = organization;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Set<String> getRoles() {
		return this.roles;
	}

	public void setRoles(Set<String> roles) {

		this.roles = roles;
	}

	@Override
	public String getAccount() {
		return this.account;
	}

	/**
	 * Set the account.
	 *
	 * @param account
	 *            The account.
	 */
	public void setAccount(String account) {

		this.account = account;
	}

	@Override
	public String getAffiliation() {
		return this.affiliation;
	}

	/**
	 * Set the affiliation.
	 *
	 * @param affiliation
	 *            the affiliation.
	 */
	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}

	@Override
	public Enrollment getEnrollment() {
		return this.enrollment;
	}

	/**
	 * Determine if this name has been registered.
	 *
	 * @return {@code true} if registered; otherwise {@code false}.
	 */
	public boolean isRegistered() {
		return enrollmentSecret != null;
	}

	/**
	 * Determine if this name has been enrolled.
	 *
	 * @return {@code true} if enrolled; otherwise {@code false}.
	 */
	public boolean isEnrolled() {
		return this.enrollment != null;
	}

	public String getEnrollmentSecret() {
		return enrollmentSecret;
	}

	public void setEnrollmentSecret(String enrollmentSecret) {
		this.enrollmentSecret = enrollmentSecret;
	}

	public void setEnrollment(Enrollment enrollment) {

		this.enrollment = enrollment;

	}

	public static String toKeyValStoreName(String name, String org) {
		return "user." + name + org;
	}

	@Override
	public String getMspId() {
		return mspId;
	}

	public void setMspId(String mspID) {
		this.mspId = mspID;

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DltUser [name=");
		builder.append(name);
		builder.append(", roles=");
		builder.append(roles);
		builder.append(", account=");
		builder.append(account);
		builder.append(", affiliation=");
		builder.append(affiliation);
		builder.append(", organization=");
		builder.append(organization);
		builder.append(", enrollmentSecret=");
		builder.append(enrollmentSecret);
		builder.append(", mspId=");
		builder.append(mspId);
		builder.append(", enrollment=");
		builder.append(enrollment);
		builder.append("]");
		return builder.toString();
	}

}
