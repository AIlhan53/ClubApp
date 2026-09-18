import { Injectable } from '@angular/core';
import {environment} from '../../environement/environement';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {InvitationTournament} from '../Interface/InviatationTournament';
@Injectable({
  providedIn: 'root',
})
export class InvitationTournamentService {

  private apiUrl = `${environment.apiUrl}/invitation/tournaments`;

  constructor(private http: HttpClient) {
  }

  getAllInvitationByTournamentId(id: number): Observable<InvitationTournament[]> {
    return this.http.get<InvitationTournament[]>(`${this.apiUrl}/${id}`);
  }
}
