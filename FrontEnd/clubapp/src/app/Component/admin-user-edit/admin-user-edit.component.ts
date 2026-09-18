import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from "@angular/forms";
import { Location } from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {Navbar} from '../navbar/navbar';
import {User} from '../../Interface/user';
import {LoginService} from '../../Service/login-service';
import {GestionUserService} from '../../Service/gestion-user.service';
import {RoleTypeEnum} from '../../Interface/enumRoleType';
import Swal from 'sweetalert2';
import {delay} from 'rxjs';
/**
 * Component for admin to edit user profiles.
 * Allows viewing and editing user information, and activating/deactivating users.
 *
 * @author ClubApp Team
 */
@Component({
  selector: 'app-admin-user-edit',
  imports: [
    FormsModule,
    ReactiveFormsModule,
    Navbar
  ],
  templateUrl: './admin-user-edit.component.html',
  standalone: true,
  styleUrl: './admin-user-edit.component.css'
})
export class AdminUserEditComponent implements OnInit {
  userForm: FormGroup;
  currentUser = signal<User | null>(null);
  loading = signal(true);
  editing = signal(false);
  successMessage = signal('');
  errorMessage = signal('');


  // Liste des rôles disponibles
  availableRoles = signal<string[]>(['MEMBRE', 'RESPONSABLE', 'ADMIN']);
  selectedRole = signal<string>('');

  constructor(
    private fb: FormBuilder,
    private userService: GestionUserService,
    private authService: LoginService,
    private route: ActivatedRoute,
    private location: Location,
    private router : Router
  ) {
    this.userForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: [{value: ''}],
      role: ['']
    });
  }

  ngOnInit(): void {

    const idFromRoute = this.route.snapshot.paramMap.get('id');

    if (!idFromRoute) {
      this.errorMessage.set("User ID missing from URL");
      return;
    }

    const targetUserId = Number(idFromRoute);
    const authUser = this.authService.currentUser();


    if (authUser && authUser.id === targetUserId) {
      this.router.navigate(
        ['/admin/gestion'],
        { queryParams: { from: 'admin-edit' } }
      );
      return;
    }

    this.loadUserById(targetUserId);
  }

  isTargetAdmin(): boolean {
    const user = this.currentUser();
    if (!user || !user.roleName) return false;

    return user.roleName == RoleTypeEnum.ADMIN;
  }

  isEditingSelf(): boolean {
    const user = this.currentUser();
    const authUser = this.authService.currentUser(); // utilisateur connecté

    if (!user || !authUser) return false;

    return user.id === authUser.id;
  }

  canEditUser(): boolean {
    // Un admin peut modifier :
    // - lui-même
    // - un non-admin
    if (!this.isTargetAdmin()) return true;

    return this.isEditingSelf();
  }

  /**
   * Loads user data by ID.
   */
  loadUserById(id: number): void {
    this.loading.set(true);

    this.userService.getUserById(id).subscribe({
      next: (user) => {
        this.currentUser.set(user);

        // Initialiser les rôles sélectionnés
        this.selectedRole.set(user.roleName);

        this.userForm.patchValue({
          firstName: user.firstName,
          lastName: user.lastName,
          email: user.email,
        });

        this.loading.set(false);
      },
      error: (error) => {
        this.errorMessage.set('Unable to load this user');
        this.loading.set(false);
        console.error(error);
      }
    });
  }

  /**
   * Toggles between edit and view mode.
   */
  toggleEdit(): void {

    if (!this.canEditUser()) {
      this.errorMessage.set("You cannot edit another administrator");
      setTimeout(() => this.errorMessage.set(''), 3000);
      return;
    }

    this.editing.update(val => !val);

    if (!this.editing()) {
      const user = this.currentUser();
      if (user) {
        this.userForm.patchValue({
          firstName: user.firstName,
          lastName: user.lastName
        });
        // Réinitialiser le rôle
        this.selectedRole.set(user.roleName);
      }
    }
  }

  /**
   * Selection of role for the admin by checkboxes
   * @param roleName
   * @param event
   */
  onRoleToggle(roleName: string, event: Event): void {
    const checkbox = event.target as HTMLInputElement;

    if (checkbox.checked) {
      // Un seul rôle peut être sélectionné
      this.selectedRole.set(roleName);
      return;
    }

    // Empêcher de supprimer le dernier rôle
    checkbox.checked = true;

    this.errorMessage.set('User must have at least one role');

    setTimeout(() => {
      this.errorMessage.set('');
    }, 3000);
  }

  /**
   * Submits the updated user data.
   */
  onSubmit(): void {
    if (this.userForm.valid && this.currentUser()) {

      //Validation : au moins un rôle doit être sélectionné
      if (this.selectedRole().length === 0) {
        this.errorMessage.set("User must have at least one role");
        setTimeout(() => this.errorMessage.set(''), 3000);
        return;
      }

      const userId = this.currentUser()!.id;

      const updatedData: any = {
        ...this.currentUser(),
        firstName: this.userForm.value.firstName,
        lastName: this.userForm.value.lastName,
        role: this.selectedRole()
      };

      this.userService.updateUser(userId, updatedData).subscribe({
        next: (user) => {
          this.currentUser.set(user);
          this.editing.set(false);
          this.successMessage.set("Profile updated successfully");

          // Mettre à jour les rôles sélectionnés
          this.selectedRole.set(user.roleName);

          setTimeout(() => this.successMessage.set(''), 3000);
        },
        error: (error) => {
          const backendMsg = error.error?.error ?? error.error?.message ?? 'An unexpected error occurred.';
          this.errorMessage.set(backendMsg);
          console.error(backendMsg);
          setTimeout(() => this.errorMessage.set(''), 3000);
        }
      });
    }
  }

  /**
   * Activates or deactivates the user.
   */
  quitOtherOrOther(): void {
    if (!this.canEditUser()) {
      this.errorMessage.set("You cannot deactivate another administrator");
      setTimeout(() => this.errorMessage.set(''), 3000);
      return;
    }

    const user = this.currentUser();

    if (!user) {
      this.errorMessage.set('User not found');
      setTimeout(() => this.errorMessage.set(''), 3000);
      return;
    }

    if (!user.active) {
      this.userService.activateOther(user.id).subscribe({

        next: () => {
          this.successMessage.set('User activated successfully');
          user.active = true;
          this.currentUser.set({ ...user });
          setTimeout(() => this.successMessage.set(''), 3000);
        },
        error: (err) => {
          console.error(err);
          this.errorMessage.set('Error activating user');
          setTimeout(() => this.errorMessage.set(''), 3000);
        }
      });
    } else {
      this.userService.deactivateOther(user.id).subscribe({
        next: () => {
          this.successMessage.set('User deactivated successfully');
          user.active = false;
          this.currentUser.set({ ...user });
          setTimeout(() => this.successMessage.set(''), 3000);
        },
        error: (err) => {
          console.error(err);
          this.errorMessage.set('Error deactivating user');
          setTimeout(() => this.errorMessage.set(''), 3000);
        }
      });
    }
  }
  /**
   * Navigates back to the previous page.
   */
  goBack(): void {
    this.location.back();
  }

  protected readonly delay = delay;
}
