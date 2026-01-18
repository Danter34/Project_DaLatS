export interface Department {
  departmentId: number;
  name: string;
  description?: string;
  phoneNumber?: string;
  staffCount: number; // Trường quan trọng
}

export interface CreateDepartmentDTO {
  name: string;
  description?: string;
  phoneNumber?: string;
}