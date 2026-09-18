import {Component, OnInit, signal} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {CommonModule, Location} from '@angular/common';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {Navbar} from '../navbar/navbar';
import {User} from '../../Interface/user';
import {LoginService} from '../../Service/login-service';
import {GestionUserService} from '../../Service/gestion-user.service';

/**
 * Component for managing user profile.
 * Allows users to view and edit their personal information.
 *
 * @author ClubApp Team
 */
@Component({
  selector: 'app-gestion-user',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    Navbar
  ],
  templateUrl: './gestion.component.html',
  styleUrl: './gestion.component.css'
})
export class GestionComponent implements OnInit {
  userForm: FormGroup;
  currentUser = signal<User | null>(null);
  loading = signal(true);
  editing = signal(false);
  successMessage = signal('');
  errorMessage = signal('');
  showPassword = signal(false);

  constructor(
    private fb: FormBuilder,
    private userService: GestionUserService,
    private authService: LoginService,
    private router: Router,
    private route: ActivatedRoute
  ) {

    this.userForm = this.fb.group({
      firstName: [''],
      lastName: [''],
      email: ['', [Validators.email]],
      password: ['', [
        Validators.minLength(6),
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@#$%^&+=!?]).+$/)
      ]],
      role: ['']
    });
  }

  ngOnInit(): void {
    this.loadCurrentUser();
  }

  /**
   * Loads the current user's profile from the server.
   */
  loadCurrentUser(): void {
    this.loading.set(true);
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUser.set(user);
        this.userForm.patchValue({
          firstName: user.firstName,
          lastName: user.lastName,
          email: user.email,
          role: user.roleName
        });
        this.loading.set(false);
      },
      error: (error) => {
        this.errorMessage.set('Error loading profile');
        this.loading.set(false);
        console.error(error);
      }
    });
  }

  /**
   * Toggles between edit and view mode.
   * Resets form values when canceling edit.
   */
  toggleEdit(): void {
    this.editing.update(v => !v);

    if (!this.editing()) {
      const user = this.currentUser();
      if (!user) return;

      this.userForm.reset({
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        role: user.roleName,
        password: ''
      });

      this.showPassword.set(false);
    }
  }

  /**
   * Toggles password visibility.
   */
  togglePasswordVisibility(): void {
    this.showPassword.set(!this.showPassword());
  }

  /**
   * Submits the updated user profile.
   */
  onSubmit(): void {
    if (this.userForm.valid && this.currentUser()) {
      const userId = this.currentUser()!.id;

      const updatedData: any = {
        firstName: this.userForm.value.firstName,
        lastName: this.userForm.value.lastName,
        email: this.userForm.value.email
      };

      if (this.userForm.value.password?.trim() !== '') {
        updatedData.password = this.userForm.value.password;
      }
      const oldEmail = this.currentUser()?.email;

      this.userService.updateUser(userId, updatedData).subscribe({
        next: (user) => {
          const emailChanged = oldEmail !== user.email;

          this.currentUser.set(user);
          this.editing.set(false);
          this.successMessage.set("Profile updated successfully");
          this.userForm.patchValue({password: ''});
          this.showPassword.set(false);

          if (emailChanged) {
            this.successMessage.set("Profile updated successfully. Please log again");
            setTimeout(() => {this.authService.logout();}, 2000);
            this.loading.set(true);
            return;
          }
          else {
            this.successMessage.set("Profile updated successfully");

            setTimeout(() => this.successMessage.set(''), 2000);
          }

          this.loading.set(false);
        },
        error: (error) => {
          this.errorMessage.set("Error updating profile");
          console.error(error);
          setTimeout(() => this.errorMessage.set(''), 2000);
        }
      });
    }
  }

  /**
   * Returns formatted role names without the ROLE_ prefix.
   */
  getRoleName(): string {
    const user = this.currentUser();
    if (!user || !user.roleName) return '';
    return user.roleName;
  }

  /**
   * Logs out the current user.
   */
  quit(): void {
    this.authService.logout();
  }

  /**
   * Navigates back to the previous page.
   */
  goBack(): void {
    const from = this.route.snapshot.queryParamMap.get('from');

    switch (from) {
      case 'admin-edit':
        this.router.navigate(['/admin/users']);
        break;
      default:
        this.router.navigate(['/dashboard']);
    }
  }
}
