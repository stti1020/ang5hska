package at.oenb.dltrouting.domain;

import org.hyperledger.fabric.sdk.ChaincodeID;

public class DltRoutingConstants {

    public static final String CHANNEL_NAME = "dltroutingchannel";

    public static final String CHAINCODE_NAME = "dltrouting_chaincode_go";
    public static final String CHAINCODE_PATH = "chaincode/dltrouting";
    public static final String CHAINCODE_VERSION = "1";
    public static final ChaincodeID CHAINCODE_ID =
            ChaincodeID.newBuilder()
                    .setName(CHAINCODE_NAME)
                    .setVersion(CHAINCODE_VERSION)
                    .setPath(CHAINCODE_PATH)
                    .build();


    public static final String CHAINCODE_FUNCTION_GET_BANK_BY_KEY = "getBankByKey";
    public static final String CHAINCODE_FUNCTION_GET_BANK_BY_COOPERATION_BIC = "getBankByCooperationBic";
    public static final String CHAINCODE_FUNCTION_GET_ALL_BANKS = "getAllBanks";
    public static final String CHAINCODE_FUNCTION_WRITE_BANK = "writeBank";
    public static final String CHAINCODE_FUNCTION_WRITE_ROUTING_ENTRY = "writeRoutingEntry";
    public static final String CHAINCODE_FUNCTION_UPDATE_ROUTING_ENTRY_BY_ID = "updateRoutingEntryById";
    public static final String CHAINCODE_FUNCTION_CLEAN_UP_ROUTING_ENTRIES = "cleanUpRoutingEntries";

}
