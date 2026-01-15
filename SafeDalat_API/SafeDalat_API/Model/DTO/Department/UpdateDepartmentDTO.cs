namespace SafeDalat_API.Model.DTO.Department
{
    public class UpdateDepartmentDTO
    {
        public string Name { get; set; } = null!;
        public string? Description { get; set; }
        public string? PhoneNumber { get; set; }
    }
}