using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SafeDalat_API.Model.Domain
{
    public class IncidentCategory
    {
        [Key]
        public int CategoryId { get; set; }
        public string Name { get; set; } = null!;
        public int? DefaultDepartmentId { get; set; }

        [ForeignKey("DefaultDepartmentId")]
        public Department? DefaultDepartment { get; set; }
    }
}
