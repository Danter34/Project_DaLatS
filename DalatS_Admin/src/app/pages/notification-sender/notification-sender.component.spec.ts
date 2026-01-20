import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NotificationSenderComponent } from './notification-sender.component';

describe('NotificationSenderComponent', () => {
  let component: NotificationSenderComponent;
  let fixture: ComponentFixture<NotificationSenderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NotificationSenderComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NotificationSenderComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
