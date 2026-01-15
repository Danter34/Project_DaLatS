using SafeDalat_API.Model.DTO.Department;

namespace SafeDalat_API.Repositories.Interface
{
    public interface IDepartmentRepository
    {
        Task<List<DepartmentResponseDTO>> GetAllAsync();
        Task<DepartmentResponseDTO?> GetByIdAsync(int id);
        Task<DepartmentResponseDTO> CreateAsync(CreateDepartmentDTO dto);
        Task<DepartmentResponseDTO?> UpdateAsync(int id, UpdateDepartmentDTO dto);
        Task<bool> DeleteAsync(int id);
    }
}