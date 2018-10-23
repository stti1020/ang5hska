package main

import (
	"encoding/json"
	"fmt"
	//"strconv" 
	"time"
	"regexp"
	
	"io"
	"crypto/rand"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
)

type SimpleChaincode struct {
}

type RoutingEntry struct {
	Id string `json:"id"`
	BankOperation string `json:"bankOperation"` 
	Service string `json:"service"` 
	CounterPartyBic string `json:"counterPartyBic"` 
	ValidFrom string `json:"validFrom"` 
	ValidTo string `json:"validTo,omitempty"`
	CooperationBic string `json:"cooperationBic"` 
}

type Bank struct{
	Bic string `json:"bic"` 
	Name string `json:"name"` 
	RoutingEntryList []*RoutingEntry `json:"routingEntryList"` 
}

type BankenListe struct{
	BankList []*Bank `json:"bankList"`
}

type Test struct{
	Id string `json:"id"`
}

// ===================================================================================
// Main
// ===================================================================================
// TODO shim hat eine GetCreator methode, kann diese vielleicht für unsere "Endorsing Policy" Überprüfungen genutzt werden?
func main() {
	err := shim.Start(new(SimpleChaincode))
	if err != nil {
		fmt.Printf("Error starting Simple chaincode: %s", err)
	}
}

// Init initializes chaincode
// ===========================
func (t *SimpleChaincode) Init(stub shim.ChaincodeStubInterface) pb.Response {
	return shim.Success(nil)
}

// Invoke - Our entry point for Invocations
// ========================================
func (t *SimpleChaincode) Invoke(stub shim.ChaincodeStubInterface) pb.Response {
	function, args := stub.GetFunctionAndParameters()
	
	if function == "writeBank" {
		return t.writeBank(stub, args)
	} else if function == "getAllBanks" {
		return t.getAllBanks(stub, args)
	} else if function == "getBankByKey" {
		return t.getBankByKey(stub, args)
	} else if function == "getBankByCooperationBic" {
		return t.getBankByCooperationBic(stub, args)
	} else if function == "cleanUpRoutingEntries" {
		return t.cleanUpRoutingEntries(stub, args)
	} else if function == "writeRoutingEntry" {
		return t.writeRoutingEntry(stub, args)
	} else if function == "updateRoutingEntryById" {
		return t.updateRoutingEntryById(stub, args)
	}	else if function == "test" {
		return t.test(stub, args)
	}

	return shim.Error("Received unknown function invocation")
}

// ============================================================
// Shell-Befehl: peer chaincode query -C dltroutingchannel -n dltrouting -c'{"Args":["test"]}'
// ============================================================
func (t *SimpleChaincode) test(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	var test string 
	test = "Test"
	
	return shim.Success([]byte(test))
}




// ============================================================
// writeBank - Schreiben einer Bank 
// Shell-Befehl: peer chaincode invoke -o host.orgaustria.dltrouting.com:7050 --tls $CORE_PEER_TLS_ENABLED --cafile $ORDERER_CA_FILE -C dltroutingchannel -n dltrouting -v 1.0 -c '{"Args":["writeBank", "SOLADES1KNZ", "Sparkasse Bodensee"]}'
// ============================================================
func (t *SimpleChaincode) writeBank(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	
	if len(args) != 2 {
		return shim.Error("Incorrect number of arguments. Expecting 8 attributes")
	}

	bic := args[0]
	bankName := args[1]

	bicValidationResult := validateBic(bic)
	if !bicValidationResult{
		return shim.Error("BIC is invalid.")
	}

	bankAsBytes, errBankAsBytes := stub.GetState(bic)
	if errBankAsBytes != nil {
		return shim.Error(errBankAsBytes.Error())
	} else if bankAsBytes != nil {
		return shim.Error("Bank with BIC " + bic + " already exists.")
	}

	var routingEntryList []*RoutingEntry

	bank := &Bank {	Bic: bic, Name: bankName, RoutingEntryList: routingEntryList}

	bankJSONasBytes, errorJson := json.Marshal(bank)
	if errorJson != nil {
		return shim.Error(errorJson.Error())
	}

	err := stub.PutState(bic, bankJSONasBytes)
	if err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success(nil)
}

// ============================================================
// getAllBanks - Lesen aller Bank / vollständiger Inhalt der WorldState
// Shell-Befehl: peer chaincode query -C dltroutingchannel -n dltrouting -c'{"Args":["getAllBanks"]}'
// ============================================================
func (t *SimpleChaincode) getAllBanks(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	var err error

	if len(args) != 0 {
		return shim.Error("Incorrect number of arguments. Expecting name of the BIC to query")
	}
 
	keysIter, err := stub.GetStateByRange("", "")
	if err != nil {
		return shim.Error(fmt.Sprintf("keys operation failed. Error accessing state: %s", err))
	}
	defer keysIter.Close()

	
	var banks []*Bank
	
	for keysIter.HasNext() {
		response, iterErr := keysIter.Next()
		
		if iterErr != nil {
			return shim.Error(fmt.Sprintf("keys operation failed. Error accessing state: %s", err))
		}
		
		var bank Bank
		errBankStruct := json.Unmarshal(response.Value, &bank)
		if errBankStruct != nil {
			return shim.Error(errBankStruct.Error())
		}
		
		banks = append(banks, &bank)
	}
	
	bankenListe := &BankenListe {BankList: banks}
	bankenListeJSON, errorJson := json.Marshal(bankenListe)
	if errorJson != nil {
		return shim.Error(errorJson.Error())
	}

	return shim.Success([]byte(bankenListeJSON))
}

// ============================================================
// getRoutingEntryByKey - Lesen einer Bank nach Key (BIC)
// Shell-Befehl: peer chaincode query -C dltroutingchannel -n dltrouting -c'{"Args":["getBankByKey","SOLADES1KNZ"]}'
// ============================================================
func (t *SimpleChaincode) getBankByKey(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting name of the BIC to query")
	}
	
	bicValidationResult := validateBic(args[0])
	if !bicValidationResult{
		return shim.Error("BIC is invalid.")
	}

	bic := args[0]
	bankAsBytes, errBankAsBytes := stub.GetState(bic)
	if errBankAsBytes != nil {
		return shim.Error(errBankAsBytes.Error())
	} else if bankAsBytes == nil {
		return shim.Error("Bank does not exist with BIC " + bic)
	}

	return shim.Success(bankAsBytes)
}

// ============================================================
// getRoutingEntryByKey - Lesen einer Bank nach Key (BIC)
// Shell-Befehl: peer chaincode query -C dltroutingchannel -n dltrouting -c'{"Args":["getBankByCooperationBic","MARKDEF0000"]}'
// ============================================================
func (t *SimpleChaincode) getBankByCooperationBic(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	var cooperationBic, jsonResp string
	var err error

	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting name of the Cooperation BIC to query")
	}
	
	bicValidationResult := validateBic(args[0])
	if !bicValidationResult{
		return shim.Error("Cooperation BIC is invalid.")
	}

	cooperationBic = args[0]
	
	keysIter, err := stub.GetStateByRange("", "")
	if err != nil {
		return shim.Error(fmt.Sprintf("keys operation failed. Error accessing state: %s", err))
	}
	defer keysIter.Close()

	
	var banks []*Bank
	
	for keysIter.HasNext() {
		response, iterErr := keysIter.Next()
		
		if iterErr != nil {
			return shim.Error(fmt.Sprintf("keys operation failed. Error accessing state: %s", err))
		}
		
		var bank Bank
		errBankStruct := json.Unmarshal(response.Value, &bank)
		if errBankStruct != nil {
			return shim.Error(errBankStruct.Error())
		}
		
		if bank.RoutingEntryList != nil {
			var newBank Bank
			newBank.Bic = bank.Bic
			newBank.Name = bank.Name
			
			for _, routingEntry := range bank.RoutingEntryList {
				if routingEntry.CooperationBic == cooperationBic {
					newBank.RoutingEntryList = append(newBank.RoutingEntryList, routingEntry)
				}
			}
			if newBank.RoutingEntryList != nil {
				banks = append(banks, &newBank)
			}
		}
	}
	
	if banks == nil {
		jsonResp = "No entries with Cooperation Bic: " + cooperationBic + " found!"
		return shim.Error(jsonResp)
	}
	
	bankenListe := &BankenListe {BankList: banks}
	bankenListeJSON, errorJson := json.Marshal(bankenListe)
	if errorJson != nil {
		return shim.Error(errorJson.Error())
	}

	return shim.Success([]byte(bankenListeJSON))
}

// ============================================================
// writeRoutingEntry - Erstellen eines Routing Entry
// Shell-Befehl: peer chaincode invoke -o host.orgaustria.dltrouting.com:7050 --tls $CORE_PEER_TLS_ENABLED --cafile $ORDERER_CA_FILE -C dltroutingchannel -n dltrouting -v 1.0 -c '{"Args":["writeRoutingEntry", "SOLADES1KNZ", "SCT", "SEPA CT", "MARKDEF0", "2012-11-01T22:08:41+00:00", "2015-11-01T22:08:41+00:00", "MARKDEF0000"]}'
// ============================================================
func (t *SimpleChaincode) writeRoutingEntry(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	
	if len(args) != 7 {
		return shim.Error("Incorrect number of arguments. Expecting 7 attributes")
	}

	bic := args[0]
	bankOperation := args[1]
	service := args[2]
	counterPartyBic := args[3]
	validFrom := args[4]
	validTo := args[5]
	cooperationBic := args[6]
	
	if counterPartyBic == "" {
		counterPartyBic = bic
	}
	
	validationResult := validateRoutingEntry(bic, bankOperation, service, counterPartyBic, validFrom, validTo, cooperationBic)
	if validationResult != ""{
		return shim.Error(validationResult)
	}

	bankAsBytes, errBankAsBytes := stub.GetState(bic)
	if errBankAsBytes != nil {
		return shim.Error(errBankAsBytes.Error())
	} else if bankAsBytes == nil {
		return shim.Error("Bank does not exist with BIC " + bic)
	}

	var bank Bank
	errBankStruct := json.Unmarshal(bankAsBytes, &bank)
	if errBankStruct != nil {
		return shim.Error(errBankStruct.Error())
		
	}

	newRoutingEntryId, errGenerateUUID := newUUID()
	if errGenerateUUID != nil {
		return shim.Error(errGenerateUUID.Error())
	}
	
	routingEntry := &RoutingEntry {Id: newRoutingEntryId ,BankOperation: bankOperation,Service: service,CounterPartyBic: counterPartyBic,ValidFrom: validFrom,ValidTo: validTo,CooperationBic: cooperationBic}
	
	duplicationValidationResult := validateRoutingEntryForDuplication(routingEntry, bank)
	if duplicationValidationResult != ""{
		return shim.Error(duplicationValidationResult)
	}
	
	bank.RoutingEntryList = append(bank.RoutingEntryList, routingEntry)
	
		
	bankJSONasBytes, errorJson := json.Marshal(bank)
	if errorJson != nil {
		return shim.Error(errorJson.Error())
	}
	
	err := stub.PutState(bic, bankJSONasBytes) 
	if err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success(nil)
}

// ============================================================
// updateRoutingEntryById - aktualisieren eines Routing Entry nach Key (BIC) und RoutingEntry Id 
// Parameter: Args":["updateRoutingEntryById","SOLADES1KNZ","1"]}' //TODO parameter beschreiben
// Shell-Befehl: peer chaincode invoke -o host.orgaustria.dltrouting.com:7050 --tls $CORE_PEER_TLS_ENABLED --cafile $ORDERER_CA_FILE -C dltroutingchannel -n dltrouting -v 1.0 -c '{"Args":["updateRoutingEntryById","SOLADES1KNZ","UUID", "SCT", "SEPA CT", "MARKDEF0", "2012-11-01T22:08:41+00:00", "2015-11-01T22:08:41+00:00", "MARKDEF0000"]}'
// ============================================================
func (t *SimpleChaincode) updateRoutingEntryById(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	
	if len(args) != 8 {
		return shim.Error("Incorrect number of arguments. Expecting 8 attributes")
	}
	
	bic := args[0]
	id := args[1]
	bankOperation := args[2]
	service := args[3]
	counterPartyBic := args[4]
	validFrom := args[5]
	validTo := args[6]
	cooperationBic := args[7]
	
	if counterPartyBic == "" {
		counterPartyBic = bic
	}
	
	validationResult := validateRoutingEntry(bic, bankOperation, service, counterPartyBic, validFrom, validTo, cooperationBic)
	if validationResult != ""{
		return shim.Error(validationResult)
	}

	bankAsBytes, errBankAsBytes := stub.GetState(bic)
	if errBankAsBytes != nil {
		return shim.Error(errBankAsBytes.Error())
	} else if bankAsBytes == nil {
		return shim.Error("Bank does not exist with BIC " + bic)
	}

	var bank Bank
	errBankStruct := json.Unmarshal(bankAsBytes, &bank)
	if errBankStruct != nil {
		return shim.Error(errBankStruct.Error())
		
	}
	
	if bank.RoutingEntryList == nil {
		return shim.Error("The RoutingEntryList for the Key/BIC: " + bic + " is empty")
	} 
	
	routingEntry := &RoutingEntry {Id: id ,BankOperation: bankOperation,Service: service,CounterPartyBic: counterPartyBic,ValidFrom: validFrom,ValidTo: validTo,CooperationBic: cooperationBic}
	entryExists := false
	
	for index := 0; index < len(bank.RoutingEntryList); index++ {
		if bank.RoutingEntryList[index].Id == id {
			bank.RoutingEntryList[index] = routingEntry
			entryExists = true
			break
		}
	}
	if !entryExists {
		return shim.Error("The RoutingEntry with the Id: " + id + " does not exist")
	}
		
	bankJSONasBytes, errorJson := json.Marshal(bank)
	if errorJson != nil {
		return shim.Error(errorJson.Error())
	}
	
	err := stub.PutState(bic, bankJSONasBytes) //rewrite the marble
	if err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success(nil)
}

// ============================================================
// cleanUpRoutingEntries - alle ungültigen RoutingEntries entfernen
// Shell-Befehl: peer chaincode invoke -o host.orgaustria.dltrouting.com:7050 --tls $CORE_PEER_TLS_ENABLED --cafile $ORDERER_CA_FILE -C dltroutingchannel -n dltrouting -v 1.0 -c'{"Args":["cleanUpRoutingEntries"]}'
// ============================================================
func (t *SimpleChaincode) cleanUpRoutingEntries (stub shim.ChaincodeStubInterface, args []string) pb.Response {
	/*currentDate*/dateLimit := time.Now() //TODO nochmal prüfen warum wir uns für AddDate entschieden haben
	//dateLimit := currentDate.AddDate(0,0,1) // currentDate + 1 day
 
	keysIter, err := stub.GetStateByRange("", "")
	if err != nil {
		return shim.Error(fmt.Sprintf("keys operation failed. Error accessing state: %s", err))
	}
	defer keysIter.Close()
	
	for keysIter.HasNext() {
		response, iterErr := keysIter.Next()
		
		if iterErr != nil {
			return shim.Error(fmt.Sprintf("keys operation failed. Error accessing state: %s", err))
		}
		

		var currentBank Bank
		errBankStruct := json.Unmarshal(response.Value, &currentBank)
		if errBankStruct != nil {
			return shim.Error(errBankStruct.Error())
		}
		
		var newRoutingEntryList []*RoutingEntry
		hasChanges := false
		
		for index := 0; index < len(currentBank.RoutingEntryList); index++ {

			validToString := currentBank.RoutingEntryList[index].ValidTo
			if validToString != "" {
				validToTime, err := time.Parse(time.RFC3339, validToString)

				if err != nil {
					return shim.Error(err.Error())
				}

				//if valid then add to new RoutingEntryList
				if !validToTime.Before(dateLimit) {
					newRoutingEntryList = append(newRoutingEntryList, currentBank.RoutingEntryList[index])
				}else{
					hasChanges = true
				}
			}
		}
		
		if(hasChanges){
			currentBank.RoutingEntryList = newRoutingEntryList
		
			bankJSON, errorJson := json.Marshal(currentBank)
			if errorJson != nil {
				return shim.Error(errorJson.Error())
			}
		
			err := stub.PutState(response.Key, bankJSON) 
			if err != nil {
				return shim.Error(err.Error())
			}
		}
	}
	
	return shim.Success(nil)
}


// Hilfsmethoden: ------------------------------------------------------------------------------------------------------------

func validateRoutingEntry(bic, bankOperation, service, counterPartyBic, validFrom, validTo, cooperationBic string) string{
	errorMessage := ""
	
	bicValidationResult := validateBic(bic)
	if !bicValidationResult{
		errorMessage += "BIC is invalid. "
	}
	
	bankOperationValidationResult := validateBankOperation(bankOperation)
	if !bankOperationValidationResult{
		errorMessage += "Bank operation is invalid. "
	} else {
		serviceValidationResult := validateService(bankOperation, service)
		if !serviceValidationResult{
			errorMessage += "Service is invalid. "
		}
	}
	
	counterPartyBicValidationResult := validateBic(counterPartyBic)
	if !counterPartyBicValidationResult{
		errorMessage += "Counter party BIC is invalid. "
	}
	
	validFromValidationResult := validateDate(validFrom)
	if !validFromValidationResult{
		errorMessage += "ValidFrom is invalid. "
	} 
	
	if validTo != "" {
	    validToValidationResult := validateDate(validTo)
		if !validToValidationResult{
			errorMessage += "ValidTo is invalid. "
		} 
		
		if validFromValidationResult && validToValidationResult{
		    validFromValidToValidationResult := validateValidFromAndValidTo(validFrom, validTo)
			if !validFromValidToValidationResult{
				errorMessage += "Combination of valideFrom and valideTo is invalid. "
			}
		}
	}
	
	cooperationBicValidationResult := validateBic(cooperationBic)
	if !cooperationBicValidationResult{
		errorMessage += "Cooperation BIC is invalid. "
	}
	
	return errorMessage
}

// Validierungen: Ob genau gleicher Eintrag vorhanden ist; Ob Eintrag mit überschneidendem Zeitraum vorhanden ist 
func validateRoutingEntryForDuplication (routingEntry *RoutingEntry, bank Bank) string {
	errorMessage := ""
	
	for _, entry := range bank.RoutingEntryList {
		// Prüfe Ob Eintrag schon voranden
		if entry.BankOperation == routingEntry.BankOperation && entry.Service == routingEntry.Service && entry.CounterPartyBic == routingEntry.CounterPartyBic && entry.CooperationBic == routingEntry.CooperationBic {
			if entry.ValidFrom == routingEntry.ValidFrom {
				if entry.ValidTo == routingEntry.ValidTo {
					errorMessage += "Duplication detected."
					return errorMessage
				}	
			} else if entry.ValidTo > routingEntry.ValidFrom{
				// Prüfe Ob Eintrag sich mit einem anderen überschneidet
				//TODO: Prüfen wie genau die Klausel mit der Überschneidung aussehen muss
				errorMessage += "Period is invalid."
				return errorMessage
			}		
		}
	}
	return errorMessage
}

func validateBic(bic string) bool {
	valid := false
	
	regExpAlphanumeric, errAn := regexp.Compile("^[A-Za-z0-9+]*$")
	if errAn != nil {
		return !valid
	}
	
	regExpAlphabet, errAb := regexp.Compile("^[A-Za-z+]*$")
	if errAb != nil {
		return !valid
	}
	
	var businessPartyPrefix, countryCode, businessPartySuffix, branchIdentifier string
	if len(bic) == 8 || len(bic) == 11 {
		businessPartyPrefix = string(bic[0]) + string(bic[1]) + string(bic[2]) + string(bic[3])
		countryCode = string(bic[4]) + string(bic[5])
		businessPartySuffix = string(bic[6]) + string(bic[7])
		
		valid = true
		if !regExpAlphanumeric.MatchString(businessPartyPrefix) {
			valid = false
		}
		if !regExpAlphabet.MatchString(countryCode) {
			valid = false
		}
		if !regExpAlphanumeric.MatchString(businessPartySuffix) {
			valid = false
		}
		
		if(len(bic) == 11 ){
			branchIdentifier = string(bic[8]) + string(bic[9]) + string(bic[10])
			if !regExpAlphanumeric.MatchString(branchIdentifier) {
				valid = false
			}
		}
	}
	
    return valid
}

func validateBankOperation(bankOperation string) bool {
	valid := false
	
	if bankOperation == "SDD" || bankOperation == "SCT"{
		valid = true
	}
	
    return valid
}

func validateService(bankOperation, service string) bool {
	valid := false
	
	if bankOperation == "SDD"{
		if service == "SDD Core" || service == "SDD B2B"{
			valid = true
		}
	}
	
	if bankOperation == "SCT"{
		if service == "SEPA CT" || service == "SCT Inst"{
			valid = true
		}
	}
	
    return valid
}

func validateDate (dateString string) bool{
	valid := false
	
	_ , err := time.Parse(time.RFC3339, dateString)
			
	if err == nil {
	    valid = true
	}
	
	return valid
}

func validateValidFromAndValidTo (validFrom, validTo string) bool {
	validFromParsed, validFromErr := time.Parse(time.RFC3339, validFrom)
	if validFromErr != nil {
	    return false
	}
	
	validToParsed, validToErr := time.Parse(time.RFC3339, validTo)
	if validToErr != nil {
	    return false
	}
		
    if validToParsed.Before(validFromParsed){
	    return false
    }
	
	return true
}


// newUUID generates a random UUID according to RFC 4122
func newUUID() (string, error) {
	uuid := make([]byte, 16)
	n, err := io.ReadFull(rand.Reader, uuid)
	if n != len(uuid) || err != nil {
		return "", err
	}
	// variant bits; see section 4.1.1
	uuid[8] = uuid[8]&^0xc0 | 0x80
	// version 4 (pseudo-random); see section 4.1.3
	uuid[6] = uuid[6]&^0xf0 | 0x40
	return fmt.Sprintf("%x-%x-%x-%x-%x", uuid[0:4], uuid[4:6], uuid[6:8], uuid[8:10], uuid[10:]), nil
}

