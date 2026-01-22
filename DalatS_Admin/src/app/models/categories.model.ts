export interface IncidentCategory {
  categoryId: number;
  name: string;
  defaultDepartmentId?: number; 
  defaultDepartmentName?: string;
}

export interface CreateCategoryDTO {
  name: string;
  defaultDepartmentId?: number | null;
}