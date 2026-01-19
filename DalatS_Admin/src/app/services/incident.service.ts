import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { IncidentDTO, MergeIncidentDTO, UpdateStatusDTO } from '../models/incident.model';


@Injectable({
  providedIn: 'root'
})
export class IncidentService {
  private apiUrl = 'http://localhost:5084/api/Incidents'; 

  constructor(private http: HttpClient) { }

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  // 1. Lấy danh sách (Admin: All, Staff: Theo phòng ban)
  getAll(): Observable<IncidentDTO[]> {
    return this.http.get<IncidentDTO[]>(this.apiUrl, { headers: this.getHeaders() });
  }

  // 2. Lấy chi tiết
  getDetail(id: number): Observable<IncidentDTO> {
    return this.http.get<IncidentDTO>(`${this.apiUrl}/get-by-id/${id}`, { headers: this.getHeaders() });
  }

  // 3. Cập nhật trạng thái / Gán phòng ban
  updateStatus(id: number, dto: UpdateStatusDTO): Observable<any> {
    return this.http.put(`${this.apiUrl}/update-by-id/${id}`, dto, { headers: this.getHeaders() });
  }

  // 4. Gợi ý sự cố trùng lặp (Admin)
  getDuplicates(id: number): Observable<IncidentDTO[]> {
    return this.http.get<IncidentDTO[]>(`${this.apiUrl}/suggest-duplicates/${id}`, { headers: this.getHeaders() });
  }

  // 5. Gộp sự cố (Admin)
  mergeIncidents(dto: MergeIncidentDTO): Observable<any> {
    return this.http.post(`${this.apiUrl}/merge`, dto, { headers: this.getHeaders() });
  }
}