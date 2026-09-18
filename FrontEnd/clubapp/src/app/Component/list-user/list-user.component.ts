import {Component, computed, signal} from '@angular/core';

import {CommonModule} from '@angular/common';
import {Router} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {Navbar} from '../navbar/navbar';
import {User} from '../../Interface/user';
import {GestionUserService} from '../../Service/gestion-user.service';
import {LoginService} from '../../Service/login-service';
import {RoleTypeEnum} from '../../Interface/enumRoleType';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-list-user',
  imports: [CommonModule, FormsModule, Navbar],
  templateUrl: './list-user.component.html',
  styleUrl: './list-user.component.css'
})
export class ListUserComponent {
  users = signal<User[]>([]);
  loading = signal(true);
  errorMessage = signal('');
  searchTerm = signal('');
  error = signal('');

  constructor(
    private userService: GestionUserService,
    private authService: LoginService,
    private router: Router) {
  }

  readonly pageSize = 6;
  currentPage = signal(1);

  totalPages = computed(() => Math.max(1, Math.ceil(this.filteredUsers.length / this.pageSize)));

  paginatedUsers = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize;
    return this.filteredUsers.slice(start, start + this.pageSize);
  });

  pageNumbers = computed(() => Array.from({ length: this.totalPages() }, (_, i) => i + 1));

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages()) this.currentPage.set(page);
  }
  nextPage(): void { this.goToPage(this.currentPage() + 1); }
  previousPage(): void { this.goToPage(this.currentPage() - 1); }


  ngOnInit(): void {
    this.loadUsers();
  }

  isAdmin(user: User): boolean {
    return user.roleName === RoleTypeEnum.ADMIN;
  }

  loadUsers(): void {
    this.loading.set(true);
    this.userService.getAllUsers().subscribe({
      next: (data) => {
        this.users.set(data);
        this.loading.set(false);
      },
      error: (error) => {
        this.errorMessage.set('Error while loading users');
        this.loading.set(false);
        console.error(error);
      }
    });
  }

  deleteUser(id: number): void {
    Swal.fire({
      icon: 'warning',
      title: 'Anonymize this user?',
      text: 'The user will be anonymize, he won\'t be able to connect to the server. ' +
        'When a user is deactivated, you can delete it by clicking on his account.',
      showCancelButton: true,
      confirmButtonText: 'Yes',
      cancelButtonText: 'No',
      confirmButtonColor: '#ef4444',
      cancelButtonColor: '#6b7280'
    }).then((result) => {

      if (!result.isConfirmed) {
        return;
      }

      this.loading.set(true);

      this.userService.deleteUser(id).subscribe({
        next: () => {
          Swal.fire({
            icon: 'success',
            title: 'User deleted',
            text: 'This user has been deleted.',
            showConfirmButton: false,
            timer: 2000,
            timerProgressBar: true
          }).then(() => {
            window.location.reload();
          });
        },
        error: (err) => {
          const backendMsg = err.error?.error ?? err.error?.message ?? 'An unexpected error occurred.';
          Swal.fire({
            icon: 'error',
            title: 'Error',
            text: backendMsg
          });
        }
      });
      this.loading.set(false);
    });
  }

  getRoleNames(user: User): string {
    return user.roleName;
  }

  get filteredUsers() {
    const term = this.searchTerm().toLowerCase();
    if (!term) return this.users();

    return this.users().filter(user =>
      user.firstName.toLowerCase().includes(term) ||
      user.lastName.toLowerCase().includes(term) ||
      user.email.toLowerCase().includes(term)
    );
  }

  viewUser(userId: number): void {
    const currentUser = this.authService.currentUser();
    if (userId == currentUser?.id) {
      this.router.navigate(['/profile-user']);
    } else {
      this.router.navigate(['/admin/users/edit', userId]
        , {queryParams: {from: 'admin-list'}});
    }
  }
}
