using SafeDalat_API.Model.DTO;
using SafeDalat_API.Model.DTO.Icident;
using SafeDalat_API.Model.DTO.Incident;

namespace SafeDalat_API.Repositories.Interface
{
    public interface IIncidentRepository
    {
        Task<IncidentResponseDTO> CreateAsync(int userId, CreateIncidentDTO dto);

        Task<List<IncidentResponseDTO>> GetByUserAsync(int userId);
        Task<List<IncidentResponseDTO>> GetAllAsync();

        Task<IncidentResponseDTO?> GetDetailAsync(int id);

        Task<bool> UpdateStatusAsync(int incidentId, int responderId, UpdateIncidentStatusDTO dto);
        Task<List<IncidentFeedDTO>> GetPublicFeedAsync();

        Task<bool> MergeAsync(MergeIncidentDTO dto, int responderId);
        Task<List<IncidentResponseDTO>> SuggestDuplicatesAsync(int incidentId);
        Task<List<IncidentMapDTO>> GetMapAsync();
        Task<List<IncidentFeedDTO>> SearchAsync(IncidentSearchDTO dto);

        Task<int?> GetUserDepartmentIdAsync(int userId);
        Task<List<IncidentResponseDTO>> GetByDepartmentAsync(int departmentId);
    }
}
