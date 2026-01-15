using SafeDalat_API.Model.DTO.Incident; // Sửa namespace import nếu cần

namespace SafeDalat_API.Model.DTO.Icident
{
    public class IncidentResponseDTO
    {
        public int IncidentId { get; set; }
        public string Title { get; set; }
        public string Description { get; set; }
        public string Address { get; set; }
        public string Ward { get; set; }
        public string StreetName { get; set; }
        public double Latitude { get; set; }
        public double Longitude { get; set; }
        public string Status { get; set; }
        public int AlertLevel { get; set; }
        public bool IsMaster { get; set; }
        public DateTime CreatedAt { get; set; }
        public bool IsPublic { get; set; }
        public int UserId { get; set; }
        public string CategoryName { get; set; }

        public string? AssignedDepartmentName { get; set; }
        public int? AssignedDepartmentId { get; set; }

        public List<IncidentImageDTO> Images { get; set; }
    }
}