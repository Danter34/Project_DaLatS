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

        Task<bool> UpdateStatusAsync(int incidentId, int adminId, UpdateIncidentStatusDTO dto);
        Task<List<IncidentFeedDTO>> GetPublicFeedAsync();

        Task<bool> MergeAsync(MergeIncidentDTO dto, int adminId);
        Task<List<IncidentResponseDTO>> SuggestDuplicatesAsync(int incidentId);
        Task<List<IncidentMapDTO>> GetMapAsync();
        Task<List<IncidentFeedDTO>> SearchAsync(IncidentSearchDTO dto);
    }
}
