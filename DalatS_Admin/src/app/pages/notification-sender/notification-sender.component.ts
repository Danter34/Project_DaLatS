import { Component, ChangeDetectorRef } from '@angular/core'; // [FIX] Import ChangeDetectorRef
import { NotificationService, SendNotificationDTO } from '../../services/notification.service';

@Component({
  selector: 'app-notification-sender',
  templateUrl: './notification-sender.component.html',
  styleUrls: ['./notification-sender.component.css'],
  standalone: false
})
export class NotificationSenderComponent {
  data: SendNotificationDTO = {
    title: '',
    content: '',
    priority: 'Tin thường',
    scope: 'All'
  };
  
  isLoading = false;
  showConfirmModal = false;
  
  resultModal = {
    show: false,
    isSuccess: true,
    message: ''
  };

  constructor(
    private notiService: NotificationService,
    private cdr: ChangeDetectorRef // [FIX] Inject CDR
  ) {}

  openConfirmModal() {
    if (!this.data.title || !this.data.content) {
      this.resultModal = {
        show: true,
        isSuccess: false,
        message: 'Vui lòng nhập đầy đủ tiêu đề và nội dung thông báo.'
      };
      return;
    }
    this.showConfirmModal = true;
  }

  send() {
    // 1. Tắt modal xác nhận
    this.showConfirmModal = false;
    
    // 2. Bật loading ngay lập tức
    this.isLoading = true;
    this.cdr.detectChanges(); // [FIX] Ép render UI để hiện Spinner ngay

    // 3. Gọi API
    this.notiService.broadcast(this.data).subscribe({
      next: (res) => {
        // Tắt loading
        this.isLoading = false;
        
        // Hiện modal thành công
        this.resultModal = {
          show: true,
          isSuccess: true,
          message: 'Thông báo đã được gửi đến toàn bộ danh sách người nhận.'
        };

        // Reset form
        this.data.title = '';
        this.data.content = '';
        this.data.priority = 'Tin thường';
        
        this.cdr.detectChanges(); // [FIX] Update UI lần nữa
      },
      error: (err) => {
        this.isLoading = false;
        
        // Hiện modal lỗi
        this.resultModal = {
          show: true,
          isSuccess: false,
          message: 'Có lỗi xảy ra khi kết nối đến Server. Vui lòng thử lại sau.'
        };
        console.error(err);
        this.cdr.detectChanges(); // [FIX] Update UI lần nữa
      }
    });
  }

  closeResultModal() {
    this.resultModal.show = false;
  }
}