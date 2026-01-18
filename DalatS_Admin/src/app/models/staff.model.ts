// Kế thừa hoặc định nghĩa lại user, thêm thông tin phòng ban
export interface StaffUser {
  userId: number;
  fullName: string;
  email: string;
  role: string;
  isLocked: boolean;
  createdAt: string;
  
  // Thêm thông tin phòng ban
  departmentId?: number; 
  departmentName?: string; 
}
export interface CreateStaffDTO {
  fullName: string;
  email: string;
  password: string;
  role: string;
  departmentId: number | null;
}
