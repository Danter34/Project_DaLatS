using SafeDalat_API.Repositories.Interface;
using SafeDalat_API.Model.DTO.Notification;
using SafeDalat_API.Model.DTO.TrafficDTO;

namespace SafeDalat_API.Workers
{
    public class TrafficAlertWorker : BackgroundService
    {
        private readonly IServiceProvider _serviceProvider;
        private readonly ILogger<TrafficAlertWorker> _logger;

        // [FIX] Dùng Dictionary để lưu thời gian báo lần cuối
        // Key: Tên đường, Value: Thời gian báo gần nhất
        private static Dictionary<string, DateTime> _lastAlertTime = new Dictionary<string, DateTime>();

        // Cấu hình: Bao lâu thì được báo lại 1 lần (Ví dụ: 30 phút)
        private readonly TimeSpan _alertCooldown = TimeSpan.FromMinutes(30);

        public TrafficAlertWorker(IServiceProvider serviceProvider, ILogger<TrafficAlertWorker> logger)
        {
            _serviceProvider = serviceProvider;
            _logger = logger;
        }

        protected override async Task ExecuteAsync(CancellationToken stoppingToken)
        {
            while (!stoppingToken.IsCancellationRequested)
            {
                _logger.LogInformation("Traffic Worker đang quét điểm nóng...");

                try
                {
                    using (var scope = _serviceProvider.CreateScope())
                    {
                        var trafficRepo = scope.ServiceProvider.GetRequiredService<ITrafficRepository>();
                        var notiRepo = scope.ServiceProvider.GetRequiredService<INotificationRepository>();

                        // 1. Lấy danh sách điểm nóng
                        var hotspots = await trafficRepo.GetHotspotsAsync();

                        // 2. Lọc điểm nóng nghiêm trọng (>= 3 reports)
                        var seriousHotspots = hotspots.Where(h => h.ReportCount >= 3).ToList();

                        foreach (var spot in seriousHotspots)
                        {
                            
                            bool shouldAlert = false;

                            if (!_lastAlertTime.ContainsKey(spot.StreetName))
                            {
                                shouldAlert = true; // Chưa báo bao giờ -> Báo luôn
                            }
                            else
                            {
                                var lastTime = _lastAlertTime[spot.StreetName];
                                // Nếu đã báo quá 30 phút trước -> Báo lại nhắc nhở
                                if (DateTime.UtcNow - lastTime > _alertCooldown)
                                {
                                    shouldAlert = true;
                                }
                            }

                            if (shouldAlert)
                            {
                                // 3. Tạo & Gửi thông báo
                                var notiDto = new SendNotificationDTO
                                {
                                    Title = $"⚠️ ÙN TẮC TẠI {spot.StreetName.ToUpper()}",
                                    Content = $"Hệ thống phát hiện {spot.ReportCount} sự cố tại khu vực này. Vui lòng hạn chế di chuyển qua đây.",
                                    Priority = "Khẩn cấp",
                                    Scope = "All"
                                };

                                await notiRepo.BroadcastAsync(notiDto);

                                // 4. Cập nhật thời gian báo lần cuối
                                if (_lastAlertTime.ContainsKey(spot.StreetName))
                                    _lastAlertTime[spot.StreetName] = DateTime.UtcNow;
                                else
                                    _lastAlertTime.Add(spot.StreetName, DateTime.UtcNow);

                                _logger.LogInformation($"Đã gửi cảnh báo: {spot.StreetName}");
                            }
                        }

                   
                        var currentStreetNames = seriousHotspots.Select(s => s.StreetName).ToHashSet();

                        // Tìm các đường có trong Cache nhưng không còn tắc nữa -> Xóa đi
                        var keysToRemove = _lastAlertTime.Keys.Where(k => !currentStreetNames.Contains(k)).ToList();
                        foreach (var key in keysToRemove)
                        {
                            _lastAlertTime.Remove(key);
                        }
                    }
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Lỗi trong Traffic Worker");
                }

                // Chờ 5 phút (300000ms) hoặc 10 giây (10000ms) nếu muốn test nhanh
                await Task.Delay(300000, stoppingToken);
            }
        }
    }
}