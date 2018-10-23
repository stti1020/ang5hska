import { Component, OnInit } from '@angular/core';
import { Services } from '../models/services';
import { BankOperationClass } from '../models/bankOperation';
import { BankOperations } from '../models/bankOperation';
import { BankOperation } from '../models/bankOperation';
import { ServiceClass } from '../models/services';
import { RoutingEntryExtended } from '../models/routingEntry';
import { RoutingEntry } from '../models/routingEntry';
import { BIC } from '../models/bic';
import { RestService } from '../services/rest.service';
import { Bank } from '../models/bank';
import { HelpService } from '../services/help.service';

import Swal from 'sweetalert2';
//var dt = require( 'datatables.net' )( window, $ );


@Component({
  selector: 'app-extract-entry',
  templateUrl: './extract-entry.component.html',
  styleUrls: ['./extract-entry.component.css']
})
export class ExtractEntryComponent implements OnInit {
	//Processmodes
	searchmode: boolean;
	extractmode: boolean;

	//Search Variables
	search: searchQuery;
	searchResult: RoutingEntryExtended[];

	//Datatable
	dtOptions: any = {};

  //Sonstige
  dateFormat: string;

  //Filter
  filter: RoutingEntry;


  constructor(private restService: RestService, private helpService: HelpService) { }

  ngOnInit() {
  	this.searchmode = true;
  	this.extractmode = false;

  	 this.search = {
  		bic: "",
  		service: " - ",
  		bankOperation: " - ",
  		bankOperations: BankOperations,
  		services: []
  	};

	 this.dtOptions = {
       // Declare the use of the extension in the dom parameter
      dom: 'Bfrtip',
        buttons: [
           'csv'        ]
    };
    this.dateFormat = 'dd.MM.yyyy'
    this.filter = new RoutingEntry();
    this.helpService.SetContentHight();

  }

   //Wird beim Auswählen einer Bankoperation in der Suchanfrage ausgeführt
  changeSearchBankOperation(operation: BankOperationClass){
  	this.search.bankOperation = operation.name;
  	this.search.services = operation.services;
  	this.search.service = operation.services[0].name;
    this.filter.bankOperation = this.search.bankOperation;
    this.filter.service = this.search.service;
  }

  //Wird beim Auswählen eines Services in der Suchanfrage ausgeführt
  changeSearchService(service){
  	this.search.service = service.name;
    this.filter.service = this.search.service;
  }

  //Routing-Einträge für das ausgewählte Institut wird vom Server abgefragt
  searchInstitut(){
  	

    var self = this;
  	this.restService.getBankByCooperationBic(this.search.bic)
  	.subscribe(function(result: Bank[]){
        self.searchmode = false;
        self.extractmode = true;
        self.searchResult = [];
        result.forEach(function(bank){
           bank.routingEntryList.forEach(function(entry){
               var newEntry = new RoutingEntryExtended();
               newEntry.bankname = bank.name;
               newEntry.cooperationBic = entry.cooperationBic;
               newEntry.counterPartyBic = entry.counterPartyBic;
               newEntry.validFrom = entry.validFrom;
               newEntry.validTo = entry.validTo;
               newEntry.service = entry.service;
               newEntry.bankOperation = entry.bankOperation;
               newEntry.id = entry.id;
               self.searchResult.push(newEntry);
           })
        })
        setTimeout(function(){
          $('table').DataTable(self.dtOptions);
          $('.dt-button').css('display', 'none');
        }, 200);
    }, function(error){
       Swal('Beim Suchen ist ein Fehler aufgetreten', error.error.message, 'error');
    })
  }

  exportEntry(){
  	$('.dt-button').click();
  }

  returnToSearchInstitut(){
  	this.searchmode = true;
  	this.extractmode = false;
  }

   clearChanges(){
    this.filter = new RoutingEntry();
    this.search = {
      bic: "",
      service: " - ",
      bankOperation: " - ",
      bankOperations: BankOperations,
      services: []
    };
   this.extractmode = false;
    this.searchmode = true;
  }

}

//Hilfsklasse für Suchanfrage
class searchQuery {
	bic: string;
	bankOperation: string;
	service: string;
	bankOperations: BankOperationClass[]
	services: ServiceClass[]
}
