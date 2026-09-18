import {Component, computed, signal} from '@angular/core';
import {Navbar} from '../navbar/navbar';
import {Tournament} from '../../Interface/tournament';
import {ActivatedRoute, Router} from '@angular/router';
import {LoginService} from '../../Service/login-service';
import {ClubService} from '../../Service/club-service';
import {TournamentService} from '../../Service/tournament-service';
import {HttpStatusCode} from '@angular/common/http';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-member-tournament-club-list-componenet',
  imports: [
    Navbar
  ],
  templateUrl: './member-tournament-club-list-componenet.html',
  styleUrl: './member-tournament-club-list-componenet.css',
})
export class MemberTournamentClubListComponenet {
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
  ) {}

  // À ajouter dans la classe du composant.
// "tournaments" est utilisé sans parenthèses dans le template (tableau/getter classique, pas un signal),
// donc je pagine par-dessus sans modifier sa nature actuelle.

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
  cancelParticipation(tournamentId: number): void {
    const token = this.getToken();

    if (!token) {
      this.error.set('Please login first');
      this.loading.set(false);
      return;
    }

    Swal.fire({
      icon: 'warning',
      title: 'Cancel participation?',
      text: 'Are you sure you want to cancel your participation in this tournament?',
      showCancelButton: true,
      confirmButtonText: 'Yes, cancel',
      cancelButtonText: 'No',
      confirmButtonColor: '#ef4444',
      cancelButtonColor: '#6b7280',
      reverseButtons: true
    }).then((result) => {

      if (!result.isConfirmed) {
        return;
      }

      this.loading.set(true);

      this.tournamentService.quitTournament(tournamentId).subscribe({
        next: () => {
          Swal.fire({
            icon: 'success',
            title: 'Participation cancelled',
            text: 'Your participation has been cancelled successfully.',
            showConfirmButton: false,
            timer: 2000,
            timerProgressBar: true
          }).then(() => {
            window.location.reload();
          });
        },
        error: (err) => {
          console.error('Error while leaving participation', err);

          this.error.set('Error while leaving participation');
          this.loading.set(false);
        }
      });
    });
  }

  goBack(): void {
    this.router.navigate(['/my-clubs']);
  }
}
