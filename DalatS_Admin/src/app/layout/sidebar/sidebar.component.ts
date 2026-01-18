import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/auth.model';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css'],
  standalone: false
})
export class SidebarComponent implements OnInit {
  role: string = '';
  
  // Biến tiện ích để check nhanh bên HTML
  isAdmin: boolean = false;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    const user = this.authService.getUser();
    if (user) {
      this.role = user.role; // "Admin", "Manager", hoặc "Staff"
      this.isAdmin = this.role === 'Admin';
    }
  }

  logout() {
    this.authService.logout();
  }
} 