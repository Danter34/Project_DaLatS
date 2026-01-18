import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Department,  CreateDepartmentDTO } from '../../models/department.model';
import {DepartmentService} from '../../services/department.service';
@Component({
  selector: 'app-departments',
  templateUrl: './departments.component.html',
  styleUrls: ['./departments.component.css'],
  standalone: false
})
export class DepartmentsComponent implements OnInit {
  
  departments: Department[] = [];
  isLoading: boolean = false;

  // --- MODAL FORM ---
  showFormModal: boolean = false;
  formMode: 'create' | 'update' = 'create';
  selectedDept: Department | null = null;
  
  // Form Data
  formData: CreateDepartmentDTO = {
    name: '',
    description: '',
    phoneNumber: ''
  };

  // --- MODAL DELETE ---
  showDeleteModal: boolean = false;
  deptToDelete: Department | null = null;

  // --- MODAL SUCCESS ---
  showSuccessModal: boolean = false;
  successMessage: string = '';

  constructor(
    private deptService: DepartmentService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData() {
    this.isLoading = true;
    this.deptService.getAll().subscribe({
      next: (res) => {
        this.departments = res;
        this.isLoading = false;
        this.cdr.detectChanges(); // Ép cập nhật giao diện
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // === FORM LOGIC ===
  openCreateModal() {
    this.formMode = 'create';
    this.selectedDept = null;
    this.formData = { name: '', description: '', phoneNumber: '' }; // Reset form
    this.showFormModal = true;
  }

  openEditModal(dept: Department) {
    this.formMode = 'update';
    this.selectedDept = dept;
    // Clone dữ liệu để không ảnh hưởng bảng khi đang gõ
    this.formData = { 
      name: dept.name, 
      description: dept.description || '', 
      phoneNumber: dept.phoneNumber || '' 
    };
    this.showFormModal = true;
  }

  closeFormModal() {
    this.showFormModal = false;
  }

  submitForm() {
    if (!this.formData.name.trim()) {
      alert('Tên phòng ban là bắt buộc!');
      return;
    }

    if (this.formMode === 'create') {
      this.deptService.create(this.formData).subscribe({
        next: () => this.handleSuccess('Thêm phòng ban thành công!'),
        error: (err) => alert('Lỗi: ' + err.message)
      });
    } else {
      if (this.selectedDept) {
        this.deptService.update(this.selectedDept.departmentId, this.formData).subscribe({
          next: () => this.handleSuccess('Cập nhật phòng ban thành công!'),
          error: (err) => alert('Lỗi: ' + err.message)
        });
      }
    }
  }

  // === DELETE LOGIC ===
  openDeleteModal(dept: Department) {
    this.deptToDelete = dept;
    this.showDeleteModal = true;
  }

  closeDeleteModal() {
    this.showDeleteModal = false;
    this.deptToDelete = null;
  }

  confirmDelete() {
    if (this.deptToDelete) {
      this.deptService.delete(this.deptToDelete.departmentId).subscribe({
        next: () => {
          this.closeDeleteModal();
          this.handleSuccess('Đã xóa phòng ban thành công!');
        },
        error: (err) => alert('Không thể xóa (có thể đang có nhân viên trong phòng này).')
      });
    }
  }

  // === SUCCESS MODAL ===
  handleSuccess(msg: string) {
    this.showFormModal = false;
    this.successMessage = msg;
    this.showSuccessModal = true;
    this.loadData();
  }

  closeSuccessModal() {
    this.showSuccessModal = false;
  }
}