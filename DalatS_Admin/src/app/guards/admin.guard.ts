import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const adminGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const user = authService.getUser();

  if (user && user.role === 'Admin') {
    return true; // Cho phép vào
  } else {
    // Nếu không phải Admin mà cố vào, đá về trang sự cố
    router.navigate(['/admin/incidents']);
    return false;
  }
};