import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CategoryService } from '../../services/category.service';
import { DepartmentService } from '../../services/department.service'; 
import { IncidentCategory } from '../../models/categories.model';
import { Department } from '../../models/department.model'; 

@Component({
  selector: 'app-categories',
  templateUrl: './categories.component.html',
  styleUrls: ['./categories.component.css'],
  standalone: false
})
export class CategoriesComponent implements OnInit {
  
  categories: IncidentCategory[] = [];
  departments: Department[] = []; // [MỚI] Danh sách phòng ban
  isLoading: boolean = false;

  // --- MODAL FORM ---
  showFormModal: boolean = false;
  formMode: 'create' | 'update' = 'create';
  selectedCategory: IncidentCategory | null = null;
  
  // Biến form
  categoryNameInput: string = ''; 
  selectedDepartmentId: number | null = null; // [MỚI] Biến binding dropdown

  // --- MODAL DELETE & SUCCESS (Giữ nguyên) ---
  showDeleteModal: boolean = false;
  categoryToDelete: IncidentCategory | null = null;
  showSuccessModal: boolean = false;
  successMessage: string = '';

  constructor(
    private categoryService: CategoryService,
    private departmentService: DepartmentService, // [MỚI] Inject
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadData();
    this.loadDepartments(); // [MỚI] Load phòng ban ngay khi vào
  }

  loadData() {
    this.isLoading = true;
    this.categoryService.getAll().subscribe({
      next: (res) => {
        this.categories = res;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
      }
    });
  }

  // [MỚI] Hàm load phòng ban
  loadDepartments() {
    this.departmentService.getAll().subscribe({
      next: (res) => {
        this.departments = res;
      },
      error: (err) => console.error('Lỗi load phòng ban:', err)
    });
  }

  // === LOGIC THÊM / SỬA ===
  openCreateModal() {
    this.formMode = 'create';
    this.selectedCategory = null;
    this.categoryNameInput = '';
    this.selectedDepartmentId = null; // Reset
    this.showFormModal = true;
  }

  openEditModal(cat: IncidentCategory) {
    this.formMode = 'update';
    this.selectedCategory = cat;
    this.categoryNameInput = cat.name;
    // [MỚI] Gán giá trị cũ vào dropdown (nếu có)
    this.selectedDepartmentId = cat.defaultDepartmentId || null; 
    this.showFormModal = true;
  }

  closeFormModal() {
    this.showFormModal = false;
  }

  submitForm() {
    if (!this.categoryNameInput.trim()) {
      alert('Vui lòng nhập tên danh mục');
      return;
    }

    // [MỚI] Tạo DTO có kèm DepartmentId
    const dto = { 
      name: this.categoryNameInput,
      defaultDepartmentId: this.selectedDepartmentId // Gửi kèm ID phòng ban (hoặc null)
    };

    if (this.formMode === 'create') {
      this.categoryService.create(dto).subscribe({
        next: () => this.handleSuccess('Thêm danh mục thành công!'),
        error: (err) => alert('Lỗi: ' + err.message)
      });
    } else {
      if (this.selectedCategory) {
        this.categoryService.update(this.selectedCategory.categoryId, dto).subscribe({
          next: () => this.handleSuccess('Cập nhật danh mục thành công!'),
          error: (err) => alert('Lỗi: ' + err.message)
        });
      }
    }
  }

  // ... (Các hàm Delete và Success giữ nguyên như code cũ của bạn) ...
  openDeleteModal(cat: IncidentCategory) {
    this.categoryToDelete = cat;
    this.showDeleteModal = true;
  }

  closeDeleteModal() {
    this.showDeleteModal = false;
    this.categoryToDelete = null;
  }

  confirmDelete() {
    if (this.categoryToDelete) {
      this.categoryService.delete(this.categoryToDelete.categoryId).subscribe({
        next: () => {
          this.closeDeleteModal();
          this.handleSuccess('Đã xóa danh mục thành công!');
        },
        error: (err) => alert('Không thể xóa danh mục này.')
      });
    }
  }

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