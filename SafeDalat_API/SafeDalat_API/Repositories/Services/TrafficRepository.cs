using Microsoft.EntityFrameworkCore;
using SafeDalat_API.Data;
using SafeDalat_API.Model.DTO.TrafficDTO;
using SafeDalat_API.Repositories.Interface;

namespace SafeDalat_API.Repositories.Services
{
    public class TrafficRepository : ITrafficRepository
    {
        private readonly AppDbContext _context;

        public TrafficRepository(AppDbContext context)
        {
            _context = context;
        }

        public async Task<List<TrafficHotspotDTO>> GetHotspotsAsync()
        {
            // 1. Lấy dữ liệu sự cố "tươi" (trong 1 tiếng gần đây)
            // Chỉ lấy các trạng thái: Chờ xử lý, Đang xử lý
            var recentTime = DateTime.UtcNow.AddHours(-1);

            var incidents = await _context.Incidents
                .AsNoTracking()
                .Where(x => x.CreatedAt >= recentTime &&
                           (x.Status == "Chờ xử lý" || x.Status == "Đang xử lý") &&
                           !string.IsNullOrEmpty(x.StreetName))
                .ToListAsync();

            // 2. Gom nhóm theo Tên đường (Logic đơn giản cho sinh viên)
            // Nếu muốn xịn hơn thì gom theo khoảng cách GPS (Haversine), nhưng tên đường là đủ demo
            var hotspots = incidents
                .GroupBy(x => x.StreetName)
                .Where(g => g.Count() >= 2) // QUY TẮC: Từ 2 người báo cáo trở lên mới cảnh báo
                .Select(g => new TrafficHotspotDTO
                {
                    StreetName = g.Key,
                    ReportCount = g.Count(),
                    // Lấy tọa độ của báo cáo mới nhất làm tâm điểm
                    Latitude = g.OrderByDescending(x => x.CreatedAt).First().Latitude,
                    Longitude = g.OrderByDescending(x => x.CreatedAt).First().Longitude,
                    AlertMessage = $"Cảnh báo: {g.Count()} người dùng đã báo cáo sự cố tại đây trong 1 giờ qua."
                })
                .ToList();

            return hotspots;
        }
    }
}