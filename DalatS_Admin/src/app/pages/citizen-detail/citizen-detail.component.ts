import { Component, OnInit, ChangeDetectorRef } from '@angular/core'; // [FIX] Import ChangeDetectorRef
import { ActivatedRoute, Router } from '@angular/router';
import { CitizenService } from '../../services/citizen.service';
import { forkJoin } from 'rxjs'; // [FIX] Import forkJoin

@Component({
  selector: 'app-citizen-detail',
  templateUrl: './citizen-detail.component.html',
  styleUrls: ['./citizen-detail.component.css'],
  standalone: false
})
export class CitizenDetailComponent implements OnInit {
  userId: number = 0;
  isLoading: boolean = false;
  isModalVisible: boolean = false;
  selectedIncident: any = null;

  // Dữ liệu hiển thị
  profile: any = null;
  stats: any = null;
  incidents: any[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private citizenService: CitizenService,
    private cdr: ChangeDetectorRef // [FIX] Inject CDR
  ) {}

  ngOnInit(): void {
    // [FIX] Dùng subscribe paramMap để bắt sự thay đổi ID chuẩn hơn snapshot
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.userId = Number(id);
        this.loadData();
      }
    });
  }

  loadData() {
    this.isLoading = true;

    // [FIX] Dùng forkJoin để gọi cả 2 API xong mới render
    forkJoin({
      info: this.citizenService.getUserDetail(this.userId),
      history: this.citizenService.getUserIncidents(this.userId)
    }).subscribe({
      next: (result) => {
        // Gán dữ liệu
        this.profile = result.info.profile;
        this.stats = result.info.stats;
        this.incidents = result.history;

        this.isLoading = false;
        
        // [QUAN TRỌNG] Ép Angular vẽ lại giao diện ngay lập tức
        this.cdr.detectChanges(); 
      },
      error: (err) => {
        console.error('Lỗi tải dữ liệu chi tiết:', err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // Helper: Màu trạng thái
  getStatusClass(status: string): string {
    switch (status) {
      case 'Chờ xử lý': return 'status-pending';
      case 'Đang xử lý': return 'status-processing';
      case 'Đã hoàn thành': return 'status-completed';
      case 'Từ chối': return 'status-rejected';
      case 'Đã gộp': return 'status-merged';
      default: return '';
    }
  }

  goBack() {
    this.router.navigate(['/admin/citizens']);
  }
  openIncidentModal(incident: any) {
    this.selectedIncident = incident;
    this.isModalVisible = true;
  }

  // HÀM ĐÓNG MODAL
  closeModal() {
    this.isModalVisible = false;
    this.selectedIncident = null;
  }
}