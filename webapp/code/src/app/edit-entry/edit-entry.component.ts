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

import * as moment from 'moment';
import Swal from 'sweetalert2';



@Component({
  selector: 'app-edit-entry',
  templateUrl: './edit-entry.component.html',
  styleUrls: ['./edit-entry.component.css']
})
export class EditEntryComponent implements OnInit {
	//Variables for dropdown
	bankOperations: BankOperationClass[];
	selectedBankOperation: BankOperationClass;
	services: ServiceClass[];
	selectedService: ServiceClass;

	//New and old routing entry
	newRoutingEntry: RoutingEntry;
	oldRoutingEntry: RoutingEntry;

	//Processmodes
	searchmode: boolean;
	choosemode: boolean;
	editmode: boolean;
	checkmode: boolean;
	finishmode: boolean;

	//Search Variables
	search: searchQuery;
	searchResult: RoutingEntry[];
  searchResultName: string;
  searchResultBic: BIC;

  //Sonstige
  dateFormat: string;
  momentDateFormat: string;

  //Filter
  filter: RoutingEntry;

  constructor(private restService: RestService, private helpService: HelpService) { }

  ngOnInit() {
  	this.searchmode = true;
  	this.choosemode = this.editmode = this.checkmode = this.finishmode = false;

  	this.search = {
  		bic: "",
  		service: " - ",
  		bankOperation: " - ",
  		bankOperations: BankOperations,
  		services: []
  	};

    this.dateFormat = 'dd.MM.yyyy'
    this.momentDateFormat = 'DD.MM.YYYY';
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
  	this.restService.searchForRoutingEntries(this.search.bic)
  	.subscribe(function(result: any){
        var bank  = result as Bank;
        self.searchResultName = bank.name;
        self.searchResultBic =bank.bic;
        self.searchResult = bank.routingEntryList;
        self.searchmode = false;
        self.choosemode = true;
        self.oldRoutingEntry = JSON.parse(JSON.stringify(self.searchResult[0]));
        self.newRoutingEntry = JSON.parse(JSON.stringify(self.searchResult[0]));
        setTimeout(function(){
          $(".radio").first().prop("checked", true);
        });
    },function(error){
            Swal('Beim Suchen ist ein Fehler aufgetreten', error.error.message, 'error')
    });
  }

  selectRoutingEntry(entry: RoutingEntry){
    this.oldRoutingEntry = JSON.parse(JSON.stringify(entry));
    this.newRoutingEntry = JSON.parse(JSON.stringify(entry));
  }

  //Ausgewählter Routingeintrag wird in Variablen gespeichert und im Änderungsmodus angezeigt
  //Datepicker wird erstellet und gesetzt
  chooseEntry(){
    $("input[type='radio']:checked").click();
  	this.editmode = true;
  	this.choosemode = false;
    this.oldRoutingEntry.validFrom = moment(this.oldRoutingEntry.validFrom).format(this.momentDateFormat);
    this.oldRoutingEntry.validTo = moment(this.oldRoutingEntry.validTo).format(this.momentDateFormat);

  	this.bankOperations = BankOperations;
  	var self = this;
  	var selectedIndex = 0;
  	this.bankOperations.forEach(function (operation:BankOperationClass, index: number) {
  	  if(operation.name === self.oldRoutingEntry.bankOperation){
  	  		selectedIndex = index;
  	  }
  	});

  this.selectedBankOperation = BankOperations[selectedIndex];
	this.services = BankOperations[selectedIndex].services;
	this.services.forEach(function (service:ServiceClass, index: number) {
	  if(service.name === self.oldRoutingEntry.service){
	  		selectedIndex = index;
	  }
	});
	this.selectedService = this.services[selectedIndex];

	setTimeout(function(){
	  	self.setDatePicker(self);
	  	//Alle Felder der rechten Spalte bekommen einen weißen Hintergrund
	  	$('.editField').css('background-color', 'white');  	
  	});
  }

  //Wird beim Auswählen einer Bankoperation im Änderungsmodus ausgeführt
  changeBankOperation(operation){
  	this.newRoutingEntry.bankOperation = operation.name;
	  this.services = operation.services;
	  this.newRoutingEntry.service = this.services[0].name;
  }

  //Wird beim Auswählen eines Services im Änderungsmodus ausgeführt
  changeService(service){
  	this.newRoutingEntry.service = service.name;
  }

  //Die Felder der rechten Spalte werden zum bearbeiten deaktiviert
  editEntry(){
  	this.editmode = false;
  	this.checkmode = true;
	$('.editField').css('background-color', '#eeeeee');
	$(".editField").prop('disabled', true);   	

  }

  //Änderung wird an den Server übermittelt
  checkEntry(){
    var self = this;
  	this.restService.updateRoutingEntry(this.newRoutingEntry, this.searchResultBic)
  	.subscribe(function(entry){
        self.checkmode = false;
        self.finishmode = true;
        self.oldRoutingEntry = self.newRoutingEntry;
        self.oldRoutingEntry.validFrom = moment(self.oldRoutingEntry.validFrom).format(self.momentDateFormat);
        self.oldRoutingEntry.validTo = moment(self.oldRoutingEntry.validTo).format(self.momentDateFormat);
        $('.editField').css('display','none');
    }, function(error){
            Swal('Beim Speichern ist ein Fehler aufgetreten', error.error.message, 'error')
    });
  }

  //Navigiert zurück zum Bearbeitungsmodus
  returnToEditEntry(){
  	this.editmode = true;
  	this.checkmode = false;
  	$('.editField').css('background-color', 'white');
	$(".editField").prop('disabled', false);   	
  }

  //Navigiert zurück zur Suche nach einem Institut
  returnToSearchInstitut(){
  	this.searchmode = true;
  	this.choosemode = false;
  }

  //Navigiert zurück zur Auswahl der Routingeinträgen
  returnToChooseEntry(){
  	this.choosemode = true;
  	this.editmode = false;
  	//Setzt den Radio-Button der ersten Reihe
  	setTimeout(function(){
	  	$(".radio").first().prop("checked", true);
  	});
  }

  clearChanges(){
    this.oldRoutingEntry = new RoutingEntry();
    this.newRoutingEntry = new RoutingEntry();
    this.filter = new RoutingEntry();
    this.search = {
      bic: "",
      service: " - ",
      bankOperation: " - ",
      bankOperations: BankOperations,
      services: []
    };
    this.checkmode = this.editmode = this.choosemode = false;
    this.searchmode = true;
  }

  //Datepicker wird erstellt
  setDatePicker(self: EditEntryComponent){
    var datepickerOptions = {
        autoclose: true,
        clearBtn: true,
        language: "de",
        todayHighlight: true,
        format: 'dd.mm.yyyy' 
    };
  	
  	$('#dateto').datepicker(datepickerOptions).on('changeDate', function(e) {
          self.newRoutingEntry.validTo = moment(e.date).endOf('day').format('YYYY-MM-DDTHH:mm:ssZ');
    });
  	$('#dateto').datepicker('setDate', moment(self.newRoutingEntry.validTo).format('DD.MM.YYYY'));
    $('#datefrom').datepicker(datepickerOptions).on('changeDate', function(e) {
       self.newRoutingEntry.validFrom = moment(e.date).endOf('day').format('YYYY-MM-DDTHH:mm:ssZ');
    });
    $('#datefrom').datepicker('setDate', moment(self.newRoutingEntry.validFrom).format('DD.MM.YYYY'));

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


