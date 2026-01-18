import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

// --- 1. Thay đổi import cho ng2-charts v6+ ---
import { BaseChartDirective, provideCharts, withDefaultRegisterables } from 'ng2-charts';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { LoginComponent } from './pages/login/login.component';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { SidebarComponent } from './layout/sidebar/sidebar.component';
import { HeaderComponent } from './layout/header/header.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { CitizensComponent } from './pages/citizens/citizens.component';
import { StaffComponent } from './pages/staff/staff.component';
import { DepartmentsComponent } from './pages/departments/departments.component';
import { IncidentsComponent } from './pages/incidents/incidents.component';
import { CategoriesComponent } from './pages/categories/categories.component';
import { QaComponent } from './pages/qa/qa.component';
import { ProfileComponent } from './pages/profile/profile.component';

@NgModule({
  declarations: [
    App,
    LoginComponent,
    MainLayoutComponent,
    SidebarComponent,
    HeaderComponent,
    DashboardComponent,
    CitizensComponent,
    StaffComponent,
    DepartmentsComponent,
    IncidentsComponent,
    CategoriesComponent,
    QaComponent,
    ProfileComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    RouterModule,
    

    BaseChartDirective 
  ],
  providers: [

    provideCharts(withDefaultRegisterables())
  ],
  bootstrap: [App]
})
export class AppModule { }