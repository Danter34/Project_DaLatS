import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardSummary, IncidentByAlert, IncidentByCategory } from '../models/dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private apiUrl = 'http://localhost:5084/api/Dashboard'; // Đổi port theo API của bạn

  constructor(private http: HttpClient) { }

  // Hàm lấy Header chứa Token
  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  getSummary(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${this.apiUrl}/summary`, { headers: this.getHeaders() });
  }

  getByAlert(): Observable<IncidentByAlert[]> {
    return this.http.get<IncidentByAlert[]>(`${this.apiUrl}/by-alert`, { headers: this.getHeaders() });
  }

  getByCategory(): Observable<IncidentByCategory[]> {
    return this.http.get<IncidentByCategory[]>(`${this.apiUrl}/by-category`, { headers: this.getHeaders() });
  }
}