namespace SafeDalat_API.Model.DTO.Icident
{
    public class UpdateIncidentStatusDTO
    {
        public string Status { get; set; }  
        public string? Note { get; set; }

        public int? AssignedDepartmentId { get; set; }
    }
}
