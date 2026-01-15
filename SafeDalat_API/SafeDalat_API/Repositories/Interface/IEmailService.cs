namespace SafeDalat_API.Repositories.Interface
{
    public interface IEmailService
    {
        Task SendVerifyEmail(string email, string token);
        Task SendResetCode(string email, string code);
    }
}
