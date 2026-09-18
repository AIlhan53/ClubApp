import {Component, signal} from '@angular/core';
import {Navbar} from '../navbar/navbar';
import {Tournament} from '../../Interface/tournament';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {LoginService} from '../../Service/login-service';
import {TournamentService} from '../../Service/tournament-service';
import {ActivatedRoute, Router} from '@angular/router';
import Swal from 'sweetalert2';
import {CreateTournamentRequest} from '../../Interface/createTournamentRequest';
import {HttpStatusCode} from '@angular/common/http';

@Component({
  selector: 'app-form-tournament',
  imports: [
    Navbar,
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './form-tournament.html',
  styleUrl: './form-tournament.css',
  standalone: true
})
export class FormTournament {

  tournamentForm: FormGroup;

  isEditMode = signal(false);
  loading = signal(false);
  error = signal('');
  success = signal('');
  tournamentId: number = 0;
  clubId = 0;

  constructor(
    private tournamentService: TournamentService,
    private tf: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private auth: LoginService,
  ) {
    this.tournamentForm = this.tf.group({
      id: [0],
      tournamentName: ['', Validators.required],
      tournamentDescription: ['', Validators.required],
      tournamentDate: ['', Validators.required]
    });
  }

  minDate = '';

  ngOnInit(): void {
    // pour prévenir une date antérieur
    const now = new Date();
    now.setDate(now.getDate() + 1);

    this.minDate = now.toISOString().slice(0, 16);

    this.route.params.subscribe(params => {
      if (params['clubId']) {
        this.clubId = +params['clubId'];
      }

      if (params['tournamentId']) {
        this.isEditMode.set(true);
        this.tournamentId = +params['tournamentId'];
        this.loadTournament(this.tournamentId);
      }
    });
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

  private loadTournament(id: number): void {
    const token = this.getAuthToken();
    if (!token) return;

    this.loading.set(true);

    let request: Tournament = this.tournamentForm.value;

    this.tournamentService.getTournamentById(id).subscribe({
      next: (data: Tournament) => {
        request = data;
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading tournament:', err);
        this.error.set('Error loading store.');
        this.loading.set(false);
      }
    });
  }

  saveTournament(): void {

    const token = this.getAuthToken();
    if (!token) {
      return;
    }

    this.loading.set(true);
    this.error.set('');
    this.success.set('');

    const request: CreateTournamentRequest = {
      ...this.tournamentForm.value,
      clubId: this.clubId
    };

    this.tournamentService.createTournament(request).subscribe({
      next: () => {

        this.success.set(
          this.isEditMode()
            ? 'Tournament updated successfully!'
            : 'Tournament created successfully!'
        );

        this.loading.set(false);

        Swal.fire({
          icon: 'success',
          title: 'An Email has been send to all members of the club',
          showConfirmButton: false,
          timer: 1500,
          timerProgressBar: true
        }).then(() => this.router.navigate(['/responsable/club/tournament', this.clubId]));

      },
      error: (err) => {
        console.error('Full error:', err);
        const backendMsg = err.error?.error ?? err.error?.message ?? 'An unexpected error occurred.';

        if (err.status === HttpStatusCode.Forbidden) {
          Swal.fire({
            icon: 'error',
            title: 'Access denied',
            text: 'You can only update your own tournament.',
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

  reset(): void {
    this.tournamentForm.reset();
    this.error.set('');
  }

  cancel(): void {
    this.router.navigate(['/responsable/club/tournament', this.clubId]);
  }
}
