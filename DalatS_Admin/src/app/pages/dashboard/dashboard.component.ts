import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core'; // 1. Import ChangeDetectorRef
import { DashboardService } from '../../services/dashboard.service';
import { DashboardSummary } from '../../models/dashboard.model';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { forkJoin, Subscription, timer } from 'rxjs';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
  standalone: false
})
export class DashboardComponent implements OnInit, OnDestroy {
  
  isLoading: boolean = true; 
  private refreshSubscription!: Subscription;

  summary: DashboardSummary = {
    tongSuCo: 0, choXuLy: 0, dangXuLy: 0, daHoanThanh: 0, tuChoi: 0
  };

  public categoryChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: { y: { beginAtZero: true } }
  };
  public categoryChartType: ChartType = 'bar';
  public categoryChartData: ChartData<'bar'> = {
    labels: [],
    datasets: [{ data: [], label: 'Số lượng', backgroundColor: '#5e72e4' }]
  };

  public alertChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { position: 'bottom' } }
  };
  public alertChartType: ChartType = 'pie';
  public alertChartData: ChartData<'pie'> = {
    labels: [],
    datasets: [{ 
      data: [], 
      backgroundColor: ['#3cff00', '#ff0000', '#ff8800', '#11cdef'] 
    }]
  };

  // 2. Inject ChangeDetectorRef vào constructor
  constructor(
    private dashboardService: DashboardService,
    private cdr: ChangeDetectorRef 
  ) {}

  ngOnInit(): void {
    // Timer: Gọi ngay lập tức (0) và lặp lại mỗi 10s
    this.refreshSubscription = timer(0, 10000).subscribe(() => {
      this.loadAllData();
    });
  }

  ngOnDestroy(): void {
    if (this.refreshSubscription) {
      this.refreshSubscription.unsubscribe();
    }
  }

  loadAllData() {
    forkJoin({
      summary: this.dashboardService.getSummary(),
      alerts: this.dashboardService.getByAlert(),
      categories: this.dashboardService.getByCategory()
    })
    .pipe(
      finalize(() => {
        // Chỉ tắt loading nếu đang loading
        if (this.isLoading) {
          this.isLoading = false;
          this.cdr.detectChanges(); // Ép giao diện cập nhật trạng thái loading
        }
      })
    )
    .subscribe({
      next: (res) => {
        // Gán dữ liệu
        this.summary = res.summary;

        // Chart 1
        this.alertChartData.labels = res.alerts.map(x => x.alertName);
        this.alertChartData.datasets[0].data = res.alerts.map(x => x.count);
        this.alertChartData = { ...this.alertChartData };

        // Chart 2
        this.categoryChartData.labels = res.categories.map(x => x.categoryName);
        this.categoryChartData.datasets[0].data = res.categories.map(x => x.count);
        this.categoryChartData = { ...this.categoryChartData };

        // 3. QUAN TRỌNG NHẤT: Báo cho Angular biết dữ liệu đã thay đổi, hãy render lại đi!
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Lỗi tải dashboard:', err);
        // Nếu lỗi 401 (Hết hạn token), có thể logout tại đây
        if (err.status === 401) {
            // this.authService.logout(); 
        }
        this.isLoading = false; // Đảm bảo tắt loading dù lỗi
        this.cdr.detectChanges();
      }
    });
  }
}