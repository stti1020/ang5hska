import { RoutingEntry } from './routingEntry';
import { BIC } from './bic';


export class Bank {
  bic: BIC;
  name: string;
  routingEntryList: RoutingEntry[] 
}
