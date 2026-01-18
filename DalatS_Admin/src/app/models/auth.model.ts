export interface LoginDTO {
  email: string;
  password: string;
}

export interface User {
  userId: number;
  fullName: string;
  email: string;
  role: string;
}

export interface LoginResponse {
  token: string;
  user: User;
}