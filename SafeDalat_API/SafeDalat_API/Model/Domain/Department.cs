using System.ComponentModel.DataAnnotations;

namespace SafeDalat_API.Model.Domain
{
    public class Department
    {
        [Key]
        public int DepartmentId { get; set; }
        public string Name { get; set; } = null!; 
        public string? Description { get; set; }
        public string? PhoneNumber { get; set; } 

        public ICollection<User> Staffs { get; set; }

        public ICollection<Incident> AssignedIncidents { get; set; }

        public ICollection<Question> AssignedQuestions { get; set; }
    }
}