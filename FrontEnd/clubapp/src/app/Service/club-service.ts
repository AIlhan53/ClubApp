import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {environment} from '../../environement/environement';
import {Observable} from 'rxjs';
import {Clubs} from '../Interface/clubs';
import {CreateClubRequest} from '../Interface/createClubRequest';
import {User} from '../Interface/user';
import {ClubMember} from '../Interface/ClubMember';
import {Tournament} from '../Interface/tournament';

@Injectable({
  providedIn: 'root',
})
export class ClubService {

  private apiUrl = `${environment.apiUrl}/club`;
  constructor(private http: HttpClient) {}


  getAllClubs(): Observable<Clubs[]> {
    return this.http.get<Clubs[]>(this.apiUrl);
  }

  getClubsByMemberId(): Observable<Clubs[]> {
    return this.http.get<Clubs[]>(`${this.apiUrl}/memberClubs`);
  }

  getClubsByManager(): Observable<Clubs[]> {
    return this.http.get<Clubs[]>(`${this.apiUrl}/responsableClubs`);
  }

  getMember(clubId: number): Observable<ClubMember[]> {
    return this.http.get<ClubMember[]>(`${this.apiUrl}/allMember/${clubId}`);
  }

  getClubById(id: number): Observable<Clubs> {
    return this.http.get<Clubs>(`${this.apiUrl}/get/${id}`);
  }

  createClub(club:CreateClubRequest): Observable<Clubs> {
    return this.http.post<Clubs>(`${this.apiUrl}/create`, club);
  }

  updateClub(id : number, club:CreateClubRequest): Observable<Clubs> {
    return this.http.put<Clubs>(`${this.apiUrl}/update/${id}`, club);
  }

  deleteClub(id : number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete/${id}`);
  }

  selfJoin(clubId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/joinClub/${clubId}`, {}, {
    });
  }

  selfLeave(clubId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/leaveClub/${clubId}`, {}, {
    });
  }
}
