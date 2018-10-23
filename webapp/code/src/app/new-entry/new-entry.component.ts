import { Component, OnInit } from '@angular/core';
import { Services } from '../models/services';
import { BankOperationClass } from '../models/bankOperation';
import { BankOperations } from '../models/bankOperation';
import { BankOperation } from '../models/bankOperation';
import { ServiceClass } from '../models/services';
import { RoutingEntry } from '../models/routingEntry';
import { RestService } from '../services/rest.service';
import { BIC } from '../models/bic';
import { Bank } from '../models/bank';
import { HelpService } from '../services/help.service';




import { } from 'jquery';
import { } from 'bootstrap-datepicker';
import * as moment from 'moment';
import Swal from 'sweetalert2';



@Component({
  selector: 'app-new-entry',
  templateUrl: './new-entry.component.html',
  styleUrls: ['./new-entry.component.css']
})
export class NewEntryComponent implements OnInit { 
    //Variables for dropdown
    bankOperations: BankOperationClass[];
    selectedBankOperation: BankOperationClass;
    services: ServiceClass[];
    selectedService: ServiceClass;
    routingEntry: RoutingEntry;
    bankName: string;
    bankBic: BIC;

    //Processmodes
    checkmode: boolean;
    finishmode: boolean;

  constructor(private restService: RestService, private helpService: HelpService) { }

  ngOnInit() {   
    this.fillData();
  	this.checkmode = this.finishmode = false;
  	this.setDatePicker(this);
  	this.returnToEdit();
    this.helpService.SetContentHight();

  }

  //Setzt die Werte in die Dropdowns
  fillData(){
    this.routingEntry = new RoutingEntry();
  	this.bankOperations = BankOperations;
  	this.selectedBankOperation = this.bankOperations[0];
  	this.services = this.selectedBankOperation.services;
  	this.selectedService = this.services[0];
  	this.routingEntry.bankOperation = BankOperation[this.selectedBankOperation.name];
  	this.routingEntry.service = this.selectedService.name;
    this.routingEntry.counterPartyBic = new BIC();
    this.routingEntry.cooperationBic = new BIC();
  }

  //Wird beim Auswählen einer Bankoperation ausgeführt
  changeBankOperation(operation){
  	this.selectedBankOperation = operation;
  	this.services = this.selectedBankOperation.services;
  	this.selectedService = this.services[0];
  	this.routingEntry.bankOperation = BankOperation[this.selectedBankOperation.name];
  	this.routingEntry.service = this.selectedService.name;
  }

  //Wird beim Auswählen eines Services ausgeführt
  changeService(service){
  	this.selectedService = service;
  	this.routingEntry.service = this.selectedService.name;
  }

  //Speichert den neuen Routingeintrag. Eintrag wird an Server geschickt
  saveEntry(){
    var self = this;

    //Überprüft ob die Bank existiert, wenn nein, wird die Bank angelegt
    this.restService.getBank(this.bankBic.toString())
    .subscribe(function(result: any){
       self.addRoutingEntry(self);
    },function(error){
       var bank = new Bank();
       bank.name = self.bankName;
       bank.bic = self.bankBic;
       self.restService.addBank(bank).subscribe(function(result){
          self.addRoutingEntry(self);
        }, function(error){
          Swal('Beim Speichern ist ein Fehler aufgetreten', error.error.message, 'error')
        });
    });

    
  }

  addRoutingEntry(self: any){
    self.restService.saveRoutingEntry(self.routingEntry, self.bankBic)
        .subscribe(function(result){
            self.finishmode = true;
        }, function(error){
          Swal('Beim Speichern ist ein Fehler aufgetreten', error.error.message, 'error')
        });
  }

  //Deaktiviert die Eingabefelder
  checkEntry(){
  	this.checkmode = true;
    $('.editField').css('background-color', '#eeeeee');
    $(".editField").prop('disabled', true);   

  }

  //Eingabefelder werden wieder bearbeitbar gemacht
  returnToEdit(){
  	this.checkmode = false;
    $('.editField').css('background-color', 'white');
    $(".editField").prop('disabled', false);   

  }

  clearChanges(){
    this.returnToEdit();
    this.fillData();
    this.bankName = "";
    this.bankBic = new BIC();
    $('#datefrom').val("");
    $('#dateto').val("");
  }

  //Datepicker werden gesetzt
  setDatePicker(self: NewEntryComponent){
    var datepickerOptions = {
        autoclose: true,
        clearBtn: true,
        language: "de",
        todayHighlight: true,
        format: 'dd.mm.yyyy'
    };
  	
	 $('#dateto').datepicker(datepickerOptions).on('changeDate', function(e) {
       self.routingEntry.validTo = moment(e.date).endOf('day').format('YYYY-MM-DDTHH:mm:ssZ');
       
    });
     $('#datefrom').datepicker(datepickerOptions).on('changeDate', function(e) {
       self.routingEntry.validFrom = moment(e.date).endOf('day').format('YYYY-MM-DDTHH:mm:ssZ');
    });;
  }
}
