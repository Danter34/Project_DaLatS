using Microsoft.EntityFrameworkCore;
using SafeDalat_API.Model.Domain;

namespace SafeDalat_API.Data;

/// <summary>Explicit, transactional demo import; never invoked during normal API startup.</summary>
public static class DemoSeeder
{
    public static async Task SeedAsync(AppDbContext db, string contentRoot, bool existingSchema = false)
    {
        const string marker = "admin@demo.dalats.test";
        var imageNames = new[] { "pothole", "rubbish", "drain", "tree", "light" };
        foreach (var name in imageNames)
            if (!File.Exists(Path.Combine(contentRoot, "wwwroot", "uploads", "incidents", "demo", name + ".jpg")))
                throw new InvalidOperationException($"Missing demo image: {name}. Run scripts/Get-DemoImages.ps1 first.");

        // Some local databases were created manually and have no EF migration baseline.
        // Explicit opt-out imports data only; it never fabricates migration history.
        if (!existingSchema)
            await db.Database.MigrateAsync();
        await using var transaction = await db.Database.BeginTransactionAsync(System.Data.IsolationLevel.Serializable);
        if (await db.Users.AnyAsync(x => x.Email == marker))
        {
            Console.WriteLine("Demo data already imported; no changes made.");
            return;
        }
        var now = DateTime.UtcNow;
        var password = BCrypt.Net.BCrypt.HashPassword("DalatS@Demo2026");
        var departmentNames = new[] { "Đội Quản lý hạ tầng giao thông", "Đội Vệ sinh môi trường", "Đội Thoát nước đô thị", "Đội Công viên và cây xanh", "Đội Chiếu sáng công cộng" };
        var categoryNames = new[] { "Hư hỏng mặt đường", "Rác thải và vệ sinh", "Thoát nước và ngập úng", "Cây xanh nguy hiểm", "Chiếu sáng công cộng" };
        var departments = new List<Department>();
        var categories = new List<IncidentCategory>();
        for (int i = 0; i < 5; i++)
        {
            var department = await db.Departments.FirstOrDefaultAsync(x => x.Name == departmentNames[i]);
            if (department == null)
            {
                department = new Department { Name = departmentNames[i], Description = "Đơn vị giả lập phục vụ demo DalatS: tiếp nhận, khảo sát và xử lý phản ánh thuộc chuyên môn.", PhoneNumber = null };
                db.Departments.Add(department);
            }
            departments.Add(department);
            var category = await db.IncidentCategories.FirstOrDefaultAsync(x => x.Name == categoryNames[i]);
            if (category == null)
            {
                category = new IncidentCategory { Name = categoryNames[i] };
                db.IncidentCategories.Add(category);
            }
            categories.Add(category);
        }
        User MakeUser(string name, string email, string role, Department? department = null, bool locked = false) => new()
        {
            FullName = name, Email = email, Password = password, Role = role,
            Department = department, IsLocked = locked, EmailVerified = true,
            TrustScore = locked ? -10 : 80, CreatedAt = now.AddDays(-90)
        };
        var admin = MakeUser("Nguyễn Minh Anh", marker, "Admin");
        var staffNames = new[] { "Trần Quốc Huy", "Lê Thị Thu Hà", "Phạm Đức Minh", "Võ Thanh Tùng", "Đặng Ngọc Linh" };
        var staff = Enumerable.Range(0, 5).Select(i => MakeUser(staffNames[i], $"staff{i + 1}@demo.dalats.test", "Staff", departments[i])).ToArray();
        var citizenNames = new[] { "Nguyễn Hoàng Nam", "Trần Ngọc Mai", "Lê Minh Khang", "Phạm Thảo Vy", "Đỗ Gia Bảo", "Bùi Thanh Trúc" };
        var citizens = Enumerable.Range(0, 6).Select(i => MakeUser(citizenNames[i], $"user{i + 1}@demo.dalats.test", "User", locked: i == 5)).ToArray();
        db.Users.Add(admin);
        db.Users.AddRange(staff);
        db.Users.AddRange(citizens);
        await db.SaveChangesAsync();

        // Each group has three reports and a matching illustrative photograph.
        var titles = new[] {
            "Ổ gà trên đường Bùi Thị Xuân gây khó khăn cho xe máy", "Mặt đường Phan Đình Phùng bong tróc sau mưa", "Ổ gà gần lề đường Nguyễn Công Trứ",
            "Rác sinh hoạt tập kết bên đường Hoàng Văn Thụ", "Rác bị đổ không đúng nơi quy định trên đường Yersin", "Rác tồn đọng tại lối đi đường Ba Tháng Hai",
            "Miệng cống đường Lê Đại Hành bị rác che kín", "Cống thoát nước đường Trần Phú bị tắc", "Rác và lá cây gây nghẹt cống đường Hùng Vương",
            "Cây đổ chắn lối đi đường Trần Hưng Đạo", "Cây gãy sau mưa trên đường Phù Đổng Thiên Vương", "Cây đổ sát lòng đường Hoàng Văn Thụ",
            "Trụ đèn đường Nguyễn Công Trứ bị nghiêng", "Trụ đèn chiếu sáng đường Bùi Thị Xuân bị cong", "Đèn công cộng đường Yersin cần sửa chữa"
        };
        var streets = new[] { "Bùi Thị Xuân", "Phan Đình Phùng", "Nguyễn Công Trứ", "Hoàng Văn Thụ", "Yersin", "Ba Tháng Hai", "Lê Đại Hành", "Trần Phú", "Hùng Vương", "Trần Hưng Đạo", "Phù Đổng Thiên Vương", "Hoàng Văn Thụ", "Nguyễn Công Trứ", "Bùi Thị Xuân", "Yersin" };
        var wards = new[] { 2, 2, 8, 5, 10, 1, 1, 3, 9, 10, 8, 5, 8, 2, 10 };
        var coordinates = new (double Lat, double Lng)[] { (11.9488,108.4398), (11.9477,108.4341), (11.9558,108.4383), (11.9338,108.4248), (11.9418,108.4522), (11.9400,108.4330), (11.9394,108.4361), (11.9358,108.4386), (11.9465,108.4668), (11.9385,108.4572), (11.9663,108.4448), (11.9350,108.4270), (11.9553,108.4390), (11.9510,108.4405), (11.9423,108.4513) };
        if (coordinates.Any(p => p.Lat < 11.92 || p.Lat > 11.98 || p.Lng < 108.41 || p.Lng > 108.48))
            throw new InvalidOperationException("All demo coordinates must be within the Da Lat urban demo area.");
        var statuses = new[] { "Chờ xử lý", "Đang xử lý", "Đã hoàn thành", "Đang xử lý", "Đã hoàn thành", "Chờ xử lý", "Đang xử lý", "Đã hoàn thành", "Chờ xử lý", "Đang xử lý", "Chờ xử lý", "Đã hoàn thành", "Đã hoàn thành", "Đang xử lý", "Từ chối" };
        var descriptions = new[] {
            "Mặt đường có hố lõm, mép nhựa bong vỡ; xe máy phải tránh sang bên cạnh. Đề nghị kiểm tra và vá mặt đường để bảo đảm an toàn.",
            "Rác bị đổ thành đống bên lối đi, ảnh hưởng vệ sinh và việc đi lại. Đề nghị thu gom và nhắc nhở việc bỏ rác đúng nơi quy định.",
            "Rác và lá cây che miệng thu nước, nước rút chậm khi có mưa. Đề nghị vệ sinh miệng cống và kiểm tra đường thoát nước.",
            "Cây bị đổ sau mưa, cành và thân cây cản trở lối đi. Đề nghị khoanh vùng và cắt dọn để người dân lưu thông an toàn.",
            "Trụ đèn chiếu sáng bị cong nghiêng, cần kiểm tra và sửa chữa. Đề nghị đơn vị chuyên môn xử lý để bảo đảm an toàn và chiếu sáng buổi tối."
        };
        var incidents = new List<Incident>();
        for (int i = 0; i < 15; i++)
        {
            int group = i / 3;
            var created = now.AddDays(-2 - i * 2).AddHours(-i);
            var incident = new Incident {
                Title = titles[i], Description = descriptions[group], Address = $"Khu vực đường {streets[i]}, Phường {wards[i]}, Đà Lạt",
                Ward = $"Phường {wards[i]}", StreetName = streets[i], Latitude = coordinates[i].Lat, Longitude = coordinates[i].Lng,
                User = citizens[i % 5], Category = categories[group], Status = statuses[i],
                AlertLevel = group == 3 ? AlertLevel.Red : i % 3 == 2 ? AlertLevel.Green : AlertLevel.Orange,
                IsPublic = statuses[i] is "Đang xử lý" or "Đã hoàn thành", IsMaster = true, CreatedAt = created,
                AssignedDepartment = statuses[i] == "Chờ xử lý" ? null : departments[group]
            };
            db.Incidents.Add(incident);
            incidents.Add(incident);
            var imageName = imageNames[group] + ".jpg";
            db.IncidentImages.Add(new IncidentImage {
                Incident = incident, FileName = imageName, FilePath = "/uploads/incidents/demo/" + imageName,
                ContentType = "image/jpeg", FileSize = new FileInfo(Path.Combine(contentRoot, "wwwroot", "uploads", "incidents", "demo", imageName)).Length, UploadedAt = created.AddMinutes(1)
            });
            if (statuses[i] != "Chờ xử lý")
            {
                db.IncidentStatusHistories.Add(new IncidentStatusHistory { Incident = incident, Admin = admin,
                    Status = statuses[i] == "Từ chối" ? "Từ chối" : "Đang xử lý", Note = statuses[i] == "Từ chối" ? "Thông tin vị trí chưa đủ rõ, đề nghị bổ sung số trụ đèn." : "Đã kiểm tra thông tin và chuyển đơn vị chuyên môn khảo sát.", UpdatedAt = created.AddHours(4) });
                if (statuses[i] == "Đã hoàn thành")
                    db.IncidentStatusHistories.Add(new IncidentStatusHistory { Incident = incident, Admin = staff[group], Status = "Đã hoàn thành", Note = "Đơn vị đã xử lý và kiểm tra lại hiện trường.", UpdatedAt = created.AddDays(1) });
                db.Notifications.Add(new Notification { User = incident.User, Message = $"Phản ánh “{incident.Title}” đã được cập nhật: {incident.Status}.", Type = "System", IsRead = i % 2 == 0, CreatedAt = created.AddDays(1) });
                if (incident.IsPublic)
                    db.IncidentComments.Add(new IncidentComment { Incident = incident, User = citizens[(i + 1) % 5], Content = statuses[i] == "Đã hoàn thành" ? "Cảm ơn đơn vị đã xử lý, việc đi lại đã thuận tiện hơn." : "Mong đơn vị sớm xử lý. Khu vực này có nhiều người qua lại.", CreatedAt = created.AddDays(1) });
            }
        }
        var questionTexts = new[] { "Tôi cần cung cấp thông tin gì khi báo ổ gà trên đường?", "Tôi nên phản ánh rác bị đổ bên đường vào danh mục nào?", "Miệng cống thường xuyên bị nghẹt khi trời mưa thì phản ánh ở đâu?", "Phát hiện cây nghiêng hoặc gãy sau mưa cần gửi thông tin gì?", "Đèn đường gần nhà bị hỏng, tôi có cần ghi số trụ đèn không?" };
        var answerTexts = new[] { "Anh/chị vui lòng gửi ảnh hiện trạng, chọn vị trí trên bản đồ và mô tả đoạn đường bị hư hỏng. Đội hạ tầng sẽ tiếp nhận để khảo sát.", "Anh/chị chọn danh mục Rác thải và vệ sinh, đính kèm ảnh và vị trí tập kết rác để đội vệ sinh kiểm tra, thu gom.", "Anh/chị chọn danh mục Thoát nước và ngập úng, ghi rõ vị trí miệng cống và thời điểm thường xảy ra đọng nước.", "Anh/chị vui lòng gửi ảnh từ vị trí an toàn, đánh dấu vị trí cây và mô tả phần đường bị cản trở để đội cây xanh kiểm tra.", "Số trụ đèn giúp xác định nhanh vị trí. Nếu không thấy số trụ, anh/chị có thể gửi ảnh cùng địa chỉ hoặc tọa độ trên bản đồ." };
        for (int i = 0; i < 5; i++)
        {
            var topic = await db.QuestionCategories.FirstOrDefaultAsync(x => x.Name == categoryNames[i]);
            if (topic == null) { topic = new QuestionCategory { Name = categoryNames[i], ResponsibleDepartment = departments[i] }; db.QuestionCategories.Add(topic); }
            var question = new Question { Content = questionTexts[i], User = citizens[i], QuestionCategory = topic, AssignedDepartment = departments[i], CreatedAt = now.AddDays(-10 + i) };
            db.Questions.Add(question);
            db.Answers.Add(new Answer { Question = question, Responder = staff[i], Content = answerTexts[i], CreatedAt = question.CreatedAt.AddHours(3) });
            if (i < 3)
                db.Questions.Add(new Question { Content = $"Tôi đã gửi phản ánh về {categoryNames[i].ToLowerInvariant()}. Tôi có thể theo dõi tiến độ xử lý trên ứng dụng như thế nào?", User = citizens[0], QuestionCategory = topic, AssignedDepartment = departments[i], CreatedAt = now.AddHours(-12 - i) });
            db.Notifications.Add(new Notification { User = staff[i], Message = $"Có phản ánh mới thuộc {departments[i].Name}. Vui lòng kiểm tra danh sách nhiệm vụ.", Type = "System", CreatedAt = now.AddHours(-3), IsRead = false });
        }
        await db.SaveChangesAsync();
        await transaction.CommitAsync();
        Console.WriteLine("Demo imported: 15 incidents, 5 departments/categories, 12 accounts, 8 questions, 5 answers, images, comments, histories and notifications.");
    }
}
