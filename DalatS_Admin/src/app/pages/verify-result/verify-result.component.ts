import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-verify-result',
  templateUrl: './verify-result.component.html',
  styleUrls: ['./verify-result.component.css'],
  standalone: false
})
export class VerifyResultComponent implements OnInit {

  isLoading = true;
  isSuccess = false;
  message = '';

  // API Backend (khớp với hình bạn gửi)
  private readonly apiUrl = 'http://localhost:5084/api/Auth/verify-email';

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private cdr: ChangeDetectorRef // Dùng để ép giao diện cập nhật ngay lập tức
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParams['token'];

    if (!token) {
      this.handleState(false, 'Token không hợp lệ hoặc thiếu.');
      return;
    }

    // --- QUAN TRỌNG NHẤT: Thêm { responseType: 'text' } ---
    // Điều này báo cho Angular biết: "Đừng parse JSON, hãy trả về chuỗi String nguyên bản"
    this.http.get(`${this.apiUrl}?token=${token}`, { responseType: 'text' })
      .subscribe({
        next: (responseBody) => {
          // responseBody chính là dòng chữ: "Xác minh email thành công"
          console.log("API Success:", responseBody);
          this.handleState(true, responseBody);
        },
        error: (err) => {
          console.error("API Error:", err);
          
          // Lấy thông báo lỗi từ Backend trả về (nếu có)
          // Backend thường trả lỗi trong err.error (cũng là text)
          let errorMsg = 'Xác minh thất bại. Vui lòng thử lại.';
          
          if (err.error && typeof err.error === 'string') {
             errorMsg = err.error; // Lấy nguyên văn câu lỗi từ server
          }

          this.handleState(false, errorMsg);
        }
      });
  }

  // Hàm cập nhật trạng thái chung để tránh viết lặp code
  private handleState(success: boolean, msg: string) {
    this.isLoading = false;
    this.isSuccess = success;
    this.message = msg;
    
    // Ép Angular vẽ lại giao diện ngay lập tức để tắt vòng xoay
    this.cdr.detectChanges();
  }
}