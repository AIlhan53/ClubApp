import {Component, computed, OnInit, signal} from '@angular/core';
import {Navbar} from '../navbar/navbar';
import {Clubs} from '../../Interface/clubs';
import {ClubService} from '../../Service/club-service';
import {LoginService} from '../../Service/login-service';
import Swal from 'sweetalert2';
import {RoleTypeEnum} from '../../Interface/enumRoleType';
import {User} from '../../Interface/user';

@Component({
  selector: 'app-dashboard',
  imports: [
    Navbar
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  clubs = signal<Clubs[]>([]);
  loading = signal(true);
  error = signal('');

  constructor(private clubService: ClubService,
              private loginService: LoginService) {
  }

  readonly pageSize = 6;
  currentPage = signal(1);

  totalPages = computed(() =>
    Math.max(1, Math.ceil(this.clubs().length / this.pageSize)));

  paginatedClubs = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize;
    return this.clubs().slice(start, start + this.pageSize);
  });

  pageNumbers = computed(() =>
    Array.from({ length: this.totalPages() }, (_, i) => i + 1));

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages()) this.currentPage.set(page);
  }

  nextPage(): void { this.goToPage(this.currentPage() + 1); }
  previousPage(): void { this.goToPage(this.currentPage() - 1); }

  ngOnInit(): void {
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

    this.loadClubs();
  }

  /**
   * Loads all clubs from the backend and sorts them
   */
  loadClubs(): void {
    this.clubService.getAllClubs().subscribe({
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

  toggleToJoin(club: Clubs): void {
    const token = this.getToken();
    if (!token) {
      console.error('No token');
      return;
    }
    if (club.joined) {
      Swal.fire({
        icon: 'warning',
        title: 'Leave this club?',
        text: 'All your tournament participations in this club will be cancelled. This action cannot be undone.',
        showCancelButton: true,
        confirmButtonText: 'Yes, leave',
        cancelButtonText: 'Cancel',
        confirmButtonColor: '#d33',
        reverseButtons: true
      }).then(result => {

        if (!result.isConfirmed) {
          return;
        }

        this.clubService.selfLeave(club.id).subscribe({
          next: () => {
            const updatedClubs = this.clubs().map(c =>
              c.id === club.id ? {...c, joined: !c.joined} : c
            );
            Swal.fire({
              icon: 'success',
              title: 'You are no longer a member of the club : ' + club.clubName,
              showConfirmButton: false,
              timer: 1000,
              timerProgressBar: true
            }).then();
            this.clubs.set(updatedClubs);
          },
          error: (err) => {
            console.error('Error while leave', err);
          }
        });
      });
    } else {
      this.clubService.selfJoin(club.id).subscribe({
        next: () => {
          const updatedClubs = this.clubs().map(c =>
            c.id === club.id ? {...c, joined: !c.joined} : c
          );
          Swal.fire({
            icon: 'success',
            title: 'you are now a member of the club: ' + club.clubName,
            showConfirmButton: false,
            timer: 1000,
            timerProgressBar: true
          }).then();
          this.clubs.set(updatedClubs);
        },
        error: (err) => {
          console.error('Error while join', err);
        }
      });
    }
  }
}
