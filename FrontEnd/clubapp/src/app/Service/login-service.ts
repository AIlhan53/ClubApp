import {Injectable, signal, WritableSignal} from '@angular/core';
import {environment} from '../../environement/environement';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Router} from '@angular/router';
import {firstValueFrom, Observable, tap} from 'rxjs';
import {JWT} from '../Interface/JWT';
import {User} from '../Interface/user';
import {RoleTypeEnum} from '../Interface/enumRoleType';

@Injectable({
  providedIn: 'root',
})

export class LoginService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private apiUrlUser =  `${environment.apiUrl}/user`;

  private readonly TOKEN_KEY = 'auth_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly USER_KEY = 'current_user';
  private readonly SELECTED_ROLE_KEY = 'selected_role';

  isAuthenticated: WritableSignal<boolean> = signal(this.hasToken());
  currentUser: WritableSignal<User | null> = signal(this.getUserFromStorage());
  currentRole: WritableSignal<RoleTypeEnum | null> = signal(this.getSelectedRoleFromStorage());

  constructor(private http: HttpClient, private router: Router) {
  }

  login(credentials: { email: string; password: string }): Observable<JWT> {
    const body = new URLSearchParams();
    body.set('email', credentials.email);
    body.set('password', credentials.password);

    return this.http.post<JWT>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
        localStorage.setItem(this.TOKEN_KEY, response.accessToken);
        localStorage.setItem(this.REFRESH_TOKEN_KEY, response.refreshToken);
        this.isAuthenticated.set(true);
      })
    );
  }

  async loadCurrentUser(): Promise<User | null> {
    try {
      const user = await firstValueFrom(this.http.get<User>(`${this.apiUrlUser}/me`));
      localStorage.setItem(this.USER_KEY, JSON.stringify(user));
      this.currentUser.set(user);
      this.setDefaultRole(user);
      this.isAuthenticated.set(true);
      return user;
    } catch (error) {
      this.isAuthenticated.set(false);
      this.currentRole.set(null);
      return null;
    }
  }

  activateAccount(token: string): Observable<void> {
    return this.http.get<void>(
      `${this.apiUrl}/activate?token=${encodeURIComponent(token)}`
    );
  }

  // ---------------------------
  // LOGOUT
  // ---------------------------
  logout(): void {
    this.http.post(`${this.apiUrl}/logout`, {}).subscribe({
      next: () => this.clearSession(),
      error: () => this.clearSession()
    });
  }

  private clearSession(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    localStorage.removeItem(this.SELECTED_ROLE_KEY);

    this.isAuthenticated.set(false);
    this.currentUser.set(null);
    this.currentRole.set(null);

    this.router.navigate(['/login']);
  }

  private setDefaultRole(user: User): void {
    if (user.roleName === 'ADMIN') {
      this.setRole(RoleTypeEnum.ADMIN);
    } else if (user.roleName === 'RESPONSABLE') {
      this.setRole(RoleTypeEnum.RESPONSABLE);
    } else {
      this.setRole(RoleTypeEnum.MEMBRE);
    }
  }

  getAvailableRoles(): RoleTypeEnum[] {
    return Object.values(RoleTypeEnum);
  }

  getCurrentUser(): User | null {
    return this.currentUser();
  }

  getCurrentRole(): RoleTypeEnum | null {
    return this.currentRole();
  }

  setRole(role: RoleTypeEnum): void {
    this.currentRole.set(role);
    localStorage.setItem(this.SELECTED_ROLE_KEY, role);
  }

  private getSelectedRoleFromStorage(): RoleTypeEnum | null {
    const role = localStorage.getItem(this.SELECTED_ROLE_KEY);
    if (role && Object.values(RoleTypeEnum).includes(role as RoleTypeEnum)) {
      return role as RoleTypeEnum;
    }
    return null;
  }

  private getUserFromStorage(): User | null {
    try {
      const user = localStorage.getItem(this.USER_KEY);
      return user ? JSON.parse(user) : null;
    } catch {
      return null;
    }
  }

  hasRole(role: RoleTypeEnum): boolean {
    const user = this.currentUser();
    if (!user || !user.roleName) return false;

    return user.roleName === role;
  }

  isAdmin(): boolean { return this.hasRole(RoleTypeEnum.ADMIN); }
  isResponsable(): boolean { return this.hasRole(RoleTypeEnum.RESPONSABLE); }
  isMembre(): boolean { return this.hasRole(RoleTypeEnum.MEMBRE); }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  isTokenValid(): boolean {
    const token = this.getToken();
    const user = this.currentUser();

    if (!token || !user) return false;
    else if(!user?.active){
      return false
    }
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  private hasToken(): boolean {
    return !!localStorage.getItem(this.TOKEN_KEY);
  }
}
