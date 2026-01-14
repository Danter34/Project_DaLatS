namespace SafeDalat_API.Model.DTO.Notification
{
    public class NotificationDTO
    {
        public int NotificationId { get; set; }
        public string Message { get; set; } = null!;
        public bool IsRead { get; set; }
        public DateTime CreatedAt { get; set; }
    }
}
