import { BankOperation } from './bankOperation';
import { Services } from './services';
import { BIC } from './bic';


export class RoutingEntry {
  id: string;
  bankOperation: string;
  service: string;
  counterPartyBic: BIC;
  validFrom: string;
  validTo: string;
  cooperationBic: BIC;
}

export class RoutingEntryExtended {
  id: string;
  bankOperation: string;
  service: string;
  counterPartyBic: BIC;
  validFrom: string;
  validTo: string;
  cooperationBic: BIC;
  bankname: string;
}

