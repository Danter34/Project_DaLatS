# Dữ liệu demo DalatS

Bộ dữ liệu giả lập phục vụ chụp màn hình, đã nạp vào database `dalats` theo cấu hình hiện tại. Các tài khoản có sẵn được giữ nguyên.

## Tài khoản

Mật khẩu chung của các tài khoản demo: **`DalatS@Demo2026`**.

| Email | Vai trò / phòng ban |
| --- | --- |
| `admin@demo.dalats.test` | Admin – Nguyễn Minh Anh |
| `staff1@demo.dalats.test` | Hạ tầng giao thông |
| `staff2@demo.dalats.test` | Vệ sinh môi trường |
| `staff3@demo.dalats.test` | Thoát nước đô thị |
| `staff4@demo.dalats.test` | Công viên và cây xanh |
| `staff5@demo.dalats.test` | Chiếu sáng công cộng |
| `user1@demo.dalats.test` đến `user5@demo.dalats.test` | Người dân, đã xác minh email |
| `user6@demo.dalats.test` | Người dân bị khóa, dùng minh họa quản lý tài khoản |

## Nội dung

- 15 sự cố, 5 danh mục, mỗi danh mục 3 sự cố.
- 4 chờ xử lý, 5 đang xử lý, 5 đã hoàn thành, 1 từ chối. Có 10 sự cố công khai trên bản đồ.
- 5 phòng ban giả lập, 1 Admin, 5 Staff và 6 người dân.
- 8 câu hỏi, 5 câu trả lời, 10 bình luận, 16 bản ghi lịch sử và 16 thông báo.
- Thời gian rải trong khoảng một tháng trước ngày chạy seed.
- GPS trong nội thành Đà Lạt: vĩ độ 11.9338–11.9663, kinh độ 108.4248–108.4668. Vị trí được đặt gần khu vực các tuyến đường để demo, không phải đo đạc hiện trường. Tên phường giữ theo danh sách hiện có của ứng dụng.
- 5 ảnh được dùng lại theo đúng nhóm sự cố: ổ gà, rác đổ bên đường, cống tắc, cây đổ, trụ đèn hư hỏng. Đây là ảnh minh họa nước ngoài, không phải ảnh xác nhận sự cố có thật tại Đà Lạt. Ảnh ở các báo cáo đã hoàn thành là ảnh lúc gửi phản ánh.

Ảnh được lưu tại `SafeDalat_API/wwwroot/uploads/incidents/demo/`, tổng dung lượng khoảng 2.5 MB, không cần truy cập mạng khi hiển thị. Nguồn, tác giả và giấy phép từng ảnh có trong [credits.json](SafeDalat_API/wwwroot/uploads/incidents/demo/credits.json). Khi dùng lại ảnh trong tài liệu công khai, kèm phần ghi công tương ứng.

## Chạy lại seed

Từ thư mục gốc `Project_DaLatS`, với SQL Server đang chạy:

```powershell
# Database hiện tại có schema tạo sẵn nhưng không có baseline migration tương ứng:
dotnet run --project SafeDalat_API/SafeDalat_API/SafeDalat_API.csproj --launch-profile http -- --seed-demo --seed-demo-existing-schema

# Database mới: cấu hình ConnectionStrings:SafeDalatConnection trước, rồi chạy:
dotnet run --project SafeDalat_API/SafeDalat_API/SafeDalat_API.csproj --launch-profile http -- --seed-demo
```

Lệnh chỉ chạy trong Development, nạp dữ liệu trong transaction và kết thúc trước khi HTTP server/worker hoạt động. Nếu email Admin demo đã tồn tại, toàn bộ seed được bỏ qua để không nhân đôi dữ liệu hoặc đặt lại mật khẩu. Không xóa dữ liệu cũ và không tự chạy seed khi khởi động bình thường. Cờ `--seed-demo-existing-schema` chỉ nhập dữ liệu, không sửa schema hoặc giả lập lịch sử migration.

Ảnh đã có trong dự án; chỉ cần lệnh sau khi thiếu ảnh:

```powershell
powershell -ExecutionPolicy Bypass -File SafeDalat_API/scripts/Get-DemoImages.ps1
```

## Chụp demo và kiểm tra

Chạy API như bình thường và Web bằng `npm start` trong `DalatS_Admin`. Đăng nhập Admin để chụp dashboard, danh sách sự cố, chi tiết ảnh, phòng ban, người dân và hỏi đáp. Đăng nhập Staff để xem công việc theo phòng ban. Android dùng `user1@demo.dalats.test` để chụp trang chủ, bản đồ, phản ánh cá nhân và thông báo.

Khi API đang chạy:

```powershell
powershell -ExecutionPolicy Bypass -File SafeDalat_API/scripts/Test-Demo.ps1
```

Script kiểm tra đăng nhập, số lượng demo, GPS Đà Lạt, tải ảnh qua HTTP, sự cố trên bản đồ, danh sách Staff, hỏi đáp và dashboard; không cập nhật dữ liệu. Google Maps trên Android vẫn cần API key hợp lệ của dự án.