import {Component, computed, OnInit, signal} from '@angular/core';
import { CommonModule } from '@angular/common';
import {RouterLink, Router, ActivatedRoute} from '@angular/router';
import {Clubs} from '../../Interface/clubs';
import {ClubService} from '../../Service/club-service';
import {LoginService} from '../../Service/login-service';
import {Navbar} from '../navbar/navbar';

@Component({
  selector: 'app-responsable-dashboard',
  standalone: true,
  imports: [CommonModule,  RouterLink, Navbar],
  templateUrl: './responsable-dashboard.component.html',
  styleUrls: ['./responsable-dashboard.component.css']
})
export class ResponsableDashboardComponent implements OnInit {
  clubs = signal<Clubs[]>([]);
  loading = signal(true);
  error = signal('');
  constructor(
    private clubService: ClubService,
    private auth: LoginService,
    private router: Router
  ) {}

  // À ajouter dans la classe du composant.

  readonly pageSize = 6;

  currentPage = signal(1);

  totalPages = computed(() =>
    Math.max(1, Math.ceil(this.clubs().length / this.pageSize))
  );

  paginatedClubs = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize;
    return this.clubs().slice(start, start + this.pageSize);
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
    const user = this.auth.getCurrentUser();
    const userId = user?.id;

    if (!userId) {
      this.error.set ('User not logged in.');
      this.loading.set(false);
      return;
    }

    this.loadClubs();
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

  /**
   * Loads all clubs from the backend and sorts them
   */
  loadClubs(): void {
    const token = this.getAuthToken();
    if (!token) return;

    this.clubService.getClubsByManager().subscribe({
      next: (clubs) => {
        this.clubs.set(clubs);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Error loading clubs');
        this.loading.set(false);
      }
    });
  }
}
