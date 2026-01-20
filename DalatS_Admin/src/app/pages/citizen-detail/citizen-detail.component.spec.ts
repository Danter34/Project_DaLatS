import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CitizenDetailComponent } from './citizen-detail.component';

describe('CitizenDetailComponent', () => {
  let component: CitizenDetailComponent;
  let fixture: ComponentFixture<CitizenDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CitizenDetailComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CitizenDetailComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
