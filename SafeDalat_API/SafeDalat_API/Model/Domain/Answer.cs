using System.ComponentModel.DataAnnotations;

namespace SafeDalat_API.Model.Domain
{
    public class Answer
    {
        [Key]
        public int AnswerId { get; set; }
        public string Content { get; set; } = null!;

        public DateTime CreatedAt { get; set; } = DateTime.Now;

        public int QuestionId { get; set; }
        public Question Question { get; set; }

        public int AdminId { get; set; }
        public User Admin { get; set; }
    }

}
