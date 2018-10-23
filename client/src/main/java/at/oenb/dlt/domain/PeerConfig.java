package at.oenb.dlt.domain;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;

public class PeerConfig {

    private String name;
    private String url;
    private ClassPathResource certFile;
    private String eventHubName;
    private String eventHubUrl;

	public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    public ClassPathResource getCertFile() {
		return certFile;
	}

	public void setCertFile(ClassPathResource certFile) {
		this.certFile = certFile;
	}
    
    public String getAbsoluteCertFilePath() {
        try {
            return certFile.getFile().getAbsolutePath();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

	public String getEventHubName() {
		return eventHubName;
	}

	public void setEventHubName(String eventHubName) {
		this.eventHubName = eventHubName;
	}

	public String getEventHubUrl() {
		return eventHubUrl;
	}

	public void setEventHubUrl(String eventHubUrl) {
		this.eventHubUrl = eventHubUrl;
	}
}