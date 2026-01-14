namespace SafeDalat_API.Model.DTO.Environment
{
    public class AirQualityResponseDTO
    {
        public int AQI { get; set; }
        public string Level { get; set; } = null!;
        public string MainPollutant { get; set; } = null!;
        public DateTime UpdatedAt { get; set; }
    }
}
