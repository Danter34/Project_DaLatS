using SafeDalat_API.Model.DTO.Dashboard;

namespace SafeDalat_API.Repositories.Interface
{
    public interface IDashboardRepository
    {
        Task<DashboardSummaryDTO> GetSummaryAsync();
        Task<List<IncidentByAlertDTO>> GetByAlertAsync();
        Task<List<IncidentByCategoryDTO>> GetByCategoryAsync();
    }
}
