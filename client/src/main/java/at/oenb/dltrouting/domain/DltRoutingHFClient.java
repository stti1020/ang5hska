package at.oenb.dltrouting.domain;

import at.oenb.dlt.domain.DltConfig;
import at.oenb.dlt.domain.DltHFClient;
import at.oenb.dlt.domain.DltOrganisation;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Peer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static at.oenb.dltrouting.domain.DltRoutingConstants.*;

@Component
@RequiredArgsConstructor
public class DltRoutingHFClient extends DltHFClient {

    private final DltConfig dltConfig;
    private final ObjectMapper objectMapper;

    public Channel createOrReconstructDltRoutingChannel(DltOrganisation dltOrganisation) {
        return createOrReconstructChannel(CHANNEL_NAME, dltConfig.getChannelConfigFile(), dltOrganisation);
    }

    public void installAndInstantiateChaincode(Channel channel, Peer peer) {
        installAndInstantiateChaincode(channel, peer, CHAINCODE_ID, dltConfig.getChaincodeDirectory(), dltConfig.getEndorsementPolicyFile());
    }

    public Bank queryBankByBic(Channel channel, BIC bic) {
        String[] args = {bic.getValue()};
        String bankJson = queryByChaincode(channel, CHAINCODE_ID, CHAINCODE_FUNCTION_GET_BANK_BY_KEY, args);
        return jsonToBank(bankJson);
    }

    public List<Bank> queryBankByCooperationBic(Channel channel, BIC bic) {
        String[] args = {bic.getValue()};
        String banksJson = queryByChaincode(channel, CHAINCODE_ID, CHAINCODE_FUNCTION_GET_BANK_BY_COOPERATION_BIC, args);
        return jsonToBanks(banksJson);
    }

    public List<Bank> queryAllBanks(Channel channel) {
        String banksJson = queryByChaincode(channel, CHAINCODE_ID, CHAINCODE_FUNCTION_GET_ALL_BANKS);
        return jsonToBanks(banksJson);
    }

    public TransactionEvent createBank(Channel channel, Bank bank) {
        String[] args = toCreateBankArgs(bank);
        return sendBlocked(channel, CHAINCODE_ID, CHAINCODE_FUNCTION_WRITE_BANK, args);
    }

    public TransactionEvent createRoutingEntry(Channel channel, BIC bic, RoutingEntry routingEntry) {
        String[] args = toCreateRoutingEntryArgs(bic, routingEntry);
        return sendBlocked(channel, CHAINCODE_ID, CHAINCODE_FUNCTION_WRITE_ROUTING_ENTRY, args);
    }

    public TransactionEvent updateRoutingEntry(Channel channel, BIC bic, RoutingEntry routingEntry) {
        String[] args = toUpdateRoutingEntryArgs(bic, routingEntry);
        return sendBlocked(channel, CHAINCODE_ID, CHAINCODE_FUNCTION_UPDATE_ROUTING_ENTRY_BY_ID, args);
    }

    public TransactionEvent cleanUpRoutingEntries(Channel channel) {
        return sendBlocked(channel, CHAINCODE_ID, CHAINCODE_FUNCTION_CLEAN_UP_ROUTING_ENTRIES);
    }

    private Bank jsonToBank(String bankJson) {
        Bank bank = null;
        try {
            bank = objectMapper.readValue(bankJson, Bank.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bank;
    }

    private List<Bank> jsonToBanks(String bankJson) {
        List<Bank> banks = null;
        try {
            WorldState state = objectMapper.readValue(bankJson, WorldState.class);
            banks = state.getBankList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return banks;
    }

    private String[] toCreateBankArgs(Bank bank) {
        return new String[]{toString(bank.getBic(), BIC::getValue), toString(bank.getName())};
    }

    private String[] toCreateRoutingEntryArgs(BIC bic, RoutingEntry routingEntry) {
        return new String[]{toString(bic, BIC::getValue),
                toString(routingEntry.getBankOperation()), toString(routingEntry.getService()),
                toString(routingEntry.getCounterPartyBic()),
                toString(routingEntry.getValidFrom()), toString(routingEntry.getValidTo()),
                toString(routingEntry.getCooperationBic())};
    }

    private String[] toUpdateRoutingEntryArgs(BIC bic, RoutingEntry routingEntry) {
        return new String[]{toString(bic, BIC::getValue), toString(routingEntry.getId()),
                toString(routingEntry.getBankOperation()), toString(routingEntry.getService()),
                toString(routingEntry.getCounterPartyBic()),
                toString(routingEntry.getValidFrom()), toString(routingEntry.getValidTo()),
                toString(routingEntry.getCooperationBic())};
    }

    private String toString(OffsetDateTime dateTime) {
        return toString(dateTime, OffsetDateTime::toString);
    }

    private String toString(Service service) {
        return toString(service, Service::getServiceName);
    }

    private String toString(BankOperation bankOperation) {
        return toString(bankOperation, Enum::toString);
    }

    private String toString(BIC bic) {
        return toString(bic, BIC::getValue);
    }

    private String toString(String value) {
        return Optional.ofNullable(value).orElse("");
    }

    private <T> String toString(T value, Function<T, String> valueExtractor) {
        return Optional.ofNullable(value)
                .map(valueExtractor)
                .orElse("");
    }

}
