import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { IncidentService } from '../../services/incident.service';
import { StaffService, DepartmentOption } from '../../services/staff.service';
import { AuthService } from '../../services/auth.service';
import { forkJoin, of } from 'rxjs';
import { IncidentDTO, MergeIncidentDTO, UpdateStatusDTO } from '../../models/incident.model';

@Component({
  selector: 'app-incidents',
  templateUrl: './incidents.component.html',
  styleUrls: ['./incidents.component.css'],
  standalone: false
})
export class IncidentsComponent implements OnInit {

  incidents: IncidentDTO[] = [];
  selectedIncident: IncidentDTO | null = null;
  activeImage: any = null;

  departments: DepartmentOption[] = [];
  suggestedDuplicates: IncidentDTO[] = [];
  selectedDuplicateIds: number[] = [];

  currentUser: any = null;
  isAdmin: boolean = false;
  isStaff: boolean = false;

  isLoading: boolean = false;
  isModalVisible: boolean = false;
  
  updateData: UpdateStatusDTO = {
    status: '',
    note: '',
    alertLevel: 1,
    assignedDepartmentId: undefined
  };

  private readonly baseUrl = 'http://localhost:5084'; 

  confirmModal = { show: false, title: '', message: '', type: 'confirm', action: () => {} };
  resultModal = { show: false, isSuccess: true, message: '' };

  constructor(
    private incidentService: IncidentService,
    private staffService: StaffService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef 
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUser();
    this.isAdmin = this.currentUser?.role === 'Admin';
    this.isStaff = this.currentUser?.role === 'Staff';
    this.loadData();
  }

  loadData() {
    this.isLoading = true;
    
    forkJoin({
      incidents: this.incidentService.getAll(),
      depts: this.staffService.getDepartments() 
    }).subscribe({
      next: (res) => {
        this.incidents = res.incidents.filter(x => x.status !== 'Đã gộp');
        this.departments = res.depts as DepartmentOption[]; 
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

  // --- HELPER FUNCTIONS (Giữ nguyên) ---
  getImageUrl(filePath: string): string {
    if (!filePath) return 'assets/images/no-image.png';
    if (filePath.startsWith('http')) return filePath;
    const path = filePath.startsWith('/') ? filePath : `/${filePath}`;
    return `${this.baseUrl}${path}`;
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'Chờ xử lý': return 'status-pending';
      case 'Đang xử lý': return 'status-processing';
      case 'Đã hoàn thành': return 'status-completed';
      case 'Từ chối': return 'status-rejected';
      case 'Đã gộp': return 'status-rejected';
      default: return '';
    }
  }

  // --- INTERACTION (Giữ nguyên) ---
  selectIncident(item: IncidentDTO) {
    this.selectedIncident = item;
    if (item.images && item.images.length > 0) {
      this.activeImage = item.images[0];
    } else {
      this.activeImage = null;
    }

    this.updateData = {
      status: item.status,
      alertLevel: item.alertLevel, 
      assignedDepartmentId: item.assignedDepartmentId, 
      note: ''
    };

    this.suggestedDuplicates = [];
    this.selectedDuplicateIds = [];

    if (item.status !== 'Đã hoàn thành' && item.status !== 'Từ chối' && item.status !== 'Đã gộp') {
      this.incidentService.getDuplicates(item.incidentId).subscribe(res => {
        this.suggestedDuplicates = res;
        this.cdr.detectChanges();
      });
    }
  }

  // --- ACTIONS ---

  approveAndAssign() {
    if (!this.selectedIncident) return;
    if (!this.updateData.assignedDepartmentId) {
      this.showResultModal(false, 'Vui lòng chọn đơn vị xử lý!');
      return;
    }
    this.openConfirmModal('Xác nhận duyệt?', 'Bạn có chắc chắn muốn duyệt và giao việc?', 'confirm', () => this.executeApprove());
  }

  private executeApprove() {
    const dto: UpdateStatusDTO = {
      status: 'Đang xử lý',
      alertLevel: this.updateData.alertLevel,
      assignedDepartmentId: this.updateData.assignedDepartmentId,
      note: this.updateData.note || 'Đã duyệt và chuyển đơn vị xử lý'
    };
    if (this.selectedDuplicateIds.length > 0) this.mergeAndApprove(dto);
    else this.callUpdateApi(dto);
  }

  transferDepartment() {
    if (!this.selectedIncident || !this.updateData.assignedDepartmentId) {
        this.showResultModal(false, 'Vui lòng chọn phòng ban cần chuyển đến.');
        return;
    }

    if (this.updateData.assignedDepartmentId === this.selectedIncident.assignedDepartmentId) {
        this.showResultModal(false, 'Sự cố đang ở phòng ban này rồi.');
        return;
    }

    const newDept = this.departments.find(d => d.departmentId == this.updateData.assignedDepartmentId)?.name;

    this.openConfirmModal(
      'Xác nhận chuyển đơn vị?', 
      `Bạn xác nhận chuyển sự cố này sang "${newDept}"? Bạn sẽ không còn quyền xử lý nữa.`, 
      'confirm',
      () => {
        const dto: UpdateStatusDTO = {
          status: 'Chờ xử lý', 
          alertLevel: this.updateData.alertLevel,
          assignedDepartmentId: this.updateData.assignedDepartmentId,
          note: this.updateData.note || `Đã chuyển từ ${this.currentUser.fullName} sang ${newDept}.`
        };
        this.callUpdateApi(dto);
      }
    );
  }

  reject() {
    if (!this.selectedIncident) return;
    this.openConfirmModal(
      'Xác nhận từ chối?',
      'Sự cố sẽ bị đóng và thông báo cho người dân. Không thể hoàn tác.',
      'reject',
      () => {
        const dto: UpdateStatusDTO = {
          status: 'Từ chối',
          note: this.updateData.note || 'Không đủ thông tin hoặc không đúng thẩm quyền'
        };
        this.callUpdateApi(dto);
      }
    );
  }

  updateProgress(status: string) {
    if (!this.selectedIncident) return;
    
    this.openConfirmModal(
      'Cập nhật trạng thái',
      `Chuyển trạng thái sự cố thành "${status}"?`,
      'confirm',
      () => {
        const dto: UpdateStatusDTO = {
          status: status,
          alertLevel: this.updateData.alertLevel, 
          assignedDepartmentId: this.selectedIncident?.assignedDepartmentId,
          note: this.updateData.note
        };

        if (this.selectedDuplicateIds.length > 0) {
            this.mergeAndApprove(dto);
        } else {
            this.callUpdateApi(dto);
        }
      }
    );
  }

  // [ĐÃ SỬA] Cập nhật trực tiếp vào mảng incidents thay vì gọi loadData()
  callUpdateApi(dto: UpdateStatusDTO) {
    if (!this.selectedIncident) return;
    
    this.isLoading = true;
    
    this.incidentService.updateStatus(this.selectedIncident.incidentId, dto).subscribe({
      next: () => {
        this.isLoading = false;
        
        // 1. Logic cho trường hợp CHUYỂN PHÒNG BAN (Staff)
        // Nếu chuyển sang phòng khác -> Xóa khỏi danh sách list
        if (this.isStaff && dto.assignedDepartmentId && dto.assignedDepartmentId !== this.currentUser.departmentId) {
             this.incidents = this.incidents.filter(i => i.incidentId !== this.selectedIncident?.incidentId);
             this.selectedIncident = null; // Clear selection
        } 
        // 2. Logic cho trường hợp UPDATE BÌNH THƯỜNG
        else {
             // Cập nhật dữ liệu cho selectedIncident
             if (this.selectedIncident) {
                this.selectedIncident.status = dto.status;
                if (dto.alertLevel) this.selectedIncident.alertLevel = dto.alertLevel;
                if (dto.assignedDepartmentId) {
                    this.selectedIncident.assignedDepartmentId = dto.assignedDepartmentId;
                    // Tìm tên phòng ban để cập nhật UI
                    const dept = this.departments.find(d => d.departmentId === dto.assignedDepartmentId);
                    if(dept) this.selectedIncident.assignedDepartmentName = dept.name;
                }
             }

             // Cập nhật dữ liệu trong mảng danh sách (incidents) để nó đổi màu badge ngay lập tức
             const index = this.incidents.findIndex(i => i.incidentId === this.selectedIncident?.incidentId);
             if (index !== -1 && this.selectedIncident) {
                 // Clone object để Angular nhận diện thay đổi
                 this.incidents[index] = { ...this.selectedIncident }; 
             }
        }

        // 3. Hiện Modal thông báo
        this.showResultModal(true, 'Cập nhật thành công!');
        
        // 4. Ép vẽ lại giao diện ngay lập tức
        this.cdr.detectChanges(); 
      },
      error: () => {
        this.isLoading = false;
        this.showResultModal(false, 'Có lỗi xảy ra. Vui lòng thử lại.');
        this.cdr.detectChanges();
      }
    });
  }

  // [ĐÃ SỬA] Xử lý gộp sự cố xong thì xóa các sự cố trùng khỏi list
  mergeAndApprove(updateDto: UpdateStatusDTO) {
    if (!this.selectedIncident) return;

    const mergeDto: MergeIncidentDTO = {
      masterIncidentId: this.selectedIncident.incidentId,
      duplicateIncidentIds: this.selectedDuplicateIds
    };

    this.incidentService.mergeIncidents(mergeDto).subscribe({
      next: () => {
        // Xóa các sự cố đã bị gộp khỏi danh sách hiển thị
        this.incidents = this.incidents.filter(i => !this.selectedDuplicateIds.includes(i.incidentId));
        
        // Reset danh sách chọn
        this.selectedDuplicateIds = [];
        this.suggestedDuplicates = [];

        // Gọi tiếp hàm update status cho sự cố chính
        this.callUpdateApi(updateDto);
      },
      error: () => {
        this.isLoading = false;
        this.showResultModal(false, 'Lỗi khi gộp sự cố.');
        this.cdr.detectChanges();
      }
    });
  }

  toggleDuplicate(id: number) {
    const index = this.selectedDuplicateIds.indexOf(id);
    if (index > -1) this.selectedDuplicateIds.splice(index, 1);
    else this.selectedDuplicateIds.push(id);
  }

  openConfirmModal(title: string, msg: string, type: 'confirm'|'reject', action: any) {
    this.confirmModal = { show: true, title: title, message: msg, type: type, action: action };
    this.cdr.detectChanges();
  }

  onConfirm() {
    this.confirmModal.show = false;
    this.confirmModal.action();
  }

  showResultModal(isSuccess: boolean, msg: string) {
    this.resultModal = { show: true, isSuccess: isSuccess, message: msg };
    this.cdr.detectChanges();
  }

  closeResultModal() {
    this.resultModal.show = false;
    this.cdr.detectChanges();
  }
}