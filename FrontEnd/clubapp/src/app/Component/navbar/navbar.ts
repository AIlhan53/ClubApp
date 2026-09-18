import {Component, effect, OnInit} from '@angular/core';
import {RoleTypeEnum} from '../../Interface/enumRoleType';
import {LoginService} from '../../Service/login-service';
import {Router, RouterLink, RouterLinkActive} from '@angular/router';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
  standalone: true
})
export class Navbar implements OnInit {

  isMenuOpen = false;
  selectedRole: RoleTypeEnum | null = null;

  constructor(
    public authService: LoginService,
    private router: Router
  ) {
    // Effect to log role changes (for debugging)
    effect(() => {
      const role = this.authService.currentRole();
      this.selectedRole = role;
    });
  }

  ngOnInit(): void {
    this.initializeRole();
  }

  private initializeRole(): void {
    const currentRole = this.authService.currentRole();

    // If no role is set, set default based on user's roles
    if (!currentRole) {
      const user = this.authService.getCurrentUser();
      if (user && user.roleName) {
        if (user.roleName === RoleTypeEnum.ADMIN) {
          this.authService.setRole(RoleTypeEnum.ADMIN);
        } else if (user.roleName === RoleTypeEnum.RESPONSABLE) {
          this.authService.setRole(RoleTypeEnum.RESPONSABLE);
        } else {
          this.authService.setRole(RoleTypeEnum.MEMBRE);
        }
      }
    }
  }

  toggleMenu(): void {
    this.isMenuOpen = !this.isMenuOpen;

    if (this.isMenuOpen) {
      document.body.classList.add('menu-open');
    } else {
      document.body.classList.remove('menu-open');
    }
  }

  closeMenu(): void {
    this.isMenuOpen = false;
    document.body.classList.remove('menu-open');
  }

  // Use the signal directly - this will be reactive
  get currentRole(): RoleTypeEnum | null {
    return this.authService.currentRole();
  }

  get isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }

  get userName(): string {
    const user = this.authService.getCurrentUser();
    return user ? `${user.firstName} ${user.lastName}` : '';
  }

  logout(): void {
    this.closeMenu();
    this.authService.logout();
  }

  protected readonly RoleTypeEnum = RoleTypeEnum;
}
