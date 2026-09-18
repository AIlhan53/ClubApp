import {Component, signal} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {LoginService} from '../../Service/login-service';
import Swal from 'sweetalert2';
import {HttpStatusCode} from '@angular/common/http';
import {ClubService} from '../../Service/club-service';
import {CreateClubRequest} from '../../Interface/createClubRequest';
import {Clubs} from '../../Interface/clubs';
import {Navbar} from '../navbar/navbar';

@Component({
  selector: 'app-form-club',
  imports: [
    Navbar,
    ReactiveFormsModule
  ],
  templateUrl: './form-club.html',
  styleUrl: './form-club.css',
  standalone: true
})
export class FormClub {

  clubForm: FormGroup;

  isEditMode = signal(false);
  loading = signal(false);
  error = signal('');
  success = signal('');
  clubId = signal<number | null>(null);
  idResponsable = 0;

  constructor(
    private clubService: ClubService,
    private tf: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private auth: LoginService,
  ) {
    this.clubForm = this.tf.group({
      id: [0],
      clubName: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.clubId.set(Number(id));
      this.isEditMode.set(true);
      this.loadClub(Number(id));
    }
  }

  private getAuthToken(): string | null {
    const token = this.auth.getToken();
    if (!token || !this.auth.isTokenValid()) {
      console.error('Token missing or expired!');
      this.auth.logout();
      return null;
    }
    return token;
  }

  private loadClub(id: number): void {
    const token = this.getAuthToken();
    if (!token) return;

    this.loading.set(true);

    this.clubService.getClubById(id).subscribe({
      next: (data: Clubs) => {
        this.clubForm.patchValue({
          clubName: data.clubName
        });
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading tournament:', err);
        this.error.set('Error loading store.');
        this.loading.set(false);
      }
    });
  }

  saveClub(): void {
    const token = this.getAuthToken();
    if (!token) {
      return;
    }

    this.loading.set(true);
    this.error.set('');
    this.success.set('');

    if (!this.isEditMode()){

      const request: CreateClubRequest = {
        ...this.clubForm.value,
        idResponsable: this.idResponsable
      };
      this.clubService.createClub(request).subscribe({
        next: () => {
          this.loading.set(false);

          Swal.fire({
            icon: 'success',
            title: 'New club created',
            showConfirmButton: false,
            timer: 1500,
            timerProgressBar: true
          }).then(() => this.router.navigate(['/responsable/dashboard']));

        },
        error: (err) => {

          console.error('Full error:', err);
          const backendMsg = err.error?.error ?? err.error?.message ?? 'An unexpected error occurred.';

          if (err.status === HttpStatusCode.Forbidden) {
            Swal.fire({
              icon: 'error',
              title: 'Access denied',
              text: 'You are not allowed to access this page.',
              confirmButtonColor: '#ef4444'
            });
          } else {
            Swal.fire({
              icon: 'error',
              title: 'Error',
              text: backendMsg
            });
          }
          this.loading.set(false);
        }
      });
    } else {
      const id = this.clubId();

      if (!id) {
        this.error.set('Club ID is missing');
        this.loading.set(false);
        return;
      }

      const request: CreateClubRequest = {
        ...this.clubForm.value
      };

      this.clubService.updateClub(id, request).subscribe({
        next: () => {
          this.loading.set(false);

          Swal.fire({
            icon: 'success',
            title: 'Club updated successfully',
            showConfirmButton: false,
            timer: 1500
          }).then(() => {
            this.router.navigate(['/responsable/dashboard']);
          });
        },
        error: (err) => {
          console.error('Full error:', err);
          const backendMsg = err.error?.error ?? err.error?.message ?? 'An unexpected error occurred.';

          if (err.status === HttpStatusCode.Forbidden) {
            Swal.fire({
              icon: 'error',
              title: 'Access denied',
              text: 'You can only update your own club.',
              confirmButtonColor: '#ef4444'
            });
          } else {
            Swal.fire({
              icon: 'error',
              title: 'Error',
              text: backendMsg
            });
          }
          this.loading.set(false);
        }
      });
    }
  }

  reset(): void {
    this.clubForm.reset();
    this.error.set('');
  }

  deleteClub(idClub : number): void {
    Swal.fire({
      icon: 'warning',
      title: 'Delete this club?',
      text: 'All members will be removed from the club and all tournaments will be deleted',
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

      this.clubService.deleteClub(idClub).subscribe({
        next: () => {
          Swal.fire({
            icon: 'success',
            title: 'club deleted',
            text: 'Your club has been deleted successfully.',
            showConfirmButton: false,
            timer: 2000,
            timerProgressBar: true
          }).then(() => {this.router.navigate(['/responsable/dashboard'])
          });
        },
        error: (err) => {
          console.error('Full error:', err);
          const backendMsg = err.error?.error ?? err.error?.message ?? 'An unexpected error occurred.';

          if (err.status === HttpStatusCode.Forbidden) {
            Swal.fire({
              icon: 'error',
              title: 'Access denied',
              text: 'You are not allowed to delete this club.',
              confirmButtonColor: '#ef4444'
            });
          } else {
            Swal.fire({
              icon: 'error',
              title: 'Error',
              text: backendMsg
            });
          }
          this.loading.set(false);
        }
      });
    });
  }

  cancel(): void {
    this.router.navigate(['/responsable/dashboard']);
  }
}
