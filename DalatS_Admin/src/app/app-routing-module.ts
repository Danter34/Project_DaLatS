import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { inject } from '@angular/core';
import { AuthService } from './services/auth.service';
import { Router } from '@angular/router';
import { IncidentsComponent } from './pages/incidents/incidents.component';
import { CitizensComponent } from './pages/citizens/citizens.component';
import { StaffComponent } from './pages/staff/staff.component';
import { DepartmentsComponent } from './pages/departments/departments.component';
import { CategoriesComponent } from './pages/categories/categories.component';
import { QaComponent } from './pages/qa/qa.component';
import { adminGuard } from './guards/admin.guard';
import { ProfileComponent } from './pages/profile/profile.component';
// Guard đơn giản (Bạn có thể tách ra file riêng)
const authGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  if (authService.isLoggedIn()) {
    return true;
  } else {
    router.navigate(['/login']);
    return false;
  }
};

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { 
    path: 'admin', 
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      // Chỉ Admin mới vào được Dashboard, Staff, Dept...
      { path: 'dashboard', component: DashboardComponent, canActivate: [adminGuard] },
      { path: 'citizens', component: CitizensComponent, canActivate: [adminGuard] },
      { path: 'staff', component: StaffComponent, canActivate: [adminGuard] },
      { path: 'departments', component: DepartmentsComponent, canActivate: [adminGuard] },
      { path: 'categories', component: CategoriesComponent, canActivate: [adminGuard] },
      
      // Mấy trang này ai vào cũng được (miễn là đã login)
      { path: 'incidents', component: IncidentsComponent },
      { path: 'qa', component: QaComponent },
      { path: 'profile', component: ProfileComponent },
      // Sửa lại redirect mặc định: Nếu gõ /admin không thì cho về incidents cho an toàn
      // Hoặc xử lý logic redirect ở component cha nếu cần
      { path: '', redirectTo: 'incidents', pathMatch: 'full' } 
    ]
  },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }