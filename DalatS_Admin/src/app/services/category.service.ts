import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateCategoryDTO, IncidentCategory } from '../models/categories.model';



@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private apiUrl = 'http://localhost:5084/api/IncidentCategories'; // Port tùy chỉnh theo backend của bạn

  constructor(private http: HttpClient) { }

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  // Lấy danh sách (Public)
  getAll(): Observable<IncidentCategory[]> {
    return this.http.get<IncidentCategory[]>(`${this.apiUrl}/get-all`);
  }

  // Thêm mới (Admin)
  create(dto: CreateCategoryDTO): Observable<IncidentCategory> {
    return this.http.post<IncidentCategory>(this.apiUrl, dto, { headers: this.getHeaders() });
  }

  // Cập nhật (Admin)
  // Lưu ý: Route backend là "update-by-id{id}" nên không có dấu "/" ở giữa
  update(id: number, dto: CreateCategoryDTO): Observable<IncidentCategory> {
    return this.http.put<IncidentCategory>(`${this.apiUrl}/update-by-id/${id}`, dto, { headers: this.getHeaders() });
  }

  // Xóa (Admin)
  delete(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/delete-by-id/${id}`, { headers: this.getHeaders() });
  }
}