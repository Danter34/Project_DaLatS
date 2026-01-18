import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CitizenService } from '../../services/citizen.service';
import { AdminUser } from '../../models/citizen.model';

@Component({
  selector: 'app-citizens',
  templateUrl: './citizens.component.html',
  styleUrls: ['./citizens.component.css'],
  standalone: false
})
export class CitizensComponent implements OnInit {
  
  // Dữ liệu
  allUsers: AdminUser[] = [];
  filteredUsers: AdminUser[] = [];
  paginatedUsers: AdminUser[] = [];

  // Tìm kiếm & Phân trang
  searchText: string = '';
  currentPage: number = 1;
  pageSize: number = 10;
  totalPages: number = 1;
  Math = Math; 

  // Modal xử lý khóa
  showModal: boolean = false;
  modalType: 'lock' | 'unlock' = 'lock';
  selectedUser: AdminUser | null = null;
  lockReason: string = '';
  
  // Loading & Success Modal
  isLoading: boolean = false;
  showSuccessModal: boolean = false;
  successMessage: string = '';

  constructor(
    private citizenService: CitizenService,
    private cdr: ChangeDetectorRef // Inject để fix lỗi view không update
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData() {
    this.isLoading = true;
    this.citizenService.getAllUsers().subscribe({
      next: (res) => {
        // Chỉ lấy role User
        this.allUsers = res.filter(u => u.role === 'User');
        
        // Gọi applyFilter ngay để hiển thị dữ liệu ban đầu
        this.applyFilter(); 
        
        this.isLoading = false;
        
        // QUAN TRỌNG: Ép Angular cập nhật giao diện ngay lập tức
        this.cdr.detectChanges(); 
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // --- LOGIC TÌM KIẾM & PHÂN TRANG (Đã sửa lỗi null) ---
  applyFilter() {
    // 1. Xử lý text tìm kiếm: Nếu null/undefined thì gán rỗng
    const text = this.searchText ? this.searchText.toLowerCase().trim() : '';

    // 2. Lọc dữ liệu
    if (text) {
      this.filteredUsers = this.allUsers.filter(u => 
        (u.fullName && u.fullName.toLowerCase().includes(text)) || 
        (u.email && u.email.toLowerCase().includes(text))
      );
    } else {
      // Nếu không có text tìm kiếm, hiển thị toàn bộ
      this.filteredUsers = [...this.allUsers];
    }

    // 3. Tính toán phân trang
    this.totalPages = Math.ceil(this.filteredUsers.length / this.pageSize);

    // Xử lý logic nhảy trang khi lọc
    if (this.filteredUsers.length === 0) {
      this.currentPage = 0;
    } else if (this.currentPage > this.totalPages || this.currentPage === 0) {
      this.currentPage = 1;
    }

    this.updatePaginatedData();
  }

  updatePaginatedData() {
    // Nếu không có dữ liệu thì reset mảng hiển thị
    if (this.currentPage === 0) {
      this.paginatedUsers = [];
      return;
    }

    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.paginatedUsers = this.filteredUsers.slice(startIndex, endIndex);
  }

  changePage(page: number) {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.updatePaginatedData();
    }
  }

  // Sự kiện khi gõ phím (keyup)
  onSearch() {
    this.applyFilter();
  }

  // --- LOGIC KHÓA / MỞ KHÓA ---
  openModal(user: AdminUser, type: 'lock' | 'unlock') {
    this.selectedUser = user;
    this.modalType = type;
    this.lockReason = ''; 
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.selectedUser = null;
  }

  closeSuccessModal() {
    this.showSuccessModal = false;
  }

  confirmAction() {
    if (!this.selectedUser) return;

    const action$ = this.modalType === 'lock' 
      ? this.citizenService.lockUser(this.selectedUser.userId, this.lockReason)
      : this.citizenService.unlockUser(this.selectedUser.userId, this.lockReason);

    action$.subscribe({
      next: () => {
        // Đóng modal nhập lý do
        this.closeModal();

        // Hiện thông báo thành công
        this.successMessage = this.modalType === 'lock' 
          ? `Đã khóa tài khoản ${this.selectedUser?.fullName} thành công!`
          : `Đã mở khóa tài khoản ${this.selectedUser?.fullName} thành công!`;
        
        this.showSuccessModal = true;

        // Tải lại dữ liệu mới nhất
        this.loadData();
      },
      error: (err) => {
        alert('Có lỗi xảy ra, vui lòng thử lại.');
        console.error(err);
      }
    });
  }
}