import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { LoginDTO } from '../../models/auth.model';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  standalone:false
})
export class LoginComponent {
  loginData: LoginDTO = { email: '', password: '' };
  errorMessage: string = '';

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit() {
    this.authService.login(this.loginData).subscribe({
      next: (res) => {
        // 1. Chặn User thường (giữ nguyên logic cũ)
        if (res.user.role === 'User') {
          this.errorMessage = 'Bạn không có quyền truy cập trang quản trị.';
          this.authService.logout();
          return;
        }

        // 2. PHÂN LUỒNG ĐIỀU HƯỚNG
        if (res.user.role === 'Admin') {
          // Admin -> Vào Dashboard xem thống kê
          this.router.navigate(['/admin/dashboard']);
        } else {
          // Manager/Staff -> Vào thẳng Quản lý sự cố
          this.router.navigate(['/admin/incidents']);
        }
      },
      error: (err) => {
        this.errorMessage = err.error || 'Đăng nhập thất bại';
      }
    });
  }
}