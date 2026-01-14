using Microsoft.EntityFrameworkCore;
using SafeDalat_API.Data;
using SafeDalat_API.Model.DTO.Dashboard;
using SafeDalat_API.Repositories.Interface;

namespace SafeDalat_API.Repositories.Services
{
    public class DashboardRepository : IDashboardRepository
    {
        private readonly AppDbContext _context;

        public DashboardRepository(AppDbContext context)
        {
            _context = context;
        }

        public async Task<DashboardSummaryDTO> GetSummaryAsync()
        {
            return new DashboardSummaryDTO
            {
                TongSuCo = await _context.Incidents.CountAsync(),
                ChoXuLy = await _context.Incidents.CountAsync(x => x.Status == "Chờ Xử Lý"),
                DangXuLy = await _context.Incidents.CountAsync(x => x.Status == "Đang Xử Lý"),
                DaHoanThanh = await _context.Incidents.CountAsync(x => x.Status == "Đã hoàn thành"),
                TuChoi = await _context.Incidents.CountAsync(x => x.Status == "Từ Chối")
            };
        }

        public async Task<List<IncidentByAlertDTO>> GetByAlertAsync()
        {
            return await _context.Incidents
                .GroupBy(x => x.AlertLevel)
                .Select(g => new IncidentByAlertDTO
                {
                    AlertName = g.Key.ToString(),
                    Count = g.Count()
                })
                .ToListAsync();
        }

        public async Task<List<IncidentByCategoryDTO>> GetByCategoryAsync()
        {
            return await _context.Incidents
                .Include(x => x.Category)
                .GroupBy(x => x.Category!.Name)
                .Select(g => new IncidentByCategoryDTO
                {
                    CategoryName = g.Key,
                    Count = g.Count()
                })
                .ToListAsync();
        }
    }
}
