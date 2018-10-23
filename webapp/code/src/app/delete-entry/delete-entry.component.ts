import { Component, OnInit } from '@angular/core';
import { Services } from '../models/services';
import { BankOperationClass } from '../models/bankOperation';
import { BankOperations } from '../models/bankOperation';
import { BankOperation } from '../models/bankOperation';
import { ServiceClass } from '../models/services';
import { RoutingEntry } from '../models/routingEntry';
import { BIC } from '../models/bic';
import { RestService } from '../services/rest.service';
import { Bank } from '../models/bank';
import { HelpService } from '../services/help.service';
import Swal from 'sweetalert2';



@Component({
  selector: 'app-delete-entry',
  templateUrl: './delete-entry.component.html',
  styleUrls: ['./delete-entry.component.css']
})
export class DeleteEntryComponent implements OnInit {
	//Processmodes
	//searchmode: boolean;
	//choosemode: boolean;
	//checkmode: boolean;
	//finishmode: boolean;

	//Search Variables
	//search: searchQuery;
	//searchResult: RoutingEntry[];
  //searchResultName: string;
  //searchResultBic: BIC;

	//Selected Routing Entries
	//selectedRoutingEntries: RoutingEntry[];

	//dtOptions: any = {};

  constructor(private restService: RestService, private helpService: HelpService) { }

  ngOnInit() {
  	/*this.searchmode = true;
	  this.choosemode = this.checkmode = this.finishmode = false;

  	this.search = {
  		bic: "",
  		service: " - ",
  		bankOperation: " - ",
  		bankOperations: BankOperations,
  		services: []
  	};

  	this.dtOptions = {
       columnDefs: [ { orderable: false, targets: [0] }]
    };*/

    this.helpService.SetContentHight();
  }

  startCleanUp(){
    this.restService.startCleanUp()
    .subscribe(function(result: any){
      Swal('CleanUp war erfolgreich', "", 'success');
    },function(error: any){
      Swal('Beim CleanUp ist ein Fehler aufgetreten', error.error.message, 'error');
    })
  }
  /*
   //Wird beim Auswählen einer Bankoperation in der Suchanfrage ausgeführt
  changeSearchBankOperation(operation: BankOperationClass){
	this.search.bankOperation = operation.name;
	this.search.services = operation.services;
	this.search.service = operation.services[0].name;
  }

  //Wird beim Auswählen eines Services in der Suchanfrage ausgeführt
  changeSearchService(service){
  	this.search.service = service.name;
  }

  //Routing-Einträge für das ausgewählte Institut wird vom Server abgefragt
  searchInstitut(){

  	var self = this;
    this.restService.searchForRoutingEntries(this.search.bic)
    .subscribe(function(result: any){
        var bank  = result as Bank;
        self.searchResultName = bank.name;
        self.searchResultBic =bank.bic;
        self.searchResult = bank.routingEntryList;
        self.searchmode = false;
        self.choosemode = true;

        setTimeout(function(){
          $('.borderless').removeClass('sorting_asc')    
        },50);
    });
  }

  //Ausgewählter Routingeinträge werden in eine Variablen gespeichert und nochmal angezeigt angezeigt
  chooseEntry(){
  	this.checkmode = true;
  	this.choosemode = false;
  	this.selectedRoutingEntries = [];

  	var self = this;
  	$(".checkbox:checked").map(function(this: any) {
  		self.selectedRoutingEntries.push(Object.assign({}, self.searchResult[parseInt(this.value)]));
	  }).get();

  }

   //Änderung wird an den Server übermittelt
  checkEntry(){
  	this.checkmode = false;
  	this.finishmode = true;
  	$('.editField').css('display','none');


  	//Muss noch im BE implementiert werden
  	//this.restService.updateRoutingEntry(this.newRoutingEntry)
  	//.subscribe(entries => test = entries);
  }

  returnToSearchInstitut(){
  	this.searchmode = true;
  	this.choosemode = false;
  }

  //Navigiert zurück zur Auswahl der Routingeinträgen
  returnToChooseEntry(){
  	this.choosemode = true;
  	this.checkmode = false;
  }*/

}
/*
//Hilfsklasse für Suchanfrage
class searchQuery {
	bic: string;
	bankOperation: string;
	service: string;
	bankOperations: BankOperationClass[]
	services: ServiceClass[]
}
*/