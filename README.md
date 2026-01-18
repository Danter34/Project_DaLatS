# 🌲 DalatS - Hệ thống Quản lý Sự cố Đô thị Đà Lạt

![Status](https://img.shields.io/badge/Status-In%20Development-yellow)
![License](https://img.shields.io/badge/License-MIT-blue)

**DalatS** là giải pháp toàn diện giúp kết nối người dân và chính quyền thành phố Đà Lạt trong việc báo cáo, tiếp nhận và xử lý các sự cố đô thị (hư hỏng hạ tầng, trật tự, vệ sinh, v.v.). Hệ thống bao gồm ứng dụng di động cho người dân và trang quản trị web cho cán bộ quản lý.

## 🚀 Công nghệ sử dụng (Tech Stack)

Dự án được xây dựng theo kiến trúc Client-Server với 3 thành phần chính:

| Thành phần | Công nghệ | Chi tiết |
| :--- | :--- | :--- |
| **Backend API** | ![C#](https://img.shields.io/badge/C%23-239120?style=flat&logo=c-sharp&logoColor=white) ![.NET](https://img.shields.io/badge/.NET%20Core-512BD4?style=flat&logo=dotnet&logoColor=white) | **ASP.NET Core Web API**, Entity Framework Core, SQL Server, JWT Authentication, BCrypt. |
| **Web Admin** | ![Angular](https://img.shields.io/badge/Angular-DD0031?style=flat&logo=angular&logoColor=white) ![TypeScript](https://img.shields.io/badge/TypeScript-007ACC?style=flat&logo=typescript&logoColor=white) | **Angular 16+**, Chart.js (Thống kê), Bootstrap/CSS custom, RxJS. |
| **Mobile App** | ![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=openjdk&logoColor=white) ![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white) | **Android Native (Java)**, XML Layout, Retrofit (API Call), MVVM Pattern. |

---

## 🔑 Tính năng chính (Features)

### 📱 Mobile App (Dành cho Người dân)
* **Đăng ký/Đăng nhập:** Xác thực tài khoản, xác minh email.
* **Gửi phản ánh:** Chụp ảnh, chọn vị trí, chọn danh mục sự cố và gửi báo cáo.
* **Theo dõi trạng thái:** Xem lịch sử xử lý của cơ quan chức năng (Đang xử lý, Đã xong...).
* **Thông báo:** Nhận thông báo khi trạng thái phản ánh thay đổi.

### 💻 Web Admin (Dành cho Quản trị viên & Nhân viên)
* **Dashboard:** Thống kê trực quan số lượng sự cố theo thời gian, danh mục, mức độ cảnh báo (Biểu đồ cột, tròn).
* **Quản lý Sự cố:** Tiếp nhận, điều phối, cập nhật trạng thái xử lý sự cố.
* **Quản lý Người dùng:**
    * Người dân: Xem danh sách, khóa/mở khóa tài khoản vi phạm.
    * Nhân viên: Thêm mới, phân bổ phòng ban, phân quyền (Admin, Manager, Staff).
* **Quản lý Danh mục & Phòng ban:** CRUD danh mục sự cố và cơ cấu tổ chức.
