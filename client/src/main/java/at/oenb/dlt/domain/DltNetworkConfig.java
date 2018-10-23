package at.oenb.dlt.domain;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties("dlt.network")
public class DltNetworkConfig {

    private List<PeerConfig> peers;
    private List<OrdererConfig> orderers;

    public void setPeers(List<PeerConfig> peers) {
        this.peers = peers;
    }

    public void setOrderers(List<OrdererConfig> orderers) {
        this.orderers = orderers;
    }

    public List<PeerConfig> getPeers() {
        return peers;
    }

    public List<OrdererConfig> getOrderers() {
        return orderers;
    }

}
