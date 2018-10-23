package at.oenb.dlt.domain;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

public class OrdererConfig {

    private String name;
    private String url;
    private ClassPathResource certFile;

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public ClassPathResource getCertFile() {
        return certFile;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
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

}