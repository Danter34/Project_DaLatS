using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SafeDalat_API.Model.Domain
{
    public class QuestionCategory
    {
        [Key]
        public int CategoryId { get; set; }
        public string Name { get; set; } = null!;

        public int? ResponsibleDepartmentId { get; set; }

        [ForeignKey("ResponsibleDepartmentId")]
        public Department? ResponsibleDepartment { get; set; }
    }
}
