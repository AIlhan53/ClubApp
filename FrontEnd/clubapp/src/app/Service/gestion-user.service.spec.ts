import { TestBed } from '@angular/core/testing';

import { GestionUserService } from './gestion-user.service';

describe('GestionUserService', () => {
  let service: GestionUserService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(GestionUserService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
