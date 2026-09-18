import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { memberGuardGuard } from './member-guard-guard';

describe('memberGuardGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) =>
    TestBed.runInInjectionContext(() => memberGuardGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
