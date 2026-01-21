namespace SafeDalat_API.Repositories.Interface
{
    public interface IFcmService
    {
        Task SendToTokenAsync(string token, string title, string body);
        Task SendToMultipleTokensAsync(List<string> tokens, string title, string body);
    }
}
