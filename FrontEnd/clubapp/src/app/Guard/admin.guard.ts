import { CanActivateFn, Router } from "@angular/router";

import { inject } from "@angular/core";
import {LoginService} from '../Service/login-service';

export const adminGuard: CanActivateFn = () => {

  const router = inject(Router);
  const authService = inject(LoginService);

  if (!authService.isAuthenticated()) {
    router.navigate(['/login']);
    return false;
  }

  if (authService.isAdmin() && authService.isTokenValid()) {
    return true;
  }

  router.navigate(['/Denied']);
  return false;
};

