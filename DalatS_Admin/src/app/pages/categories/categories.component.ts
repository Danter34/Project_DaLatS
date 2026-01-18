import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CategoryService } from '../../services/category.service';
import { IncidentCategory } from '../../models/categories.model';

@Component({
  selector: 'app-categories',
  templateUrl: './categories.component.html',
  styleUrls: ['./categories.component.css'],
  standalone: false
})
export class CategoriesComponent implements OnInit {
  
  categories: IncidentCategory[] = [];
  isLoading: boolean = false;

  // --- MODAL FORM (THÊM / SỬA) ---
  showFormModal: boolean = false;
  formMode: 'create' | 'update' = 'create';
  selectedCategory: IncidentCategory | null = null;
  categoryNameInput: string = ''; // Biến binding với input

  // --- MODAL DELETE ---
  showDeleteModal: boolean = false;
  categoryToDelete: IncidentCategory | null = null;

  // --- MODAL SUCCESS ---
  showSuccessModal: boolean = false;
  successMessage: string = '';

  constructor(
    private categoryService: CategoryService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData() {
    this.isLoading = true;
    this.categoryService.getAll().subscribe({
      next: (res) => {
        this.categories = res; // API trả về List<IncidentCategory>
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

  // === LOGIC THÊM / SỬA ===
  openCreateModal() {
    this.formMode = 'create';
    this.selectedCategory = null;
    this.categoryNameInput = '';
    this.showFormModal = true;
  }

  openEditModal(cat: IncidentCategory) {
    this.formMode = 'update';
    this.selectedCategory = cat;
    this.categoryNameInput = cat.name;
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

    const dto = { name: this.categoryNameInput };

    if (this.formMode === 'create') {
      this.categoryService.create(dto).subscribe({
        next: () => this.handleSuccess('Thêm danh mục thành công!'),
        error: (err) => alert('Lỗi khi thêm: ' + err.message)
      });
    } else {
      if (this.selectedCategory) {
        this.categoryService.update(this.selectedCategory.categoryId, dto).subscribe({
          next: () => this.handleSuccess('Cập nhật danh mục thành công!'),
          error: (err) => alert('Lỗi khi cập nhật: ' + err.message)
        });
      }
    }
  }

  // === LOGIC XÓA ===
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
        error: (err) => alert('Không thể xóa danh mục này (có thể đang được sử dụng).')
      });
    }
  }

  // === LOGIC SUCCESS MODAL ===
  handleSuccess(msg: string) {
    this.showFormModal = false; // Đóng form nếu đang mở
    this.successMessage = msg;
    this.showSuccessModal = true;
    this.loadData(); // Tải lại dữ liệu
  }

  closeSuccessModal() {
    this.showSuccessModal = false;
  }
}