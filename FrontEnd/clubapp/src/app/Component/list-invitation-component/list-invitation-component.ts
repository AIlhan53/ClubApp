import {Component, signal} from '@angular/core';
import {InvitationTournament} from '../../Interface/InviatationTournament';
import {InvitationTournamentService} from '../../Service/invitation-tournament-service';
import {FormsModule} from '@angular/forms';
import {Navbar} from '../navbar/navbar';
import {ActivatedRoute, Router} from '@angular/router';
import {Tournament} from '../../Interface/tournament';

@Component({
  selector: 'app-list-invitation-component',
  imports: [
    FormsModule,
    Navbar
  ],
  templateUrl: './list-invitation-component.html',
  styleUrl: './list-invitation-component.css',
})
export class ListInvitationComponent { requests = signal<InvitationTournament[]>([]);
  filteredRequests = signal<InvitationTournament[]>([]);
  loading = signal(true);
  errorMessage = signal('');
  filterStatus = signal<string>('ALL');

  clubId = 0;
  tournamentId = 0;

  constructor(private invitationService: InvitationTournamentService,
              private route:ActivatedRoute,
              private router: Router) {
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.clubId = +params['clubId'];
      this.tournamentId = +params['tournamentId'];

      this.loadRequests();
    });
  }

  loadRequests(): void {
    this.loading.set(true);
    this.invitationService.getAllInvitationByTournamentId(this.tournamentId).subscribe({
      next: (data) => {
        this.requests.set(data);
        this.applyFilter();
        this.loading.set(false);
      },
      error: (error) => {
        this.errorMessage.set('Error while loading requests');
        this.loading.set(false);
        console.error(error);
      }
    });
  }

  applyFilter(): void {
    const status = this.filterStatus();
    if (status === 'ALL') {
      this.filteredRequests.set(this.requests());
    } else {
      this.filteredRequests.set(
        this.requests().filter(r => r.invitationStatus === status)
      );
    }
  }

  onFilterChange(status: string): void {
    this.filterStatus.set(status);
    this.applyFilter();
  }

  getStatusLabel(status: string): string {
    const labels: { [key: string]: string } = {
      'PENDING': 'PENDING',
      'ACCEPTED': 'ACCEPTED',
      'DECLINED': 'DECLINED',
      'CANCELLED': 'CANCELLED'
    };
    return labels[status] || status;
  }

  goBack(): void {
    this.router.navigate(['/responsable/club/tournament', this.clubId]);
  }
}
