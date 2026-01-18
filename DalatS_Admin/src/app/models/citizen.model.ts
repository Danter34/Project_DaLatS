export interface AdminUser {
  userId: number;
  fullName: string;
  email: string;
  role: string;
  isLocked: boolean;
  emailVerified: boolean;
  createdAt: string;
  incidentCount: number;
}

export interface LockUserDTO {
  reason: string;
}