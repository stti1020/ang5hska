import { CoreComponent } from './core/core.component';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NewEntryComponent } from './new-entry/new-entry.component';
import { HomeComponent } from './home/home.component';
import { EditEntryComponent } from './edit-entry/edit-entry.component';
import { DeleteEntryComponent } from './delete-entry/delete-entry.component';
import { ExtractEntryComponent } from './extract-entry/extract-entry.component';



@NgModule({
  imports: [
    RouterModule.forRoot([
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      { path: 'home', component: HomeComponent },
      { path: 'newentry', component: NewEntryComponent },
      { path: 'editentry', component: EditEntryComponent },
      { path: 'deleteentry', component: DeleteEntryComponent },
      { path: 'extractentry', component: ExtractEntryComponent }
    ])
  ],
  declarations: [],
  exports: [ RouterModule]
})
export class AppRoutingModule { }
