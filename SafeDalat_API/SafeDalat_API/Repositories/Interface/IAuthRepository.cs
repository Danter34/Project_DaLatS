using SafeDalat_API.Model.Domain;
using SafeDalat_API.Model.DTO.Auth;

namespace SafeDalat_API.Repositories.Interface
{
    public interface IAuthRepository
    {
        string GenerateToken(User user);

        Task<bool> ChangePasswordAsync(int userId, ChangePasswordDTO dto);
        Task<UserProfileDTO> GetProfileAsync(int userId);
        Task<bool> UpdateProfileAsync(int userId, UpdateProfileDTO dto);
        Task<UserDashboardDTO> GetDashboardAsync(int userId);
        Task<List<AdminUserDTO>> GetAllUsersAsync();
        Task<bool> LockUserAsync(int userId);
        Task<bool> UnlockUserAsync(int userId);

    }
}
