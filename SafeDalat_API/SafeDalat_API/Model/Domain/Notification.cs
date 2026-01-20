using System.ComponentModel.DataAnnotations;

namespace SafeDalat_API.Model.Domain
{
    public class Notification
    {
        [Key]
        public int NotificationId { get; set; }
        public string Message { get; set; } = null!;

        public bool IsRead { get; set; } = false;
        public DateTime CreatedAt { get; set; } = DateTime.Now;

        public int UserId { get; set; }
        public User User { get; set; }
        public string Type { get; set; } = "Info";
    }
}
