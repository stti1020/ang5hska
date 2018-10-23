package at.oenb.dltrouting;

import at.oenb.dlt.AbstractDltTest;
import at.oenb.dltrouting.domain.*;
import at.oenb.infrastructure.GeneratorUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static at.oenb.dltrouting.helper.Formatter.DATE_TIME;
import static org.junit.Assert.*;

//TODO after asserting an exception within a test, read entity again and check if nothing changed ... in all tests
public class ChainCodeIT extends AbstractDltTest {

    private static final BIC TEST_MARKDEF0_BIC = new BIC("MARKDEF0");
    private static final RoutingEntry TEST_ROUTING_ENTRY_WITHOUT_ID = toValidRoutingEntry(null);
    private static final BIC TEST_BIC = new BIC("SOLADES1KNZ");
    private static final Bank TEST_BANK = toBank(TEST_BIC, "Sparkasse Bodensee", TEST_ROUTING_ENTRY_WITHOUT_ID);
    private static final BIC NOT_EXISTING_BIC = new BIC("NOTEXIST");
    private static final BIC INVALID_BIC = new BIC("INVALID BIC");

    @Autowired
    private DltRoutingComponent dltRoutingComponent;

    @PostConstruct
    public void setup() {
        createAndAssertTestBank();
    }

    @Test
    public void whenABankIsCreatedThenItCanBeReadAgain() {
        createAndAssertRandomBank();
    }

    @Test
    public void whenABankWithAnInvalidBicShouldBeCreatedThenAnErrorShouldHappen() {
        Bank bank = GeneratorUtil.generateRandomBank();
        bank.setBic(INVALID_BIC);
        try {
            dltRoutingComponent.createBank(bank);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, "BIC is invalid.");
        }
    }

    @Test
    public void whenADuplicateBankIsCreatedThenAnErrorShouldHappen() {
        Bank createdBank = createAndAssertRandomBank();

        Bank bankSameBicDifferentName = createdBank.toBuilder()
                .name("Duplicate BIC")
                .build();
        try {
            dltRoutingComponent.createBank(bankSameBicDifferentName);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, String.format("Bank with BIC %s already exists.", createdBank.getBic().getValue()));
        }
        Bank queriedBank = dltRoutingComponent.getBank(createdBank.getBic());
        assertEquals(createdBank, queriedBank);
    }

    @Test
    public void whenAnExistingBankIsQueriedByBicThenItIsReturned() {
        Bank testBank = dltRoutingComponent.getBank(TEST_BANK.getBic());
        assertNotNull(testBank);
        assertEquals(TEST_BANK.getBic(), testBank.getBic());
        assertEquals(TEST_BANK.getName(), testBank.getName());
    }

    @Test
    public void whenANotExistingBankIsQueriedByBicThenAnErrorShouldHappen() {
        try {
            dltRoutingComponent.getBank(NOT_EXISTING_BIC);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, "Bank does not exist");
        }
    }

    @Test
    public void whenABankIsQueriedByAnInvalidBicThenAnErrorShouldHappen() {
        try {
            dltRoutingComponent.getBank(INVALID_BIC);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, "BIC is invalid.");
        }
    }

    @Test
    public void whenAnExistingBankIsQueriedByACooperationBicThenItIsReturned() {
        BIC randomCooperationBic = GeneratorUtil.generateRandomBIC();
        Bank createdBankOne = createAndAssertRandomBank();
        Bank createdBankTwo = createAndAssertRandomBank();
        Bank createdBankThree = createAndAssertRandomBank();

        RoutingEntry sampleRoutingEntry = toValidRoutingEntry(null);
        sampleRoutingEntry.setCooperationBic(randomCooperationBic);
        dltRoutingComponent.createRoutingEntry(createdBankOne.getBic(), sampleRoutingEntry);
        dltRoutingComponent.createRoutingEntry(createdBankTwo.getBic(), sampleRoutingEntry);
        dltRoutingComponent.createRoutingEntry(createdBankThree.getBic(), sampleRoutingEntry);

        List<Bank> testBanks = dltRoutingComponent.getBankByCooperationBic(randomCooperationBic);
        assertNotNull(testBanks);
        assertEquals(3, testBanks.size());
        List<BIC> routingEntryBics = testBanks.stream().map(Bank::getBic).collect(Collectors.toList());
        assertTrue(routingEntryBics.contains(createdBankOne.getBic()));
        assertTrue(routingEntryBics.contains(createdBankTwo.getBic()));
        assertTrue(routingEntryBics.contains(createdBankThree.getBic()));
    }

    @Test
    public void whenABankIsQueriedWithByAnInvalidCooperationBicThenAnErrorShouldHappen() {
        try {
            dltRoutingComponent.getBankByCooperationBic(INVALID_BIC);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, "BIC is invalid.");
        }
    }

    @Test
    public void whenANotExistingBankIsQueriedByACooperationBicThenAnErrorShouldHappen() {
        try {
            dltRoutingComponent.getBankByCooperationBic(NOT_EXISTING_BIC);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, String.format("No entries with Cooperation Bic: %s found", NOT_EXISTING_BIC.getValue()));
        }
    }

    @Test
    public void whenARoutingEntryIsCreatedForAnExistingBankThenItCanBeReadAgain() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        dltRoutingComponent.createRoutingEntry(createdBank.getBic(), routingEntryToCreate);
        Bank queriedBank = dltRoutingComponent.getBank(createdBank.getBic());
        RoutingEntry queriedRoutingEntry = assertAndReturnExactlyOneRoutingEntry(queriedBank);
        RoutingEntry expectedRoutingEntry = routingEntryToCreate
                .toBuilder()
                .id(queriedRoutingEntry.getId())
                .build();
        Bank expectedBank = createdBank
                .toBuilder()
                .routingEntryList(Collections.singletonList(expectedRoutingEntry))
                .build();
        assertEquals(expectedBank, queriedBank);
    }

    @Test
    public void whenARoutingEntryShouldBeCreatedForANotExistingBankThenAnErrorShouldHappen() {
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        try {
            dltRoutingComponent.createRoutingEntry(NOT_EXISTING_BIC, routingEntryToCreate);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, "Bank does not exist");
        }
    }

    @Test
    public void whenARoutingEntryShouldBeCreatedForAnInvalidBankBicThenAnErrorShouldHappen() {
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        try {
            dltRoutingComponent.createRoutingEntry(INVALID_BIC, routingEntryToCreate);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, "BIC is invalid.");
        }
    }

    @Test
    public void whenARoutingEntryWithAnInvalidBankOperationShouldBeCreatedThenAnErrorShouldHappen() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        routingEntryToCreate.setBankOperation(null);
        try {
            dltRoutingComponent.createRoutingEntry(createdBank.getBic(), routingEntryToCreate);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, "Bank operation is invalid.");
        }
    }

    @Test
    public void whenARoutingEntryWithAnInvalidServiceShouldBeCreatedThenAnErrorShouldHappen() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        routingEntryToCreate.setService(null);
        try {
            dltRoutingComponent.createRoutingEntry(createdBank.getBic(), routingEntryToCreate);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, "Service is invalid.");
        }
    }

    @Test
    public void whenARoutingEntryWithAnInvalidBankOperationAndServiceCombinationShouldBeCreatedThenAnErrorShouldHappen() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        routingEntryToCreate.setBankOperation(BankOperation.SCT);
        routingEntryToCreate.setService(Service.SDD_CORE);
        try {
            dltRoutingComponent.createRoutingEntry(createdBank.getBic(), routingEntryToCreate);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, "Service is invalid.");
        }
    }

    @Test
    public void whenARoutingEntryWithAnValidBankOperationAndServiceCombinationIsCreatedThenItShouldBeReturned() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        routingEntryToCreate.setBankOperation(BankOperation.SCT);
        routingEntryToCreate.setService(Service.SEPA_CT);
        dltRoutingComponent.createRoutingEntry(createdBank.getBic(), routingEntryToCreate);
        Bank createdBankWithRoutingEntry = dltRoutingComponent.getBank(createdBank.getBic());
        RoutingEntry routingEntry = assertAndReturnExactlyOneRoutingEntry(createdBankWithRoutingEntry);
        assertEquals(BankOperation.SCT, routingEntry.getBankOperation());
        assertEquals(Service.SEPA_CT, routingEntry.getService());
    }

    @Test
    public void whenARoutingEntryWithAnInvalidCounterPartyBicShouldBeCreatedThenAnErrorShouldHappen() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        routingEntryToCreate.setCounterPartyBic(INVALID_BIC);
        try {
            dltRoutingComponent.createRoutingEntry(createdBank.getBic(), routingEntryToCreate);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, "Counter party BIC is invalid.");
        }
    }

    @Test
    public void whenARoutingEntryWithAnEmptyCounterPartyBicIsCreatedThenItIsReturnedHavingACounterPartyBicEqualToTheBanksBic() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        routingEntryToCreate.setCounterPartyBic(null);
        dltRoutingComponent.createRoutingEntry(createdBank.getBic(), routingEntryToCreate);

        Bank createdBankWithRoutingEntry = dltRoutingComponent.getBank(createdBank.getBic());
        RoutingEntry routingEntry = assertAndReturnExactlyOneRoutingEntry(createdBankWithRoutingEntry);
        assertNotNull(routingEntry.getCounterPartyBic());
        assertEquals(createdBank.getBic(), routingEntry.getCounterPartyBic());
    }

    @Test
    public void whenARoutingEntryWithAnInvalidValidFromShouldBeCreatedThenAnErrorShouldHappen() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        routingEntryToCreate.setValidFrom(null);
        try {
            dltRoutingComponent.createRoutingEntry(createdBank.getBic(), routingEntryToCreate);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, "ValidFrom is invalid.");
        }
    }

    @Test
    public void whenARoutingEntryWithEmptyValidToIsCreatedThenItIsReturned() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        routingEntryToCreate.setValidTo(null);
        dltRoutingComponent.createRoutingEntry(createdBank.getBic(), routingEntryToCreate);
        Bank createdBankWithRoutingEntry = dltRoutingComponent.getBank(createdBank.getBic());
        RoutingEntry createdRoutingEntry = assertAndReturnExactlyOneRoutingEntry(createdBankWithRoutingEntry);
        assertNull(createdRoutingEntry.getValidTo());
    }

    @Test
    public void whenARoutingEntryWithAValidToBeforeValidFromShouldBeCreatedThenAnErrorShouldHappen() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        routingEntryToCreate.setValidTo(routingEntryToCreate.getValidFrom().minusDays(5));
        try {
            dltRoutingComponent.createRoutingEntry(createdBank.getBic(), routingEntryToCreate);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, "Combination of valideFrom and valideTo is invalid.");
        }
    }

    @Test
    public void whenARoutingEntryWithAnInvalidCooperationBicShouldBeCreatedThenAnErrorShouldHappen() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        routingEntryToCreate.setCooperationBic(INVALID_BIC);
        try {
            dltRoutingComponent.createRoutingEntry(createdBank.getBic(), routingEntryToCreate);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, "Cooperation BIC is invalid.");
        }
    }

    @Test
    public void whenARoutingEntryIsUpdatedForAnExistingBankThenItCanBeReadAgain() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        dltRoutingComponent.createRoutingEntry(createdBank.getBic(), routingEntryToCreate);
        Bank queriedBank = dltRoutingComponent.getBank(createdBank.getBic());
        RoutingEntry createdRoutingEntry = assertAndReturnExactlyOneRoutingEntry(queriedBank);
        RoutingEntry routingEntryToUpdate = createdRoutingEntry
                .toBuilder()
                .service(Service.SEPA_CT)
                .build();
        dltRoutingComponent.updateRoutingEntry(createdBank.getBic(), routingEntryToUpdate);
        Bank expectedBank = createdBank
                .toBuilder()
                .routingEntryList(Collections.singletonList(routingEntryToUpdate))
                .build();
        queriedBank = dltRoutingComponent.getBank(createdBank.getBic());
        assertEquals(expectedBank, queriedBank);
    }

    @Test
    public void whenARoutingEntryIsUpdatedForANotExistingBankThenAnErrorShouldHappen() {
        RoutingEntry routingEntry = toValidRoutingEntry(UUID.randomUUID().toString());
        try {
            dltRoutingComponent.updateRoutingEntry(NOT_EXISTING_BIC, routingEntry);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, "Bank does not exist");
        }
    }

    @Test
    public void whenANotExistingRoutingEntryIsUpdatedThenAnErrorShouldHappen() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry notExistingRoutingEntry = toValidRoutingEntry(UUID.randomUUID().toString());
        try {
            dltRoutingComponent.updateRoutingEntry(createdBank.getBic(), notExistingRoutingEntry);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, String.format("The RoutingEntryList for the Key/BIC: %s is empty", createdBank.getBic().getValue()));
        }
    }

    @Test
    public void whenARoutingEntryShouldBeUpdatedWithAnInvalidBicThenAnErrorShouldHappen() {
        RoutingEntry routingEntryToUpdate = toValidRoutingEntry(null);
        try {
            dltRoutingComponent.updateRoutingEntry(INVALID_BIC, routingEntryToUpdate);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, "BIC is invalid.");
        }
    }

    @Test
    public void whenAnExistingRoutingEntryShouldBeUpdatedWithAnInvalidBankOperationThenAnErrorShouldHappen() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        dltRoutingComponent.createRoutingEntry(createdBank.getBic(), routingEntryToCreate);
        Bank queriedBank = dltRoutingComponent.getBank(createdBank.getBic());

        RoutingEntry queriedRoutingEntry = queriedBank.getRoutingEntryList().iterator().next();
        queriedRoutingEntry.setBankOperation(null);
        try {
            dltRoutingComponent.updateRoutingEntry(queriedBank.getBic(), queriedRoutingEntry);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, "Bank operation is invalid.");
        }
    }

    @Test
    public void whenAnExistingRoutingEntryShouldBeUpdatedWithAnInvalidServiceOperationThenAnErrorShouldHappen() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        dltRoutingComponent.createRoutingEntry(createdBank.getBic(), routingEntryToCreate);
        Bank queriedBank = dltRoutingComponent.getBank(createdBank.getBic());

        RoutingEntry queriedRoutingEntry = queriedBank.getRoutingEntryList().iterator().next();
        queriedRoutingEntry.setService(null);
        try {
            dltRoutingComponent.updateRoutingEntry(queriedBank.getBic(), queriedRoutingEntry);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, "Service is invalid.");
        }
    }

    @Test
    public void whenAnExistingRoutingEntryShouldBeUpdatedWithAnInvalidBankOperationAndServiceCombinationThenAnErrorShouldHappen() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        dltRoutingComponent.createRoutingEntry(createdBank.getBic(), routingEntryToCreate);
        Bank queriedBank = dltRoutingComponent.getBank(createdBank.getBic());

        RoutingEntry queriedRoutingEntry = queriedBank.getRoutingEntryList().iterator().next();
        queriedRoutingEntry.setBankOperation(BankOperation.SCT);
        queriedRoutingEntry.setService(Service.SDD_B2B);
        try {
            dltRoutingComponent.updateRoutingEntry(queriedBank.getBic(), queriedRoutingEntry);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, "Service is invalid.");
        }
    }

    @Test
    public void whenAnExistingRoutingEntryIsUpdatedWithAValidBankOperationAndServiceCombinationThenItIsReturned() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        dltRoutingComponent.createRoutingEntry(createdBank.getBic(), routingEntryToCreate);
        Bank queriedBank = dltRoutingComponent.getBank(createdBank.getBic());

        RoutingEntry queriedRoutingEntry = queriedBank.getRoutingEntryList().iterator().next();
        queriedRoutingEntry.setBankOperation(BankOperation.SCT);
        queriedRoutingEntry.setService(Service.SEPA_CT);
        dltRoutingComponent.updateRoutingEntry(queriedBank.getBic(), queriedRoutingEntry);

        Bank createdBankWithRoutingEntry = dltRoutingComponent.getBank(createdBank.getBic());
        RoutingEntry routingEntry = assertAndReturnExactlyOneRoutingEntry(createdBankWithRoutingEntry);
        assertEquals(BankOperation.SCT, routingEntry.getBankOperation());
        assertEquals(Service.SEPA_CT, routingEntry.getService());
    }

    @Test
    public void whenAnExistingRoutingEntryShouldBeUpdatedWithAnInvalidCounterPartyBicThenAnErrorShouldHappen() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        dltRoutingComponent.createRoutingEntry(createdBank.getBic(), routingEntryToCreate);
        Bank queriedBank = dltRoutingComponent.getBank(createdBank.getBic());

        RoutingEntry queriedRoutingEntry = queriedBank.getRoutingEntryList().iterator().next();
        queriedRoutingEntry.setCounterPartyBic(INVALID_BIC);
        try {
            dltRoutingComponent.updateRoutingEntry(queriedBank.getBic(), queriedRoutingEntry);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, "Counter party BIC is invalid.");
        }
    }

    @Test
    public void whenAnExistingRoutingEntryIsUpdatedWithAnEmptyCounterPartyBicThenItIsReturnedHavingACounterPartyBicEqualToTheBanksBic() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        dltRoutingComponent.createRoutingEntry(createdBank.getBic(), routingEntryToCreate);
        Bank queriedBank = dltRoutingComponent.getBank(createdBank.getBic());

        RoutingEntry queriedRoutingEntry = queriedBank.getRoutingEntryList().iterator().next();
        queriedRoutingEntry.setCounterPartyBic(null);
        dltRoutingComponent.updateRoutingEntry(queriedBank.getBic(), queriedRoutingEntry);

        Bank createdBankWithRoutingEntry = dltRoutingComponent.getBank(createdBank.getBic());
        RoutingEntry routingEntry = assertAndReturnExactlyOneRoutingEntry(createdBankWithRoutingEntry);
        assertNotNull(routingEntry.getCounterPartyBic());
        assertEquals(createdBank.getBic(), routingEntry.getCounterPartyBic());
    }

    @Test
    public void whenAnExistingRoutingEntryShouldBeUpdatedWithAnInvalidValidFromThenAnErrorShouldHappen() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        dltRoutingComponent.createRoutingEntry(createdBank.getBic(), routingEntryToCreate);
        Bank queriedBank = dltRoutingComponent.getBank(createdBank.getBic());

        RoutingEntry queriedRoutingEntry = queriedBank.getRoutingEntryList().iterator().next();
        queriedRoutingEntry.setValidFrom(null);
        try {
            dltRoutingComponent.updateRoutingEntry(queriedBank.getBic(), queriedRoutingEntry);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, "ValidFrom is invalid.");
        }
    }

    @Test
    public void whenAnExistingRoutingEntryIsUpdatedWithAnEmptyValidToThenItIsReturned() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        dltRoutingComponent.createRoutingEntry(createdBank.getBic(), routingEntryToCreate);
        Bank queriedBank = dltRoutingComponent.getBank(createdBank.getBic());

        RoutingEntry queriedRoutingEntry = queriedBank.getRoutingEntryList().iterator().next();
        queriedRoutingEntry.setValidTo(null);
        dltRoutingComponent.updateRoutingEntry(queriedBank.getBic(), queriedRoutingEntry);

        Bank createdBankWithRoutingEntry = dltRoutingComponent.getBank(createdBank.getBic());
        RoutingEntry createdRoutingEntry = assertAndReturnExactlyOneRoutingEntry(createdBankWithRoutingEntry);
        assertNull(createdRoutingEntry.getValidTo());
    }

    @Test
    public void whenAnExistingRoutingEntryShouldBeUpdatedWithAValidToBeforeValidFromThenAnErrorShouldHappen() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        dltRoutingComponent.createRoutingEntry(createdBank.getBic(), routingEntryToCreate);
        Bank queriedBank = dltRoutingComponent.getBank(createdBank.getBic());

        RoutingEntry queriedRoutingEntry = queriedBank.getRoutingEntryList().iterator().next();
        queriedRoutingEntry.setValidTo(queriedRoutingEntry.getValidFrom().minusDays(5));
        try {
            dltRoutingComponent.updateRoutingEntry(queriedBank.getBic(), queriedRoutingEntry);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, "Combination of valideFrom and valideTo is invalid.");
        }
    }

    @Test
    public void whenAnExistingRoutingEntryShouldBeUpdatedWithAnInvalidCooperationBicThenAnErrorShouldHappen() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        dltRoutingComponent.createRoutingEntry(createdBank.getBic(), routingEntryToCreate);
        Bank queriedBank = dltRoutingComponent.getBank(createdBank.getBic());

        RoutingEntry queriedRoutingEntry = queriedBank.getRoutingEntryList().iterator().next();
        queriedRoutingEntry.setCooperationBic(INVALID_BIC);
        try {
            dltRoutingComponent.updateRoutingEntry(queriedBank.getBic(), queriedRoutingEntry);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, "Cooperation BIC is invalid.");
        }
    }

    @Test
    public void whenAnExistingRoutingEntryShouldBeUpdatedWithAnInvalidIdThenAnErrorShouldHappen() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        dltRoutingComponent.createRoutingEntry(createdBank.getBic(), routingEntryToCreate);
        Bank queriedBank = dltRoutingComponent.getBank(createdBank.getBic());

        RoutingEntry queriedRoutingEntry = queriedBank.getRoutingEntryList().iterator().next();
        queriedRoutingEntry.setId("invalid_id");
        try {
            dltRoutingComponent.updateRoutingEntry(queriedBank.getBic(), queriedRoutingEntry);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, "The RoutingEntry with the Id: invalid_id does not exist");
        }
    }

    @Test
    public void whenAnExistingRoutingEntryShouldBeUpdatedWithAnEmptyIdThenAnErrorShouldHappen() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry routingEntryToCreate = toValidRoutingEntry(null);
        dltRoutingComponent.createRoutingEntry(createdBank.getBic(), routingEntryToCreate);
        Bank queriedBank = dltRoutingComponent.getBank(createdBank.getBic());

        RoutingEntry queriedRoutingEntry = queriedBank.getRoutingEntryList().iterator().next();
        queriedRoutingEntry.setId(null);
        try {
            dltRoutingComponent.updateRoutingEntry(queriedBank.getBic(), queriedRoutingEntry);
            fail();
        } catch (Exception e) {
            assertErrorMessage(e, "The RoutingEntry with the Id:  does not exist");
        }
    }

    @Test
    public void whenAnExistingInvalidRoutingEntryIsDeletedThenItShouldNotExistAnymore() {
        Bank createdBank = createAndAssertRandomBank();
        RoutingEntry invalidRoutingEntry = toInvalidRoutingEntry(null);
        dltRoutingComponent.createRoutingEntry(createdBank.getBic(), invalidRoutingEntry);
        List<RoutingEntry> queriedRoutingEntry = dltRoutingComponent.getBank(createdBank.getBic()).getRoutingEntryList();
        assertEquals(1, queriedRoutingEntry.size());
        dltRoutingComponent.cleanUpRoutingEntries();
        queriedRoutingEntry = dltRoutingComponent.getBank(createdBank.getBic()).getRoutingEntryList();
        assertNull(queriedRoutingEntry);
    }

    // Helper-------------------------------------------------------------------------------------------------

    private Bank createAndAssertRandomBank() {
        Bank bank = GeneratorUtil.generateRandomBank();
        dltRoutingComponent.createBank(bank);
        Bank createdBank = dltRoutingComponent.getBank(bank.getBic());
        assertEquals(bank, createdBank);
        return bank;
    }

    private void createAndAssertTestBank() {
        Bank testBank;
        try {
            testBank = dltRoutingComponent.getBank(TEST_BANK.getBic());
        } catch (Exception e) {
            if (e.getMessage().contains("Bank does not exist")) {
                dltRoutingComponent.createBank(TEST_BANK);
                dltRoutingComponent.createRoutingEntry(TEST_BIC, TEST_ROUTING_ENTRY_WITHOUT_ID);
                testBank = dltRoutingComponent.getBank(TEST_BANK.getBic());
            } else {
                throw e;
            }
        }
        RoutingEntry routingEntry = assertAndReturnExactlyOneRoutingEntry(testBank);
        RoutingEntry expectedTestBankRoutingEntryWithId = TEST_BANK.getRoutingEntryList().get(0).toBuilder()
                .id(routingEntry.getId())
                .build();
        Bank expectedTestBankWithRoutingEntryWithId = TEST_BANK.toBuilder()
                .routingEntryList(Collections.singletonList(expectedTestBankRoutingEntryWithId))
                .build();
        assertEquals(expectedTestBankWithRoutingEntryWithId, testBank);
    }

    private static Bank toBank(BIC bic, String name, RoutingEntry routingEntry) {
        return Bank.builder()
                .bic(bic)
                .name(name)
                .routingEntryList(Optional.of(routingEntry).map(Collections::singletonList).orElse(new ArrayList<>()))
                .build();
    }

    private static RoutingEntry toInvalidRoutingEntry(String id) {
        return toRoutingEntry(id,
                OffsetDateTime.parse("2012-11-01T22:08:41+00:00", DATE_TIME),
                OffsetDateTime.parse("2015-11-01T22:08:41+00:00", DATE_TIME));
    }

    private static RoutingEntry toValidRoutingEntry(String id) {
        return toRoutingEntry(id,
                OffsetDateTime.parse("2012-11-01T22:08:41+00:00", DATE_TIME),
                OffsetDateTime.parse("2019-11-01T22:08:41+00:00", DATE_TIME));
    }

    private static RoutingEntry toRoutingEntry(String id, OffsetDateTime from, OffsetDateTime to) {
        return RoutingEntry.builder()
                .id(id)
                .bankOperation(BankOperation.SCT)
                .service(Service.SCT_INST)
                .counterPartyBic(TEST_MARKDEF0_BIC)
                .validFrom(from)
                .validTo(to)
                .cooperationBic(TEST_MARKDEF0_BIC)
                .build();
    }

    private RoutingEntry assertAndReturnExactlyOneRoutingEntry(Bank bank) {
        assertNotNull(bank);
        assertNotNull(bank.getRoutingEntryList());
        assertEquals(bank.getRoutingEntryList().size(), 1);
        RoutingEntry routingEntry = bank.getRoutingEntryList().get(0);
        assertValidUUID(routingEntry.getId());
        return routingEntry;
    }

    private void assertValidUUID(String id) {
        assertNotNull(id);
        assertTrue(id.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    private void assertErrorMessage(Exception e, String expectedErrorMessage) {
        assertTrue(String.format("Expected '%s' within message:\n%s", expectedErrorMessage, e.getMessage()),
                e.getMessage().contains(expectedErrorMessage));
    }
}
