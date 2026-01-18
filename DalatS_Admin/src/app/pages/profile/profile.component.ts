import { Component, OnInit, ChangeDetectorRef } from '@angular/core'; // 1. Import ChangeDetectorRef
import { ProfileService, UserProfile, ChangePasswordDTO } from '../../services/profile.service';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css'],
  standalone: false
})
export class ProfileComponent implements OnInit {
  
  user: UserProfile | null = null;
  
  // Dữ liệu đổi mật khẩu
  passData = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  };

  isLoading: boolean = false;
  message: string = '';
  isError: boolean = false;

  constructor(
    private profileService: ProfileService,
    private cdr: ChangeDetectorRef // 2. Inject ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile() {
    this.profileService.getProfile().subscribe({
      next: (res) => {
        this.user = res;
        // 3. Ép giao diện cập nhật ngay khi có dữ liệu profile
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.cdr.detectChanges();
      }
    });
  }

  onChangePassword() {
    // 1. Validate cơ bản
    if (!this.passData.oldPassword || !this.passData.newPassword) {
      this.showMessage('Vui lòng nhập đầy đủ thông tin', true);
      return;
    }

    if (this.passData.newPassword !== this.passData.confirmPassword) {
      this.showMessage('Mật khẩu xác nhận không khớp', true);
      return;
    }

    if (this.passData.newPassword.length < 6) {
      this.showMessage('Mật khẩu mới phải từ 6 ký tự', true);
      return;
    }

    // 2. Gọi API
    this.isLoading = true;
    const dto: ChangePasswordDTO = {
      oldPassword: this.passData.oldPassword,
      newPassword: this.passData.newPassword
    };

    this.profileService.changePassword(dto).subscribe({
      next: () => {
        this.isLoading = false;
        this.showMessage('Đổi mật khẩu thành công!', false);
        // Reset form
        this.passData = { oldPassword: '', newPassword: '', confirmPassword: '' };
        
        // 4. Ép giao diện cập nhật (tắt loading, hiện thông báo)
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isLoading = false;
        // Backend throw Exception("Mật khẩu cũ không đúng") sẽ trả về 500 hoặc 400
        this.showMessage('Mật khẩu cũ không đúng hoặc lỗi hệ thống', true);
        
        // 5. Ép giao diện cập nhật khi lỗi
        this.cdr.detectChanges();
      }
    });
  }

  showMessage(msg: string, isErr: boolean) {
    this.message = msg;
    this.isError = isErr;
    
    // 6. Cập nhật ngay để hiện thông báo
    this.cdr.detectChanges();

    setTimeout(() => {
      this.message = '';
      // 7. Cập nhật lại sau khi ẩn thông báo
      this.cdr.detectChanges();
    }, 3000); 
  }
}