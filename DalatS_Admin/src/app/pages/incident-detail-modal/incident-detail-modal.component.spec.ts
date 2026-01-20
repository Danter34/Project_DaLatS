import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IncidentDetailModalComponent } from './incident-detail-modal.component';

describe('IncidentDetailModalComponent', () => {
  let component: IncidentDetailModalComponent;
  let fixture: ComponentFixture<IncidentDetailModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [IncidentDetailModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(IncidentDetailModalComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
