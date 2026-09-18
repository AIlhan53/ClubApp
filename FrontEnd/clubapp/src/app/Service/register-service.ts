import { Injectable } from '@angular/core';
import {environment} from '../../environement/environement';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {registerRequest} from '../Interface/registerRequest';

@Injectable({
  providedIn: 'root',
})
export class RegisterService {

  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // Méthode pour inscrire un utilisateur
  register(request: registerRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/auth/register`,request);
  }
}
