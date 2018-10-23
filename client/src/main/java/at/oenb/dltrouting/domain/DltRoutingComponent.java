package at.oenb.dltrouting.domain;

import at.oenb.dlt.domain.DltConfig;
import at.oenb.dlt.domain.DltOrganisation;
import at.oenb.dlt.domain.DltOrganisationConfig;
import org.hyperledger.fabric.sdk.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class DltRoutingComponent {

    private final DltConfig dltConfig;
    private final DltOrganisationConfig dltOrganisationConfig;
    private final DltRoutingHFClient dltRoutingClient;

    private DltOrganisation dltOrganisation;
    private Channel dltChannel;

    @Autowired
    public DltRoutingComponent(DltConfig dltConfig, DltOrganisationConfig dltOrganisationConfig, DltRoutingHFClient dltRoutingClient) {
        this.dltConfig = dltConfig;
        this.dltOrganisationConfig = dltOrganisationConfig;
        this.dltRoutingClient = dltRoutingClient;
    }

    @PostConstruct
    private void init() {
        dltOrganisation = new DltOrganisation(dltConfig, dltOrganisationConfig, dltRoutingClient);
        dltRoutingClient.setUserContext(dltOrganisation.getPeerAdmin());
        dltRoutingClient.setVerbose(true);
        dltChannel = dltRoutingClient.createOrReconstructDltRoutingChannel(dltOrganisation);
        dltRoutingClient.installAndInstantiateChaincode(dltChannel, dltOrganisation.getPeerOfOrg());
    }

    public void createBank(Bank bank) {
        // TODO user
        dltRoutingClient.createBank(dltChannel, bank);
    }

    public Bank getBank(BIC bic) {
        // TODO user
        // wie lösen wir das zusammenspiel
        // von demjenigen der den rest service aufruft und dem Zertifikat, das ihm
        // zugeordnet ist

        return dltRoutingClient.queryBankByBic(dltChannel, bic);
    }
    
    public List<Bank> getBankByCooperationBic(BIC bic) {

        return dltRoutingClient.queryBankByCooperationBic(dltChannel, bic);
    }

    public List<Bank> getAllBanks() {
        // TODO user
        return dltRoutingClient.queryAllBanks(dltChannel);
    }

    public void cleanUpRoutingEntries() {
        // TODO user
        dltRoutingClient.cleanUpRoutingEntries(dltChannel);
    }

    public void createRoutingEntry(BIC bic, RoutingEntry routingEntry) {
        // TODO user
        dltRoutingClient.createRoutingEntry(dltChannel, bic, routingEntry);
    }

    public void updateRoutingEntry(BIC bic, RoutingEntry routingEntry) {
        // TODO user
        dltRoutingClient.updateRoutingEntry(dltChannel, bic, routingEntry);
    }
}
