import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SendNotificationDTO {
  title: string;
  content: string;
  priority: string; // 'Tin thường' | 'Khẩn cấp'
  scope: string;    // 'All' | 'Phường 1'...
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private apiUrl = 'http://localhost:5084/api';

  constructor(private http: HttpClient) { }

  private getHeaders() {
    const token = localStorage.getItem('token');
    return new HttpHeaders({ 'Authorization': `Bearer ${token}` });
  }

  broadcast(dto: SendNotificationDTO): Observable<any> {
    return this.http.post(`${this.apiUrl}/Notifications/broadcast`, dto, { headers: this.getHeaders(), responseType: 'text' });
  }
}