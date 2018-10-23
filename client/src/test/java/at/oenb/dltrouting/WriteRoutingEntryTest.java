package at.oenb.dltrouting;

import at.oenb.dlt.AbstractDltTest;
import at.oenb.dlt.domain.DltConfig;
import at.oenb.dlt.domain.DltOrganisation;
import at.oenb.dlt.domain.DltOrganisationConfig;
import at.oenb.dltrouting.domain.*;
import at.oenb.dltrouting.helper.Formatter;
import org.hyperledger.fabric.sdk.Channel;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;

public class WriteRoutingEntryTest extends AbstractDltTest {

    @Autowired
    private DltRoutingHFClient dltRoutingClient;
    @Autowired
    private DltConfig dltConfig;
    @Autowired
    private DltOrganisationConfig dltOrganisationConfig;

    @Test
    public void test() throws Exception {

        DltOrganisation dltOrganisation = new DltOrganisation(dltConfig, dltOrganisationConfig, dltRoutingClient);
        dltRoutingClient.setUserContext(dltOrganisation.getPeerAdmin());
        Channel dltChannel = dltRoutingClient.createOrReconstructDltRoutingChannel(dltOrganisation);

        BIC bic = new BIC("SOLADES1KNZ");
        RoutingEntry routingEntry = RoutingEntry.builder()
                .bankOperation(BankOperation.SCT)
                .service(Service.SEPA_CT)
                .counterPartyBic(new BIC("MARKDEF0"))
                .validFrom(OffsetDateTime.parse("2012-11-01T22:08:41+00:00", Formatter.DATE_TIME))
                .validTo(OffsetDateTime.parse("2015-11-01T22:08:41+00:00", Formatter.DATE_TIME))
                .cooperationBic(new BIC("MARKDEF0000"))
                .build();

        dltRoutingClient.createRoutingEntry(dltChannel, bic, routingEntry);
        dltRoutingClient.queryBankByBic(dltChannel, bic);
        dltRoutingClient.queryAllBanks(dltChannel);

        RoutingEntry updatedRoutingEntry = RoutingEntry.builder()
                .id("1")
                .bankOperation(BankOperation.SCT)
                .service(Service.SCT_INST)
                .counterPartyBic(new BIC("MARKDEF0"))
                .validFrom(OffsetDateTime.parse("2012-11-01T22:08:41+00:00", Formatter.DATE_TIME))
                .validTo(OffsetDateTime.parse("2015-11-01T22:08:41+00:00", Formatter.DATE_TIME))
                .cooperationBic(new BIC("MARKDEF0000"))
                .build();
        dltRoutingClient.updateRoutingEntry(dltChannel, bic, updatedRoutingEntry);
        dltRoutingClient.queryBankByBic(dltChannel, bic);
        dltRoutingClient.cleanUpRoutingEntries(dltChannel);
        dltRoutingClient.queryBankByBic(dltChannel, bic);
    }
}
