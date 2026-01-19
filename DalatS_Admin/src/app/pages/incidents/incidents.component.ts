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

  // Dữ liệu chính
  incidents: IncidentDTO[] = [];
  selectedIncident: IncidentDTO | null = null;
  
  // BIẾN MỚI: Lưu ảnh đang được hiển thị to
  activeImage: any = null; 

  // Dữ liệu bổ trợ
  departments: DepartmentOption[] = [];
  suggestedDuplicates: IncidentDTO[] = [];
  
  // User info
  currentUser: any = null;
  isAdmin: boolean = false;

  // Form
  isLoading: boolean = false;
  updateData: UpdateStatusDTO = {
    status: '',
    note: '',
    alertLevel: 1,
    assignedDepartmentId: undefined
  };

  selectedDuplicateIds: number[] = [];
  
  // Cấu hình Base URL API (Sửa lại port nếu khác 5084)
  private readonly baseUrl = 'http://localhost:5084'; 

  constructor(
    private incidentService: IncidentService,
    private staffService: StaffService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUser();
    this.isAdmin = this.currentUser?.role === 'Admin';
    this.loadData();
  }

  loadData() {
    this.isLoading = true;
    
    forkJoin({
      incidents: this.incidentService.getAll(),
      depts: this.isAdmin ? this.staffService.getDepartments() : of([]) 
    }).subscribe({
      next: (res) => {
        this.incidents = res.incidents;
        this.departments = res.depts as DepartmentOption[]; 
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
      }
    });
  }

  // --- HELPER FUNCTIONS ---
  
  // Xử lý đường dẫn ảnh
  getImageUrl(filePath: string): string {
    if (!filePath) return 'assets/images/no-image.png';
    if (filePath.startsWith('http')) return filePath;
    const path = filePath.startsWith('/') ? filePath : `/${filePath}`;
    return `${this.baseUrl}${path}`;
  }

  // Xử lý màu sắc trạng thái
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

  // --- INTERACTION ---

  selectIncident(item: IncidentDTO) {
    this.selectedIncident = item;
    
    // Logic chọn ảnh mặc định: Lấy ảnh đầu tiên
    if (item.images && item.images.length > 0) {
      this.activeImage = item.images[0];
    } else {
      this.activeImage = null;
    }

    // Reset Form
    this.updateData = {
      status: item.status,
      alertLevel: item.alertLevel,
      assignedDepartmentId: item.assignedDepartmentId,
      note: ''
    };

    this.suggestedDuplicates = [];
    this.selectedDuplicateIds = [];

    // Nếu Admin & Chờ xử lý -> Load gợi ý trùng
    if (this.isAdmin && item.status === 'Chờ xử lý') {
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
      alert('Vui lòng chọn đơn vị xử lý!');
      return;
    }

    const dto: UpdateStatusDTO = {
      status: 'Đang xử lý',
      alertLevel: this.updateData.alertLevel,
      assignedDepartmentId: this.updateData.assignedDepartmentId,
      note: this.updateData.note || 'Đã duyệt và chuyển đơn vị xử lý'
    };

    if (this.selectedDuplicateIds.length > 0) {
      this.mergeAndApprove(dto);
    } else {
      this.callUpdateApi(dto);
    }
  }

  reject() {
    if (!this.selectedIncident) return;
    if (!confirm('Bạn chắc chắn muốn từ chối sự cố này?')) return;

    const dto: UpdateStatusDTO = {
      status: 'Từ chối',
      note: this.updateData.note || 'Không đủ thông tin hoặc không đúng thẩm quyền'
    };
    this.callUpdateApi(dto);
  }

  updateProgress(status: string) {
    if (!this.selectedIncident) return;
    
    const dto: UpdateStatusDTO = {
      status: status,
      note: this.updateData.note
    };
    this.callUpdateApi(dto);
  }

  callUpdateApi(dto: UpdateStatusDTO) {
    if (!this.selectedIncident) return;
    
    this.isLoading = true;
    this.incidentService.updateStatus(this.selectedIncident.incidentId, dto).subscribe({
      next: () => {
        alert('Cập nhật thành công!');
        this.isLoading = false;
        this.selectedIncident = null; // Clear selection
        this.loadData();
      },
      error: () => {
        alert('Lỗi cập nhật');
        this.isLoading = false;
      }
    });
  }

  mergeAndApprove(updateDto: UpdateStatusDTO) {
    if (!this.selectedIncident) return;

    const mergeDto: MergeIncidentDTO = {
      masterIncidentId: this.selectedIncident.incidentId,
      duplicateIncidentIds: this.selectedDuplicateIds
    };

    this.incidentService.mergeIncidents(mergeDto).subscribe({
      next: () => {
        this.callUpdateApi(updateDto);
      },
      error: () => alert('Lỗi khi gộp sự cố')
    });
  }

  toggleDuplicate(id: number) {
    const index = this.selectedDuplicateIds.indexOf(id);
    if (index > -1) {
      this.selectedDuplicateIds.splice(index, 1);
    } else {
      this.selectedDuplicateIds.push(id);
    }
  }
}