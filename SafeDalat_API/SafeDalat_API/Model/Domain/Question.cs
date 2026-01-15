using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SafeDalat_API.Model.Domain
{
    public class Question
    {
        [Key]
        public int QuestionId { get; set; }
        public string Content { get; set; } = null!;

        public DateTime CreatedAt { get; set; } = DateTime.Now;

        public int UserId { get; set; }
        public User User { get; set; }


        public int QuestionCategoryId { get; set; }
        public QuestionCategory QuestionCategory { get; set; }


        public int? AssignedDepartmentId { get; set; }
        [ForeignKey("AssignedDepartmentId")]
        public Department? AssignedDepartment { get; set; }

        public ICollection<Answer> Answers { get; set; }
    }
}