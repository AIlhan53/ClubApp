import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClubListTournament } from './club-list-tournament';

describe('ClubListTournament', () => {
  let component: ClubListTournament;
  let fixture: ComponentFixture<ClubListTournament>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClubListTournament],
    }).compileComponents();

    fixture = TestBed.createComponent(ClubListTournament);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
