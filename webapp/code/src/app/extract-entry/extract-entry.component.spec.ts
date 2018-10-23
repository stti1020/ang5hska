import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ExtractEntryComponent } from './extract-entry.component';

describe('ExtractEntryComponent', () => {
  let component: ExtractEntryComponent;
  let fixture: ComponentFixture<ExtractEntryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ExtractEntryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExtractEntryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
