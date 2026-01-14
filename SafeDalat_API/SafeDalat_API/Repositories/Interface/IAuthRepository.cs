using SafeDalat_API.Model.Domain;

namespace SafeDalat_API.Repositories.Interface
{
    public interface IAuthRepository
    {
        string GenerateToken(User user);
    }
}
