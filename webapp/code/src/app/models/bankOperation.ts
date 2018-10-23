import { Services } from './services';
import { ServiceClass } from './services';

export enum BankOperation {
	SDD = "SDD", 
	SCT = "SCT"
}

export class BankOperationClass {
	name: string;
	services: ServiceClass[];
}

export var BankOperations: BankOperationClass[] = 
[
	{
		name: BankOperation.SDD, 
		services: [
		{
			name: Services.SDD_B2B
		}, {
			name: Services.SDD_CORE
		}],
	}, 
	{
		name: BankOperation.SCT, 
		services: [
		{
			name: Services.SCT_INST
		}, {
			name: Services.SEPA_CT
		}],
	}
]






