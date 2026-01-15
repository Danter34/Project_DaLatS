using SafeDalat_API.Model.DTO.Icident;

namespace SafeDalat_API.Model.DTO.Incident
{
    public class IncidentFeedDTO
    {
        public int IncidentId { get; set; }
        public string Title { get; set; } = null!;
        public string Description { get; set; } = null!;
        public string Ward { get; set; } = null!;
        public string StreetName { get; set; } = null!;
        public double Latitude { get; set; }
        public double Longitude { get; set; }
        public string Status { get; set; } = null!;
        public int AlertLevel { get; set; }
        public DateTime CreatedAt { get; set; }
        public string CategoryName { get; set; } = null!;

        public string? AssignedDepartmentName { get; set; }

        public List<IncidentImageDTO> Images { get; set; } = new();
    }
}