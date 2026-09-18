import {Component, computed, signal} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {LoginService} from '../../Service/login-service';
import {ClubService} from '../../Service/club-service';
import {HttpStatusCode} from '@angular/common/http';
import {Tournament} from '../../Interface/tournament';
import {Navbar} from '../navbar/navbar';
import {TournamentService} from '../../Service/tournament-service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-club-list-tournament',
  imports: [
    Navbar,
    RouterLink
  ],
  templateUrl: './club-list-tournament.html',
  styleUrl: './club-list-tournament.css',
  standalone: true
})
export class ClubListTournament {
  tournaments: Tournament[] = [];
  clubId: number = 0;
  loading = signal(true);
  error = signal('');

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: LoginService,
    private clubService: ClubService,
    private tournamentService: TournamentService
  ) {
  }

  readonly pageSize = 6; // ajustable

  currentPage = signal(1);

  totalPages = computed(() =>
    Math.max(1, Math.ceil(this.tournaments.length / this.pageSize))
  );

  paginatedTournaments = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize;
    return this.tournaments.slice(start, start + this.pageSize);
  });

  pageNumbers = computed(() =>
    Array.from({ length: this.totalPages() }, (_, i) => i + 1)
  );

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages()) {
      this.currentPage.set(page);
    }
  }

  nextPage(): void {
    this.goToPage(this.currentPage() + 1);
  }

  previousPage(): void {
    this.goToPage(this.currentPage() - 1);
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.clubId = +id;
      this.loadTournamentInfo();
      this.loadTournament();
    }
  }

  private getToken(): string | null {
    return this.authService.getToken();
  }

  loadTournamentInfo(): void {
    const token = this.getToken();
    if (!token) return;

    this.clubService.getClubById(this.clubId).subscribe({
      next: (club) => {
        if (club.memberCount > 0) {
        }
      },
      error: (err) => console.error('Error loading club info:', err)
    });
  }

  loadTournament(): void {
    const token = this.getToken();
    if (!token) {
      this.error.set('Please login first');
      this.loading.set(false);
      return;
    }
    this.tournamentService.getTournamentByClubId(this.clubId).subscribe({
      next: (tournaments) => {
        this.tournaments = tournaments;
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading tournaments:', err);
        if (err.status === HttpStatusCode.Forbidden) {
          this.error.set('You can only view tournament of your own store.');
        } else {
          this.error.set('Error loading subscribers.');
        }
        this.loading.set(false);
      }
    });
  }

  deleteTournament(tournamentId: number): void {
    const token = this.getToken();
    if (!token) {
      this.error.set('Please login first');
      this.loading.set(false);
      return;
    }

    Swal.fire({
      icon: 'warning',
      title: 'Delete this tournament?',
      text: 'This tournament will be removed and all invitation will be cancelled',
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
      this.tournamentService.deleteTournament(tournamentId).subscribe({

        next: () => {
          Swal.fire({
            icon: 'success',
            title: 'tournament deleted',
            text: 'this tournament has been deleted.',
            showConfirmButton: false,
            timer: 2000,
            timerProgressBar: true
          }).then(() => {
            window.location.reload();
          });
        },
        error: (err) => {
          console.error('Full error:', err);
          const backendMsg = err.error?.error ?? err.error?.message ?? 'An unexpected error occurred.';

          if (err.status === HttpStatusCode.Forbidden) {
            Swal.fire({
              icon: 'error',
              title: 'Access denied',
              text: backendMsg,
              confirmButtonColor: '#ef4444'
            });
          } else {
            Swal.fire({
              icon: 'error',
              title: 'Error',
              text: backendMsg
            });
          }
        }
      });
      this.loading.set(false);
    });
  }

  goBack(): void {
    this.router.navigate(['/responsable/dashboard']);
  }
}
