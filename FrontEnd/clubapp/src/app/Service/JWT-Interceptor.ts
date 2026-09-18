import {inject} from '@angular/core';
import {HttpInterceptorFn} from '@angular/common/http';
import {LoginService} from './login-service';


export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(LoginService);
  const token = authService.getToken();

  // Ne pas ajouter le token pour la requête de login
  if (req.url.includes('/login')) {
    return next(req);
  }

  // Ajouter le token Bearer pour les autres requêtes
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req);
};
