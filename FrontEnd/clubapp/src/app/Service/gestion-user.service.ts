import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environement/environement';
import {User} from '../Interface/user';
import {UpdateUser} from '../Interface/updateUser';


@Injectable({
  providedIn: 'root'
})
export class GestionUserService {

  /**
   * apiUrl : environement variables for path ApiUrl
   * @private
   */
  private apiUrl = `${environment.apiUrl}/user`;

  /**
   *
   * @param http
   */
  constructor(private http: HttpClient) { }

  /**
   * Method for get all users
   */
  getAllUsers(): Observable<User[]> {
      return this.http.get<User[]>(this.apiUrl);
  }

  /**
   * Method for get a user by his ID
   */
  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`);
  }

  /**
   * Method for update a user
   * @param id of user
   * @param user object JSON to update
   */
  updateUser(id: number, user: UpdateUser): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/update/${id}`, user);
  }

  /**
   * method for deleting a user (ADMINISTRATOR only)
   * @param id of user
   */
  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/remove/${id}`);
  }

  /**
   * Get the actual user
   */
  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/me`);
  }

  deactivateOther(id: number): Observable<void>  {
    return this.http.post<void>(`${this.apiUrl}/${id}/deactivate`, null);
  }

  activateOther(id: number): Observable<any>  {
    return this.http.post<any>(`${this.apiUrl}/${id}/activate`, null);
  }

}
