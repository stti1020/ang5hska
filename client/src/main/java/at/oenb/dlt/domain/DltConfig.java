package at.oenb.dlt.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class DltConfig {

    @Value("${dlt.admin.username}")
    private String adminUsername;
    @Value("${dlt.admin.password}")
    private String adminPassword;

    @Value("${dlt.domain}")
    private String domain;

    @Value("${dlt.channel-config.file-path}")
    private ClassPathResource channelConfigFileResource;

    @Value("${dlt.chaincode.dir-path}")
    private ClassPathResource chaincodeDirPath;

    @Value("${dlt.endorsementpolicy.file-path}")
    private ClassPathResource endorsementPolicyFilePath;

    @Autowired
    private DltNetworkConfig dltNetworkConfig;

    public String getAdminUsername() {
        return adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public String getDomain() {
        return domain;
    }

    public File getChannelConfigFile() {
        try {
            return channelConfigFileResource.getFile();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public File getChaincodeDirectory() {
        try {
            return chaincodeDirPath.getFile().getParentFile();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public File getEndorsementPolicyFile() {
        try {
            return endorsementPolicyFilePath.getFile();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public List<OrdererConfig> getOrdererConfigs() {
        return dltNetworkConfig.getOrderers();
    }

    public List<PeerConfig> getPeerConfigs() {
        return dltNetworkConfig.getPeers();
    }
}
