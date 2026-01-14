namespace SafeDalat_API.Model.DTO.Environment
{
    public class WeatherResponseDTO
    {
        public double Temperature { get; set; }
        public int Humidity { get; set; }
        public string Description { get; set; } = null!;
        public double WindSpeed { get; set; }
    }
}
