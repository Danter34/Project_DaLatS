using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema; 

namespace SafeDalat_API.Model.Domain
{
    public class Answer
    {
        [Key]
        public int AnswerId { get; set; }
        public string Content { get; set; } = null!;

        public DateTime CreatedAt { get; set; } = DateTime.Now;

        // Liên kết với Câu hỏi
        public int QuestionId { get; set; }
        public Question Question { get; set; }
        public int ResponderId { get; set; }

        [ForeignKey("ResponderId")]
        public User Responder { get; set; }
    }
}