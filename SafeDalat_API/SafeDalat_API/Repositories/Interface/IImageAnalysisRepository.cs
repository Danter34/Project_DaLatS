namespace SafeDalat_API.Repositories.Interface
{
    public interface IImageAnalysisRepository
    {
        Task<bool> ValidateImageContentAsync(IFormFile imageFile);
    }
}
