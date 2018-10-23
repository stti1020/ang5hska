package at.oenb.dltrouting;

import at.oenb.dlt.AbstractDltTest;
import at.oenb.dlt.domain.DltConfig;
import at.oenb.dlt.domain.DltOrganisation;
import at.oenb.dlt.domain.DltOrganisationConfig;
import at.oenb.dltrouting.domain.BIC;
import at.oenb.dltrouting.domain.Bank;
import at.oenb.dltrouting.domain.DltRoutingHFClient;
import org.hyperledger.fabric.sdk.Channel;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


public class PingTest extends AbstractDltTest {

    @Autowired
    private DltRoutingHFClient dltRoutingClient;
    @Autowired
    private DltConfig dltConfig;
    @Autowired
    private DltOrganisationConfig dltOrganisationConfig;

    private Channel dltChannel;

    @Test
    public void ping() {
        initChannelAndChaincode();
        try {
            Bank bank = dltRoutingClient.queryBankByBic(dltChannel, new BIC("SOLADES1KNZ"));
            assertNotNull(bank);
        } catch (Exception e) {
            if (!e.getMessage().contains("Bank does not exist")) {
                e.printStackTrace();
                fail();
            }
        }
    }

    private void initChannelAndChaincode() {
        DltOrganisation dltOrganisation = new DltOrganisation(dltConfig, dltOrganisationConfig, dltRoutingClient);
        dltRoutingClient.setUserContext(dltOrganisation.getPeerAdmin());
        dltChannel = dltRoutingClient.createOrReconstructDltRoutingChannel(dltOrganisation);
        dltRoutingClient.installAndInstantiateChaincode(dltChannel, dltOrganisation.getPeerOfOrg());
    }

}
