import { Injectable } from '@angular/core';

import { HttpClient, HttpHeaders } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';
import { catchError, map, tap } from 'rxjs/operators';

import { Bank } from '../models/bank';
import { RoutingEntry } from '../models/routingEntry';
import { BIC } from '../models/bic';


const httpOptions = {
  headers: new HttpHeaders(
  	{ 'Content-Type': 'application/json',
      'Access-Control-Allow-Origin':'*' 
  })
};

@Injectable()
export class RestService {
	url: string;

  constructor(
	    private http: HttpClient) {
  			//ToDo Url anpassen
  			//this.url = 'http://192.168.191.127:8080/rest/v1/dlt-routing';
  			this.url = 'http://localhost:8080/rest/v1/dlt-routing';

	    }

	saveRoutingEntry(entry: RoutingEntry, bic: BIC): Observable<RoutingEntry>{
		return this.http.post<RoutingEntry>(`${this.url}/bank/${bic}/routing-entry`,entry,httpOptions);
	}

	updateRoutingEntry(entry: RoutingEntry, bic: BIC ):  Observable<RoutingEntry>{
		return this.http.put<RoutingEntry>(`${this.url}/bank/${bic.value}/routing-entry/${entry.id}`,entry,httpOptions);
	}

	//Muss noch im BE implementiert werden
	deleteRoutingEntry(entry: RoutingEntry, bic: BIC ):  Observable<RoutingEntry>{
		return this.http.put<RoutingEntry>(`${this.url}/bank/${bic.value}/routing-entry/${entry.id}`,entry,httpOptions);
	}

	getBank (bic: string): Observable<Bank> {
		return this.http.get<Bank>(`${this.url}/bank/${bic}`);
	}

	addBank(bank: Bank): Observable<Bank> {
		return this.http.post<Bank>(`${this.url}/bank`, bank, httpOptions);
	}

	searchForRoutingEntries (bic: string): Observable<Bank[]> {
		return this.http.get<Bank[]>(`${this.url}/bank/${bic}`);
	}
	
	getBankByCooperationBic(bic: string): Observable<Bank[]> {
		return this.http.get<Bank[]>(`${this.url}/bank/cooperationBic/${bic}`);
	}

	startCleanUp():Observable<object> {
		return this.http.put(`${this.url}/routing-entries/clean-up`,{},httpOptions);
	}

}
