import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateAnswerDTO, QuestionDTO } from '../models/qa.model';

// DTO trả về từ API

@Injectable({
  providedIn: 'root'
})
export class QaService {
  private apiUrl = 'http://localhost:5084/api/QA'; 

  constructor(private http: HttpClient) { }

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  // Lấy danh sách câu hỏi
  getAllQuestions(): Observable<QuestionDTO[]> {
    return this.http.get<QuestionDTO[]>(this.apiUrl, { headers: this.getHeaders() });
  }

  // Gửi câu trả lời
  sendAnswer(questionId: number, content: string): Observable<any> {
    const dto: CreateAnswerDTO = { content };
    return this.http.post(`${this.apiUrl}/${questionId}/answer`, dto, { headers: this.getHeaders() });
  }
}