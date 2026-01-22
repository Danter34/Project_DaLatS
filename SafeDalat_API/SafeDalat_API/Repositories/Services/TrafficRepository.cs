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
            
            // Chỉ lấy các trạng thái: Chờ xử lý, Đang xử lý
            var recentTime = DateTime.UtcNow.AddHours(-1);

            var incidents = await _context.Incidents
                .AsNoTracking()
                .Where(x => x.CreatedAt >= recentTime &&
                           (x.Status == "Chờ xử lý" || x.Status == "Đang xử lý") &&
                           !string.IsNullOrEmpty(x.StreetName))
                .ToListAsync();

          
            var hotspots = incidents
                .GroupBy(x => x.StreetName)
                .Where(g => g.Count() >= 2) 
                .Select(g => new TrafficHotspotDTO
                {
                    StreetName = g.Key,
                    ReportCount = g.Count(),
                   
                    Latitude = g.OrderByDescending(x => x.CreatedAt).First().Latitude,
                    Longitude = g.OrderByDescending(x => x.CreatedAt).First().Longitude,
                    AlertMessage = $"Cảnh báo: {g.Count()} người dùng đã báo cáo sự cố tại đây trong 1 giờ qua."
                })
                .ToList();

            return hotspots;
        }
    }
}