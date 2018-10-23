import { Injectable } from '@angular/core';

@Injectable()
export class HelpService {

  constructor() { }

  SetContentHight(){
  	  $('.content-wrapper').css("min-height", $(window).height()-101);
  }

}
