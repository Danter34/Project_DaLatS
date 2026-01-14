using System.ComponentModel.DataAnnotations;

namespace SafeDalat_API.Model.Domain
{
    public class IncidentComment
    {
        [Key]
        public int CommentId { get; set; }

        public string Content { get; set; } = null!;

        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        public int IncidentId { get; set; }
        public Incident Incident { get; set; }

        public int UserId { get; set; }
        public User User { get; set; }
    }
}
