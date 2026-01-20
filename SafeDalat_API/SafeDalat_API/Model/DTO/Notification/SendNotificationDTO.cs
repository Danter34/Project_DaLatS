namespace SafeDalat_API.Model.DTO.Notification
{
    public class SendNotificationDTO
    {
        public string Title { get; set; } = null!;
        public string Content { get; set; } = null!;

        // Mức độ: "Tin thường", "Khẩn cấp"
        public string Priority { get; set; } = "Tin thường";

        // Phạm vi: "All" hoặc tên Phường cụ thể
        public string Scope { get; set; } = "All";
    }
}