import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import {LoginService} from '../Service/login-service';
import {RoleTypeEnum} from '../Interface/enumRoleType';

export const managerGuard: CanActivateFn = (route, state) => {
  const authService = inject(LoginService);
  const router = inject(Router);

  const token = authService.getToken();

  if (!token || !authService.isTokenValid()) {
    router.navigate(['/login']);
    return false;
  }

  // Check if user HAS the MANAGER role (not the selected role)
  const user = authService.getCurrentUser();
  const hasManagerRole = user?.roleName === RoleTypeEnum.RESPONSABLE || user?.roleName === RoleTypeEnum.ADMIN;

  if (hasManagerRole) {
    return true;
  }

  router.navigate(['/unauthorized']);
  return false;
};
