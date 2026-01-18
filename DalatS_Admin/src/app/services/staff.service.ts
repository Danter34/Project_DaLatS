import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StaffUser, CreateStaffDTO } from '../models/staff.model';



// Interface cho Phòng ban (dùng để đổ vào dropdown lọc)
export interface DepartmentOption {
  departmentId: number;
  name: string;
}

@Injectable({
  providedIn: 'root'
})
export class StaffService {
  private apiUrl = 'http://localhost:5084/api'; 

  constructor(private http: HttpClient) { }

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  // Lấy tất cả user (sau đó về Client sẽ lọc Role = Staff)
  getAllUsers(): Observable<StaffUser[]> {
    return this.http.get<StaffUser[]>(`${this.apiUrl}/auth/get-all`, { headers: this.getHeaders() });
  }

  // Lấy danh sách phòng ban để làm Filter
  getDepartments(): Observable<DepartmentOption[]> {
    return this.http.get<DepartmentOption[]>(`${this.apiUrl}/departments/get-all`, { headers: this.getHeaders() });
  }

  // Các hàm khóa/mở khóa (giống user)
  lockUser(id: number, reason: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/auth/${id}/lock`, { reason }, { headers: this.getHeaders(), responseType: 'text' });
  }

  unlockUser(id: number, reason: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/auth/${id}/unlock`, { reason }, { headers: this.getHeaders(), responseType: 'text' });
  }
  createStaff(dto: CreateStaffDTO): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/create-staff`, dto, { headers: this.getHeaders() });
  }
}