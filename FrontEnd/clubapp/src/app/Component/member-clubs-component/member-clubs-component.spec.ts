import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MemberClubsComponent } from './member-clubs-component';

describe('MemberClubsComponent', () => {
  let component: MemberClubsComponent;
  let fixture: ComponentFixture<MemberClubsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MemberClubsComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(MemberClubsComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
