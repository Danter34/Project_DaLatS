import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { LoginDTO, LoginResponse, User } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:5084/api/auth'; 

  constructor(private http: HttpClient, private router: Router) { }

  login(dto: LoginDTO): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, dto).pipe(
      tap(response => {
        // Chỉ lưu nếu role KHÔNG PHẢI là User
        if (response.user.role !== 'User') {
          localStorage.setItem('token', response.token);
          localStorage.setItem('user', JSON.stringify(response.user));
        }
      })
    );
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.router.navigate(['/login']);
  }

  getUser(): User | null {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }
}