import { TestBed } from '@angular/core/testing';

import { InvitationTournamentService } from './invitation-tournament-service';

describe('InvitationTournamentService', () => {
  let service: InvitationTournamentService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(InvitationTournamentService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
