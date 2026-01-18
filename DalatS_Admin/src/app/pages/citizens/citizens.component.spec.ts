import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CitizensComponent } from './citizens.component';

describe('CitizensComponent', () => {
  let component: CitizensComponent;
  let fixture: ComponentFixture<CitizensComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CitizensComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CitizensComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
