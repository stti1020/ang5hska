package at.oenb.dltrouting.service;

import at.oenb.dltrouting.domain.BIC;
import at.oenb.dltrouting.domain.Bank;
import at.oenb.dltrouting.domain.DltRoutingComponent;
import at.oenb.dltrouting.domain.RoutingEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping(value = ServiceUrls.DLT_ROUTING_URL)
class DltRoutingRestService {

    private final DltRoutingComponent dltRoutingComponent;

    @Autowired
    public DltRoutingRestService(DltRoutingComponent dltRoutingComponent) {
        this.dltRoutingComponent = dltRoutingComponent;
    }

	@CrossOrigin(origins = "http://localhost:4200")
    @PostMapping(value = "bank")
    @ResponseBody
    ResponseEntity<?> createBank(@RequestBody Bank bank) throws InterruptedException, ExecutionException, TimeoutException {
        dltRoutingComponent.createBank(bank);
        return new ResponseEntity<Bank>(HttpStatus.CREATED);
    }

	@CrossOrigin(origins = "http://localhost:4200")
    @GetMapping(value = "bank/{bic}")
    @ResponseBody
    ResponseEntity<?> getBank(@PathVariable("bic") String bicValue) throws JsonParseException, JsonMappingException, IOException {
        Bank bank = dltRoutingComponent.getBank(new BIC(bicValue));
        if (bank == null) {
        	return new ResponseEntity<Bank>(HttpStatus.NOT_FOUND);
        }
    	return new ResponseEntity<Bank>(bank, HttpStatus.OK);
    }
    
	@CrossOrigin(origins = "http://localhost:4200")
    @GetMapping(value = "bank/cooperationBic/{bic}")
    @ResponseBody
    ResponseEntity<?> getBankByCooperationBic(@PathVariable("bic") String bicValue) throws JsonParseException, JsonMappingException, IOException {
        List<Bank> banks = dltRoutingComponent.getBankByCooperationBic(new BIC(bicValue));
        if (banks == null) {
        	return new ResponseEntity<List<Bank>>(HttpStatus.NOT_FOUND);
        }
    	return new ResponseEntity<List<Bank>>(banks, HttpStatus.OK);
    }

	@CrossOrigin(origins = "http://localhost:4200")
    @GetMapping(value = "banks")
    @ResponseBody
	ResponseEntity<?> getAllBanks() throws JsonParseException, JsonMappingException, IOException {
    	List<Bank> banks = dltRoutingComponent.getAllBanks();
    	if (banks == null) {
    		return new ResponseEntity<List<Bank>>(HttpStatus.NOT_FOUND);
    	}
        return new ResponseEntity<List<Bank>>(banks, HttpStatus.OK);
    }

	@CrossOrigin(origins = "http://localhost:4200")
    @PostMapping(value = "bank/{bic}/routing-entry")
    @ResponseBody
    ResponseEntity<?> createRoutingEntry(@PathVariable("bic") String bicValue, @RequestBody RoutingEntry routingEntry) throws InterruptedException, ExecutionException, TimeoutException {
    	dltRoutingComponent.createRoutingEntry(new BIC(bicValue), routingEntry);
        return new ResponseEntity<RoutingEntry>(HttpStatus.CREATED);
    }

	@CrossOrigin(origins = "http://localhost:4200")
    @PutMapping(value = "bank/{bic}/routing-entry/{routingEntryId}")
    @ResponseBody
    void updateRoutingEntry(@PathVariable("bic") String bicValue, @PathVariable("routingEntryId") String routingEntryId, @RequestBody RoutingEntry routingEntry) throws InterruptedException, ExecutionException, TimeoutException {
        //TODO id necessary? does it work with id? mit chaincode team abklären
        dltRoutingComponent.updateRoutingEntry(new BIC(bicValue), routingEntry);
    }

	@CrossOrigin(origins = "http://localhost:4200")
    @PutMapping(value = "routing-entries/clean-up")
    @ResponseBody
    ResponseEntity<?> cleanUpRoutingEntries() throws InterruptedException, ExecutionException, TimeoutException {
         dltRoutingComponent.cleanUpRoutingEntries();
         return new ResponseEntity<RoutingEntry>(HttpStatus.NO_CONTENT);
    }

    // TODO
    // file import and parse (old routing entry xml file format, see wiki)
    // anschauen wie ein file entgegen genommen werden kann
    // in der DltRoutingComponent parsen und die Routing Entries einzeln (oder vielleicht über eine bulk insert methode im chaincode?) an den ledger schicken

}