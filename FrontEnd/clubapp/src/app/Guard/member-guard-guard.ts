import {CanActivateFn, Router} from '@angular/router';
import {inject} from '@angular/core';
import {LoginService} from '../Service/login-service';

export const memberGuardGuard: CanActivateFn = (route, state) => {
  const loginService = inject(LoginService);
  const router = inject(Router);
  const token = loginService.getToken();

  if (!token || !loginService.isTokenValid()) {
    router.navigate(['/login']);
    return false;
  }

  return true;
};
