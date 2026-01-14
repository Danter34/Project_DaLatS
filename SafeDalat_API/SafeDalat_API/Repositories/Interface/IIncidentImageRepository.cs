namespace SafeDalat_API.Repositories.Interface
{
    public interface IIncidentImageRepository
    {
        Task<List<string>> UploadAsync(int incidentId, List<IFormFile> files);
    }
}
