import {Component, computed, signal} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {LoginService} from '../../Service/login-service';
import {ClubService} from '../../Service/club-service';
import {ClubMember} from '../../Interface/ClubMember';
import {HttpStatusCode} from '@angular/common/http';
import {Navbar} from '../navbar/navbar';
import {Clubs} from '../../Interface/clubs';

@Component({
  selector: 'app-club-member-component',
  imports: [
    Navbar
  ],
  templateUrl: './club-member-component.html',
  styleUrl: './club-member-component.css',
  standalone: true
})
export class ClubMemberComponent {
  members: ClubMember[] = [];
  clubId: number = 0;
  loading = signal(true);
  error = signal('');

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: LoginService,
    private clubService: ClubService
  ) {}

  readonly pageSize = 6;

  currentPage = signal(1);

  totalPages = computed(() =>
    Math.max(1, Math.ceil(this.members.length / this.pageSize))
  );

  paginatedMembers = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize;
    return this.members.slice(start, start + this.pageSize);
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
      this.loadClubInfo();
      this.loadMember();
    }
  }

  private getToken(): string | null {
    return this.authService.getToken();
  }

  loadClubInfo(): void {
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

  loadMember(): void {
    const token = this.getToken();
    if (!token) {
      this.error.set('Please login first');
      this.loading.set(false);
      return;
    }

    this.clubService.getMember(this.clubId).subscribe({
      next: (members) => {
        this.members = members;
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading subscribers:', err);
        if (err.status === HttpStatusCode.Forbidden) {
          this.error.set('You can only view subscribers of your own store.');
        } else {
          this.error.set('Error loading subscribers.');
        }
        this.loading.set(false);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/responsable/dashboard']);
  }
}
