import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VerifyResultComponent } from './verify-result.component';

describe('VerifyResultComponent', () => {
  let component: VerifyResultComponent;
  let fixture: ComponentFixture<VerifyResultComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [VerifyResultComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VerifyResultComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
