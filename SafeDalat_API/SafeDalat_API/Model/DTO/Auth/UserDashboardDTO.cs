namespace SafeDalat_API.Model.DTO.Auth
{
    public class UserDashboardDTO
    {
        public int TotalIncidents { get; set; }
        public int PendingIncidents { get; set; }
        public int ProcessingIncidents { get; set; }
        public int CompletedIncidents { get; set; }
        public int RejectedIncidents { get; set; }
        public int UnreadNotifications { get; set; }

        public int TrustScore { get; set; }          // Điểm hiện tại
        public int DailyReportLimit { get; set; }    // Giới hạn tối đa trong ngày
        public int UsedDailyQuota { get; set; }      // Đã dùng bao nhiêu lượt hôm nay
        public string TrustStatus { get; set; }
    }
}
