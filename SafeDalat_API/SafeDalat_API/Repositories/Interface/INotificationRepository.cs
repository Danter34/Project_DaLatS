using SafeDalat_API.Model.DTO.Notification;

namespace SafeDalat_API.Repositories.Interface
{
    public interface INotificationRepository
    {
        Task<List<NotificationDTO>> GetMyAsync(int userId);
        Task MarkAsReadAsync(int notificationId, int userId);
        Task CreateAsync(int userId, string message);
    }
}
