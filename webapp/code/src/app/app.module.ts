import { AppRoutingModule } from './app-routing.module';
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClientModule }    from '@angular/common/http';
import { DataTablesModule } from 'angular-datatables';



import { AppComponent } from './app.component';
import { CoreComponent } from './core/core.component';
import { HeaderComponent } from './core/header/header.component';
import { LeftSideComponent } from './core/left-side/left-side.component';
import { FooterComponent } from './core/footer/footer.component';
import { ControlSidebarComponent } from './core/control-sidebar/control-sidebar.component';
import { NewEntryComponent } from './new-entry/new-entry.component';
import { HomeComponent } from './home/home.component';
import { EditEntryComponent } from './edit-entry/edit-entry.component';
import { DeleteEntryComponent } from './delete-entry/delete-entry.component';
import { ExtractEntryComponent } from './extract-entry/extract-entry.component';

import { RestService } from './services/rest.service';
import { HelpService } from './services/help.service';
import { RoutingEntryFilterPipe } from './shared/routing-entry-filter.pipe';



@NgModule({
  declarations: [
    AppComponent,
    CoreComponent,
    HeaderComponent,
    LeftSideComponent,
    FooterComponent,
    ControlSidebarComponent,
    NewEntryComponent,
    HomeComponent,
    EditEntryComponent,
    DeleteEntryComponent,
    ExtractEntryComponent,
    RoutingEntryFilterPipe
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    DataTablesModule
  ],
  providers: [RestService, HelpService],
  bootstrap: [AppComponent]
})
export class AppModule { }
