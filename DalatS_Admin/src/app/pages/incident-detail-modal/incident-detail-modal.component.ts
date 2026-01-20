import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';

@Component({
  selector: 'app-incident-detail-modal',
  templateUrl: './incident-detail-modal.component.html',
  styleUrls: ['./incident-detail-modal.component.css'],
  standalone: false
})
export class IncidentDetailModalComponent implements OnChanges {
  @Input() incident: any = null;
  @Input() isVisible: boolean = false;
  @Output() onClose = new EventEmitter<void>();

  activeImage: string = 'assets/images/no-image.png'; // Mặc định ảnh rỗng
  private readonly baseUrl = 'http://localhost:5084'; // Đảm bảo port đúng

  ngOnChanges(changes: SimpleChanges): void {
    // Mỗi khi dữ liệu incident thay đổi hoặc modal mở lên
    if (changes['incident'] && this.incident) {
      if (this.incident.images && this.incident.images.length > 0) {
        // Lấy ảnh đầu tiên làm ảnh chính
        this.activeImage = this.getImageUrl(this.incident.images[0].filePath);
      } else {
        this.activeImage = 'assets/images/no-image.png';
      }
    }
  }

  close() {
    this.onClose.emit();
  }

  // Hàm xử lý đường dẫn ảnh (Giống hệt bên IncidentsComponent)
  getImageUrl(filePath: string): string {
    if (!filePath) return 'assets/images/no-image.png';
    if (filePath.startsWith('http')) return filePath;
    
    // Xử lý dấu gạch chéo để tránh lỗi double slash //
    const path = filePath.startsWith('/') ? filePath : `/${filePath}`;
    return `${this.baseUrl}${path}`;
  }

  // Khi click vào ảnh nhỏ -> Đổi ảnh to
  setActiveImage(filePath: string) {
    this.activeImage = this.getImageUrl(filePath);
  }
}