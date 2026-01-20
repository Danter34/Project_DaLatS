import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AdminUser } from '../models/citizen.model';

export interface LockUserDTO {
  reason: string;
}

@Injectable({
  providedIn: 'root'
})
export class CitizenService {
  // Giả sử API của bạn nằm ở /api/auth hoặc /api/users
  private apiUrl = 'http://localhost:5084/api'; 

  constructor(private http: HttpClient) { }

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  getAllUsers(): Observable<AdminUser[]> {
    return this.http.get<AdminUser[]>(`${this.apiUrl}/Auth/get-all`, { headers: this.getHeaders() });
  }

  lockUser(id: number, reason: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/Auth/${id}/lock`, { reason }, { headers: this.getHeaders(), responseType: 'text' });
  }

  unlockUser(id: number, reason: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/Auth/${id}/lock`, { reason }, { headers: this.getHeaders(), responseType: 'text' });
  }
  getUserDetail(userId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/Auth/admin/user-dashboard/${userId}`, { headers: this.getHeaders() });
  }

  // 2. Lấy danh sách lịch sử báo cáo
  getUserIncidents(userId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/Incidents/admin/get-by-user/${userId}`, { headers: this.getHeaders() });
  }
}