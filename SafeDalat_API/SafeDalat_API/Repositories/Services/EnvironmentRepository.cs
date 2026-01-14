using SafeDalat_API.Model.DTO.Environment;
using SafeDalat_API.Repositories.Interface;
using System.Text.Json;
namespace SafeDalat_API.Repositories.Services
{
    public class EnvironmentRepository:IEnvironmentRepository
    {
        private readonly HttpClient _http;
        private readonly IConfiguration _config;

        // Tọa độ Đà Lạt
        private const double LAT = 11.9416;
        private const double LON = 108.4583;

        public EnvironmentRepository(HttpClient http, IConfiguration config)
        {
            _http = http;
            _config = config;
        }

        // AIR QUALITY 
        public async Task<AirQualityResponseDTO> GetAirQualityAsync()
        {
            var apiKey = _config["IQAir:ApiKey"];

            var url =
                $"https://api.airvisual.com/v2/nearest_city" +
                $"?lat={LAT}&lon={LON}&key={apiKey}";

            var response = await _http.GetAsync(url);
            response.EnsureSuccessStatusCode();

            var json = await response.Content.ReadAsStringAsync();
            using var doc = JsonDocument.Parse(json);

            var pollution = doc.RootElement
                .GetProperty("data")
                .GetProperty("current")
                .GetProperty("pollution");

            int aqi = pollution.GetProperty("aqius").GetInt32();
            string mainPollutant = pollution.GetProperty("mainus").GetString()!;

            return new AirQualityResponseDTO
            {
                AQI = aqi,
                Level = MapAQILevel(aqi),
                MainPollutant = mainPollutant,
                UpdatedAt = DateTime.UtcNow
            };
        }

        private static string MapAQILevel(int aqi)
        {
            if (aqi <= 50) return "Tốt";
            if (aqi <= 100) return "Trung bình";
            if (aqi <= 150) return "Kém";
            return "Nguy hiểm";
        }

        //  WEATHER 
        public async Task<WeatherResponseDTO> GetWeatherAsync()
        {
            var apiKey = _config["OpenWeather:ApiKey"];

            var url =
                $"https://api.openweathermap.org/data/2.5/weather" +
                $"?lat={LAT}&lon={LON}&units=metric&lang=vi&appid={apiKey}";

            var response = await _http.GetAsync(url);
            response.EnsureSuccessStatusCode();

            var json = await response.Content.ReadAsStringAsync();
            using var doc = JsonDocument.Parse(json);

            return new WeatherResponseDTO
            {
                Temperature = doc.RootElement.GetProperty("main").GetProperty("temp").GetDouble(),
                Humidity = doc.RootElement.GetProperty("main").GetProperty("humidity").GetInt32(),
                Description = doc.RootElement.GetProperty("weather")[0].GetProperty("description").GetString()!,
                WindSpeed = doc.RootElement.GetProperty("wind").GetProperty("speed").GetDouble()
            };
        }
    }
}
