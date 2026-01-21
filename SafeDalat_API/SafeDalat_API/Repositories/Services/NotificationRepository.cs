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
        private readonly IFcmService _fcmService;

        public NotificationRepository(AppDbContext context, IFcmService fcmService)
        {
            _context = context;
            _fcmService = fcmService;
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
            // 1. Lưu vào DB
            _context.Notifications.Add(new Model.Domain.Notification
            {
                UserId = userId,
                Message = message,
                Type = "System",
                IsRead = false,
                CreatedAt = DateTime.UtcNow
            });
            await _context.SaveChangesAsync();

            // 2. Gửi FCM cá nhân
            var userToken = await _context.Users
                .Where(u => u.UserId == userId)
                .Select(u => u.FcmToken)
                .FirstOrDefaultAsync();

            // [FIX] Làm sạch token trước khi gửi
            if (!string.IsNullOrEmpty(userToken))
            {
                string cleanToken = userToken.Replace("\"", "").Trim();
                if (!string.IsNullOrEmpty(cleanToken))
                {
                    await _fcmService.SendToTokenAsync(cleanToken, "Thông báo mới", message);
                }
            }
        }

     
        public async Task BroadcastAsync(SendNotificationDTO dto)
        {
            // 1. Lấy toàn bộ User (Chỉ lấy ID và Token cho nhẹ)
            var users = await _context.Users
                .AsNoTracking()
                .Select(x => new { x.UserId, x.FcmToken })
                .ToListAsync();

            if (users.Count == 0) return;

            string prefix = dto.Priority == "Khẩn cấp" ? "[KHẨN CẤP] " : "";
            string fullMessage = $"{prefix}{dto.Title}: {dto.Content}";
            string type = dto.Priority == "Khẩn cấp" ? "Urgent" : "Info";
            var now = DateTime.UtcNow;

          
            var notifications = new List<Model.Domain.Notification>();
            foreach (var u in users)
            {
                notifications.Add(new Model.Domain.Notification
                {
                    UserId = u.UserId,
                    Message = fullMessage,
                    Type = type,
                    IsRead = false,
                    CreatedAt = now
                });
            }

            // Bulk Insert vào DB
            await _context.Notifications.AddRangeAsync(notifications);
            await _context.SaveChangesAsync();

        
            var validTokens = users
    .Where(u => !string.IsNullOrWhiteSpace(u.FcmToken))
    .Select(u => u.FcmToken.Trim().Replace("\"", ""))
    .Where(t => t.Length > 20) // token FCM luôn rất dài
    .Distinct()
    .ToList();


            // Nếu không có ai đủ điều kiện nhận FCM thì dừng, không gọi Google
            if (validTokens.Count == 0) return;

          
            int batchSize = 500;
            int total = validTokens.Count;

            for (int i = 0; i < total; i += batchSize)
            {
                // Cắt lấy 500 token
                var currentBatch = validTokens.Skip(i).Take(batchSize).ToList();

                try
                {
                    // Gửi cho nhóm này
                    await _fcmService.SendToMultipleTokensAsync(currentBatch, dto.Title, dto.Content);
                }
                catch (Exception ex)
                {
                    // Nếu nhóm này lỗi, ghi log nhưng VẪN TIẾP TỤC vòng lặp gửi cho nhóm sau
                    Console.WriteLine($"[Lỗi Broadcast] Batch {i}: {ex.Message}");
                }
            }
        }
    }
}