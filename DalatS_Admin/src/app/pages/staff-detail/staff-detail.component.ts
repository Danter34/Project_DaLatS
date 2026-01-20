import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { StaffService } from '../../services/staff.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-staff-detail',
  templateUrl: './staff-detail.component.html',
  styleUrls: ['./staff-detail.component.css'],
  standalone: false
})
export class StaffDetailComponent implements OnInit {
  userId: number = 0;
  isLoading: boolean = true;
  isModalVisible: boolean = false;
  selectedIncident: any = null;
  profile: any = null;
  stats: any = null;
  tasks: any[] = []; // Danh sách công việc

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private staffService: StaffService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
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

  this.staffService.getStaffDashboard(this.userId).subscribe({
    next: (res) => {
      this.profile = res.profile;
      this.stats = res.stats;

      // [FIX] NẾU CÓ PHÒNG BAN -> GỌI API LẤY TASK
      if (this.profile.departmentId) {
        this.staffService.getStaffTasks(this.profile.departmentId).subscribe(tasks => {
          this.tasks = tasks;
          this.isLoading = false;
          this.cdr.detectChanges(); // Update giao diện
        });
      } else {
        // Nếu không có phòng ban thì danh sách rỗng
        this.tasks = [];
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    },
    error: (err) => {
      console.error(err);
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  });
}

  goBack() {
    this.router.navigate(['/admin/staff']);
  }
  openIncidentModal(incident: any) {
    this.selectedIncident = incident;
    this.isModalVisible = true;
  }

  closeModal() {
    this.isModalVisible = false;
    this.selectedIncident = null;
  }
}