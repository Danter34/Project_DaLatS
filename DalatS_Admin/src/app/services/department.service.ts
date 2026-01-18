import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateDepartmentDTO, Department } from '../models/department.model';


@Injectable({
  providedIn: 'root'
})
export class DepartmentService {
  private apiUrl = 'http://localhost:5084/api/Departments'; // Đổi port nếu cần

  constructor(private http: HttpClient) { }

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  getAll(): Observable<Department[]> {
    return this.http.get<Department[]>(`${this.apiUrl}/get-all`, { headers: this.getHeaders() });
  }

  create(dto: CreateDepartmentDTO): Observable<Department> {
    return this.http.post<Department>(`${this.apiUrl}/create`, dto, { headers: this.getHeaders() });
  }

  update(id: number, dto: CreateDepartmentDTO): Observable<Department> {
    return this.http.put<Department>(`${this.apiUrl}/update-by-id/${id}`, dto, { headers: this.getHeaders() });
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/delete-by-id/${id}`, { headers: this.getHeaders() });
  }
}