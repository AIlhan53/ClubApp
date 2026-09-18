import {Component, computed, OnInit, signal} from '@angular/core';
import {Navbar} from '../navbar/navbar';
import {TournamentService} from '../../Service/tournament-service';
import {LoginService} from '../../Service/login-service';
import {Tournament} from '../../Interface/tournament';

@Component({
  selector: 'app-tournament-list-component',
  imports: [
    Navbar
  ],
  templateUrl: './tournament-list-component.html',
  styleUrl: './tournament-list-component.css',
})
export class TournamentListComponent implements OnInit {
  tournaments = signal<Tournament[]>([]);
  loading = signal(true);
  error = signal('');

  constructor(private tournamentService: TournamentService,
              private loginService: LoginService) {
  }


  readonly pageSize = 6;

  currentPage = signal(1);


  ngOnInit() {
  this.loadData();
  }

  private getToken(): string | null {
    return this.loginService.getToken();
  }

  private loadData(): void {
    const token = this.getToken();
    if (!token) {
      this.error.set('Please login first');
      this.loading.set(false);
      return;
    }

    this.loadTournaments();
  }
  /**
   * Loads all tournaments from the backend and sorts them
   */
  loadTournaments(): void {
    this.tournamentService.getAllTournament().subscribe({
      next: (tournament) => {
        this.tournaments.set(tournament);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Error loading tournaments');
        this.loading.set(false);
      }
    });
  }

  totalPages = computed(() =>
    Math.max(1, Math.ceil(this.tournaments().length / this.pageSize))
  );

  paginatedTournaments = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize;
    return this.tournaments().slice(start, start + this.pageSize);
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

}
