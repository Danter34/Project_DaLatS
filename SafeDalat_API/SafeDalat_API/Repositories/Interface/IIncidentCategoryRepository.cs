using SafeDalat_API.Model.DTO.IncidentCategory;
using SafeDalat_API.Model.Domain;
namespace SafeDalat_API.Repositories.Interface
{
    public interface IIncidentCategoryRepository
    {
        Task<List<IncidentCategory>> GetAllAsync();
        Task<IncidentCategory?> GetByIdAsync(int id);

        Task<IncidentCategory> CreateAsync(CreateIncidentCategoryDTO dto);
        Task<IncidentCategory?> UpdateAsync(int id,CreateIncidentCategoryDTO dto);
        Task<bool> DeleteAsync(int id);
    }
}
