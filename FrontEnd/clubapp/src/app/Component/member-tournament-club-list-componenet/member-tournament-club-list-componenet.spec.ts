import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MemberTournamentClubListComponenet } from './member-tournament-club-list-componenet';

describe('MemberTournamentClubListComponenet', () => {
  let component: MemberTournamentClubListComponenet;
  let fixture: ComponentFixture<MemberTournamentClubListComponenet>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MemberTournamentClubListComponenet],
    }).compileComponents();

    fixture = TestBed.createComponent(MemberTournamentClubListComponenet);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
