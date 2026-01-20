using Microsoft.EntityFrameworkCore;
using SafeDalat_API.Data;
using SafeDalat_API.Model.Domain;
using SafeDalat_API.Repositories.Interface;

namespace SafeDalat_API.Repositories.Services
{
    public class IncidentImageRepository : IIncidentImageRepository
    {
        private readonly AppDbContext _context;
        private readonly IWebHostEnvironment _env;

        // Service AI (Đã đăng ký trong Program.cs)
        private readonly IImageAnalysisRepository _imageAnalysis;

        public IncidentImageRepository(
            AppDbContext context,
            IWebHostEnvironment env,
            IImageAnalysisRepository imageAnalysis)
        {
            _context = context;
            _env = env;
            _imageAnalysis = imageAnalysis;
        }

        public async Task<List<string>> UploadAsync(int incidentId, List<IFormFile> files)
        {
            var incident = await _context.Incidents
                 .Include(x => x.User) // <--- Quan trọng: Phải Include User mới sửa được điểm
                 .FirstOrDefaultAsync(x => x.IncidentId == incidentId);
            if (incident == null) throw new Exception("Không tìm thấy sự cố để đính kèm ảnh.");
            if (incident.User == null) throw new Exception("Không tìm thấy người dùng tạo sự cố.");

            foreach (var file in files)
            {
                if (file.Length > 0)
                {
                    bool isValid = await _imageAnalysis.ValidateImageContentAsync(file);

                    if (!isValid)
                    {
                        // --- XỬ LÝ PHẠT ---
                        incident.User.TrustScore -= 5;          // Trừ 5 điểm uy tín
                        incident.User.ConsecutiveViolations++;  // Tăng đếm vi phạm

                        string errorMsg = $"Ảnh vi phạm tiêu chuẩn cộng đồng. Bạn bị trừ 5 điểm uy tín.";

                        // Kiểm tra ngưỡng khóa tạm thời (Ví dụ: 3 lần liên tiếp)
                        if (incident.User.ConsecutiveViolations >= 3)
                        {
                            // Khóa 1 giờ
                            incident.User.LockUntil = DateTime.UtcNow.AddHours(1);
                            incident.User.ConsecutiveViolations = 0; // Reset đếm
                            errorMsg = "Tài khoản bị KHÓA TẠM THỜI (1 giờ) do vi phạm liên tục.";
                        }

                        // --- ROLLBACK: XÓA SỰ CỐ ---
                        _context.Incidents.Remove(incident);

                        // Lưu thay đổi (Cập nhật User + Xóa Incident)
                        await _context.SaveChangesAsync();

                        // Ném lỗi ra Controller
                        throw new Exception(errorMsg);
                    }
                }
            }


            // Nếu user gửi ảnh sạch, reset chuỗi vi phạm về 0 để tránh phạt oan lần sau
            if (incident.User.ConsecutiveViolations > 0)
            {
                incident.User.ConsecutiveViolations = 0;
            }

            var uploadFolder = Path.Combine(_env.WebRootPath, "uploads", "incidents");
            Directory.CreateDirectory(uploadFolder);

            var result = new List<string>();

            foreach (var file in files)
            {
                var fileName = $"{Guid.NewGuid()}{Path.GetExtension(file.FileName)}";
                var filePath = Path.Combine(uploadFolder, fileName);

                using var stream = new FileStream(filePath, FileMode.Create);
                await file.CopyToAsync(stream);

                _context.IncidentImages.Add(new IncidentImage
                {
                    IncidentId = incidentId,
                    FileName = file.FileName,
                    FilePath = $"/uploads/incidents/{fileName}",
                    ContentType = file.ContentType,
                    FileSize = file.Length
                });

                result.Add($"/uploads/incidents/{fileName}");
            }

            await _context.SaveChangesAsync();
            return result;
        }
    }
}