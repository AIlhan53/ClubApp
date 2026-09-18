import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListInvitationComponent } from './list-invitation-component';

describe('ListInvitationComponent', () => {
  let component: ListInvitationComponent;
  let fixture: ComponentFixture<ListInvitationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListInvitationComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ListInvitationComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
