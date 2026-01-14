using SafeDalat_API.Model.DTO.Environment;

namespace SafeDalat_API.Repositories.Interface
{
    public interface IEnvironmentRepository
    {
        Task<AirQualityResponseDTO> GetAirQualityAsync();
        Task<WeatherResponseDTO> GetWeatherAsync();
    }
}
