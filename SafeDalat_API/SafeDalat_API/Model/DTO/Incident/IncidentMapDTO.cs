namespace SafeDalat_API.Model.DTO.Incident
{
    public class IncidentMapDTO
    {
        public int IncidentId { get; set; }
        public string Title { get; set; } = null!;
        public double Latitude { get; set; }
        public double Longitude { get; set; }
        public string Status { get; set; } = null!;
        public int AlertLevel { get; set; }
        public string CategoryName { get; set; } = null!;
    }
}
