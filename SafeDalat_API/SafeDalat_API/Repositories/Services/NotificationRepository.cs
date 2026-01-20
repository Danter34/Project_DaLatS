using Microsoft.EntityFrameworkCore;
using SafeDalat_API.Data;
using SafeDalat_API.Model.Domain;
using SafeDalat_API.Model.DTO.Notification;
using SafeDalat_API.Repositories.Interface;

namespace SafeDalat_API.Repositories.Services
{
    public class NotificationRepository : INotificationRepository
    {
        private readonly AppDbContext _context;

        public NotificationRepository(AppDbContext context)
        {
            _context = context;
        }

        public async Task<List<NotificationDTO>> GetMyAsync(int userId)
        {
            return await _context.Notifications
                .AsNoTracking()
                .Where(x => x.UserId == userId)
                .OrderByDescending(x => x.CreatedAt)
                .Select(x => new NotificationDTO
                {
                    NotificationId = x.NotificationId,
                    Message = x.Message,
                    IsRead = x.IsRead,
                    CreatedAt = x.CreatedAt
                })
                .ToListAsync();
        }

        public async Task MarkAsReadAsync(int notificationId, int userId)
        {
            var noti = await _context.Notifications
                .FirstOrDefaultAsync(x => x.NotificationId == notificationId && x.UserId == userId);

            if (noti == null) return;

            noti.IsRead = true;
            await _context.SaveChangesAsync();
        }

        public async Task CreateAsync(int userId, string message)
        {
            _context.Notifications.Add(new Notification
            {
                UserId = userId,
                Message = message
            });

            await _context.SaveChangesAsync();
        }
      

        // Thêm vào NotificationRepository
        public async Task BroadcastAsync(SendNotificationDTO dto)
        {
            // 1. Xác định danh sách người nhận
            IQueryable<User> query = _context.Users.AsNoTracking();

            // Nếu Scope không phải "All", lọc theo địa chỉ (Phường)
            // Lưu ý: User cần có trường Address/Ward thì mới lọc được. 
            // Nếu User chưa có Ward, tạm thời gửi All hoặc bỏ qua logic lọc này.
            // Giả sử gửi All cho đơn giản như yêu cầu "áp dụng cho mọi tài khoản"

            var users = await query.Select(x => x.UserId).ToListAsync();

            // 2. Tạo nội dung tin nhắn (Ghép Tiêu đề + Nội dung)
            string prefix = dto.Priority == "Khẩn cấp" ? "[KHẨN CẤP] " : "";
            string fullMessage = $"{prefix}{dto.Title}: {dto.Content}";
            string type = dto.Priority == "Khẩn cấp" ? "Urgent" : "Info";

            // 3. Tạo danh sách Notification để Bulk Insert
            var notifications = new List<Notification>();

            foreach (var uid in users)
            {
                notifications.Add(new Notification
                {
                    UserId = uid,
                    Message = fullMessage,
                    Type = type,
                    IsRead = false,
                    CreatedAt = DateTime.UtcNow
                });
            }

            // 4. Lưu vào DB (Dùng AddRange cho nhanh)
            await _context.Notifications.AddRangeAsync(notifications);
            await _context.SaveChangesAsync();
        }
    }
}
