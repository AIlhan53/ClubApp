import {Component, computed, signal} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {InvitationStatus} from '../../Interface/InvitationStatus';

@Component({
  selector: 'app-inviation-status-component',
  imports: [],
  templateUrl: './inviation-status-component.html',
  styleUrl: './inviation-status-component.css',
})
export class InviationStatusComponent {

  status = signal<InvitationStatus>(InvitationStatus.unknown);

  isAccepted = computed(
    () => this.status() === InvitationStatus.accepted
  );

  isDeclined = computed(
    () => this.status() === InvitationStatus.declined
  );

  constructor(
    private route: ActivatedRoute
  ) {
    const rawStatus = this.route.snapshot.queryParamMap.get('status');

    if (
      rawStatus === InvitationStatus.accepted ||
      rawStatus === InvitationStatus.declined
    ) {
      this.status.set(rawStatus);
    }
  }
}
