import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../environement/environement';
import {Observable} from 'rxjs';
import {Tournament} from '../Interface/tournament';
import {CreateTournamentRequest} from '../Interface/createTournamentRequest';

@Injectable({
  providedIn: 'root',
})
export class TournamentService {

  private apiUrl = `${environment.apiUrl}/tournaments`;
  constructor(private http: HttpClient) {
  }

  getAllTournament(): Observable<Tournament[]> {
    return this.http.get<Tournament[]>(this.apiUrl);
  }

  createTournament(tournament:CreateTournamentRequest): Observable<Tournament> {
    return this.http.post<Tournament>(`${this.apiUrl}/create`, tournament);
  }

  deleteTournament(id : number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete/${id}`);
  }


  getTournamentById(id: number): Observable<Tournament> {
    return this.http.get<Tournament>(`${this.apiUrl}/${id}`);
  }

  getTournamentByClubId(clubId: number): Observable<Tournament[]> {
    return this.http.get<Tournament[]>(`${this.apiUrl}/allTournamentByClub/${clubId}`);
  }

  quitTournament(tournamentId : number): Observable<void>{
    return this.http.get<void>(`${this.apiUrl}/quitTournament/${tournamentId}`);
  }
}
