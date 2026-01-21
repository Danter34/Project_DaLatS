using SafeDalat_API.Model.DTO.TrafficDTO;
namespace SafeDalat_API.Repositories.Interface
{
    public interface ITrafficRepository
    {
        Task<List<TrafficHotspotDTO>> GetHotspotsAsync();
    }
}
