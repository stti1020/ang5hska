package at.oenb.dlt.domain;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class DltOrganisationConfig {

	@Value("${dlt.organisation.name}")
	private String name;
	@Value("${dlt.organisation.msp-id}")
	private String mspId;
	@Value("${dlt.organisation.domain}")
	private String domain;

	@Value("${dlt.organisation.ca.url}")
	private String caUrl;
	@Value("${dlt.organisation.ca.cert.file-path}")
	private ClassPathResource caCertFileResource;

	@Value("${dlt.organisation.peer.name}")
	private String peerName;
	@Value("${dlt.organisation.peer.url}")
	private String peerUrl;
	@Value("${dlt.organisation.peer.cert.file-path}")
	private ClassPathResource peerCertFileResource;

	@Value("${dlt.organisation.peer.event-hub.name}")
	private String peerEventHubName;
	@Value("${dlt.organisation.peer.event-hub.url}")
	private String peerEventHubUrl;

	@Value("${dlt.organisation.peer.admin.username}")
	private String peerAdminName;
	@Value("${dlt.organisation.peer.admin.pk.dir-path}")
	private ClassPathResource peerAdminKeyDirectoryResource;
	@Value("${dlt.organisation.peer.admin.cert.file-path}")
	private ClassPathResource peerAdminCertFileResource;

	@Value("${dlt.organisation.orderer.name}")
	private String ordererName;
	@Value("${dlt.organisation.orderer.url}")
	private String ordererUrl;
	@Value("${dlt.organisation.orderer.cert.file-path}")
	private ClassPathResource ordererCertFileResource;

	public String getName() {
		return name;
	}

	public String getMspId() {
		return mspId;
	}

	public String getDomain() {
		return domain;
	}

	public String getCaUrl() {
		return caUrl;
	}

	public String getAbsoluteCaCertFilePath() {
		try {
			return caCertFileResource.getFile().getAbsolutePath();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public String getPeerName() {
		return peerName;
	}

	public String getPeerUrl() {
		return peerUrl;
	}

	public String getAbsolutePeerCertFilePath() {
		try {
			return peerCertFileResource.getFile().getAbsolutePath();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public String getPeerEventHubName() {
		return peerEventHubName;
	}

	public String getPeerEventHubUrl() {
		return peerEventHubUrl;
	}

	public String getPeerAdminName() {
		return peerAdminName;
	}

	public File getPeerAdminKeyFile() {
		File directory;
		try {
			directory = peerAdminKeyDirectoryResource.getFile();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		File file = null;
		if (directory.exists() && directory.isDirectory()) {
			File[] files = directory.listFiles((d, fileName) -> fileName.endsWith("_sk"));
			if (files != null && files.length == 1) {
				file = files[0];
			}
		}
		if (file == null) {
			throw new IllegalStateException(String.format(
					"Missing exactly one peer admin file with ending sk at path %s.", directory.getAbsolutePath()));
		}
		return file;

	}

	public File getPeerAdminCertFile() {
		try {
			return peerAdminCertFileResource.getFile();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public String getOrdererName() {
		return ordererName;
	}

	public String getOrdererUrl() {
		return ordererUrl;
	}

	public String getAbsoluteOrdererCertFilePath() {
		try {
			return ordererCertFileResource.getFile().getAbsolutePath();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

}
