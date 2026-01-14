namespace SafeDalat_API.Model.DTO.Incident
{
    public class IncidentSearchDTO
    {
        public string? Keyword { get; set; }
        public string? Ward { get; set; }
        public int? CategoryId { get; set; }
        public int? AlertLevel { get; set; }

        // Tìm theo vị trí
        public double? Latitude { get; set; }
        public double? Longitude { get; set; }
        public double? RadiusKm { get; set; }
    }
}
