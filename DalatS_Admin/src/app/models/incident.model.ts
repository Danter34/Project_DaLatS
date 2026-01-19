export enum AlertLevel {
  Green = 1,
  Orange = 2,
  Red = 3
}

export interface IncidentImage {
  imageId: number;
  filePath: string;
}

export interface IncidentDTO {
  incidentId: number;
  title: string;
  description: string;
  address: string;
  ward: string;
  streetName: string;
  latitude: number;
  longitude: number;
  status: string;
  alertLevel: number;
  isMaster: boolean;
  createdAt: string;
  isPublic: boolean;
  userId: number;
  categoryName: string;
  assignedDepartmentId?: number;
  assignedDepartmentName?: string;
  images: IncidentImage[];
}

export interface UpdateStatusDTO {
  status: string;
  note?: string;
  alertLevel?: number;
  assignedDepartmentId?: number;
}

export interface MergeIncidentDTO {
  masterIncidentId: number;
  duplicateIncidentIds: number[];
}