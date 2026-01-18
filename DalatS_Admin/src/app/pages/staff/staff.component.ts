import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { StaffService, DepartmentOption } from '../../services/staff.service';
import { AuthService } from '../../services/auth.service'; // 1. Import AuthService
import { forkJoin } from 'rxjs';
import { CreateStaffDTO, StaffUser } from '../../models/staff.model'; // Đảm bảo đường dẫn import đúng

@Component({
  selector: 'app-staff',
  templateUrl: './staff.component.html',
  styleUrls: ['./staff.component.css'],
  standalone: false
})
export class StaffComponent implements OnInit {
  showCreateModal: boolean = false;

  createData: CreateStaffDTO = {
    fullName: '',
    email: '',
    password: '',
    role: 'Staff', 
    departmentId: null
  };
  
  // Dữ liệu
  allStaff: StaffUser[] = [];
  departments: DepartmentOption[] = [];
  
  // Dữ liệu hiển thị
  filteredStaff: StaffUser[] = [];
  paginatedStaff: StaffUser[] = [];

  // Filter & Search
  searchText: string = '';
  selectedDeptId: any = 'all'; 

  // Pagination
  currentPage: number = 1;
  pageSize: number = 10;
  totalPages: number = 1;
  Math = Math;

  // Modal
  showModal: boolean = false;
  modalType: 'lock' | 'unlock' = 'lock';
  selectedUser: StaffUser | null = null;
  lockReason: string = '';
  
  showSuccessModal: boolean = false;
  successMessage: string = '';
  isLoading: boolean = false;

  // Biến lưu ID người đang đăng nhập
  currentUserId: number = 0;

  constructor(
    private staffService: StaffService,
    private authService: AuthService, // 2. Inject AuthService
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    // 3. Lấy thông tin người đang đăng nhập
    const currentUser = this.authService.getUser();
    if (currentUser) {
        this.currentUserId = currentUser.userId; // Hoặc user.id tùy model của bạn
    }

    this.loadData();
  }

  // ... (Giữ nguyên các hàm openCreateModal, closeCreateModal, submitCreate) ...
  openCreateModal() {
    this.createData = {
      fullName: '',
      email: '',
      password: '',
      role: 'Staff',
      departmentId: null 
    };
    
    if (this.departments.length > 0) {
       this.createData.departmentId = this.departments[0].departmentId;
    }

    this.showCreateModal = true;
  }

  closeCreateModal() {
    this.showCreateModal = false;
  }

  submitCreate() {
    if (!this.createData.fullName || !this.createData.email || !this.createData.password) {
      alert('Vui lòng nhập đầy đủ thông tin bắt buộc!');
      return;
    }

    this.isLoading = true;
    this.staffService.createStaff(this.createData).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.closeCreateModal();
        this.successMessage = `Đã thêm nhân viên ${this.createData.fullName} thành công!`;
        this.showSuccessModal = true;
        this.loadData();
      },
      error: (err) => {
        this.isLoading = false;
        console.error(err);
        const msg = err.error || 'Có lỗi xảy ra khi tạo nhân viên';
        alert(msg); 
        this.cdr.detectChanges();
      }
    });
  }

  loadData() {
    this.isLoading = true;
    
    forkJoin({
      users: this.staffService.getAllUsers(),
      depts: this.staffService.getDepartments()
    }).subscribe({
      next: (res) => {
        // 4. LOGIC LỌC QUAN TRỌNG:
        this.allStaff = res.users.filter(u => 
            (u.role === 'Staff' || u.role === 'Admin') && 
            u.userId !== this.currentUserId
        );
        
        this.departments = res.depts;

        this.applyFilter(); 
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // ... (Giữ nguyên phần còn lại: applyFilter, updatePaginatedData, changePage, onFilterChange...)
  
  applyFilter() {
    let temp = [...this.allStaff];

    const text = this.searchText ? this.searchText.toLowerCase().trim() : '';
    
    if (text) {
      temp = temp.filter(u => 
        (u.fullName && u.fullName.toLowerCase().includes(text)) || 
        (u.email && u.email.toLowerCase().includes(text))
      );
    }

    if (this.selectedDeptId && this.selectedDeptId.toString() !== 'all') {
      const deptId = Number(this.selectedDeptId);
      temp = temp.filter(u => u.departmentId === deptId);
    }

    this.filteredStaff = temp;

    this.totalPages = Math.ceil(this.filteredStaff.length / this.pageSize);
    
    if (this.filteredStaff.length === 0) {
      this.currentPage = 0;
    } else {
      if (this.currentPage > this.totalPages || this.currentPage === 0) {
        this.currentPage = 1;
      }
    }

    this.updatePaginatedData();
  }

  updatePaginatedData() {
    if (this.currentPage === 0) {
      this.paginatedStaff = [];
      return;
    }
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.paginatedStaff = this.filteredStaff.slice(startIndex, endIndex);
  }

  changePage(page: number) {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.updatePaginatedData();
    }
  }

  onFilterChange() {
    this.applyFilter();
  }

  openModal(user: StaffUser, type: 'lock' | 'unlock') {
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
      ? this.staffService.lockUser(this.selectedUser.userId, this.lockReason)
      : this.staffService.unlockUser(this.selectedUser.userId, this.lockReason);

    action$.subscribe({
      next: () => {
        this.closeModal();
        this.successMessage = this.modalType === 'lock' 
          ? `Đã khóa nhân viên ${this.selectedUser?.fullName}!`
          : `Đã mở khóa nhân viên ${this.selectedUser?.fullName}!`;
        this.showSuccessModal = true;
        this.loadData();
      },
      error: () => alert('Lỗi hệ thống')
    });
  }
}