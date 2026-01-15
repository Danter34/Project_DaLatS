using System.ComponentModel.DataAnnotations;

namespace SafeDalat_API.Model.DTO.Department
{
    public class CreateDepartmentDTO
    {
        [Required]
        public string Name { get; set; } = null!;
        public string? Description { get; set; }
        public string? PhoneNumber { get; set; }
    }
}