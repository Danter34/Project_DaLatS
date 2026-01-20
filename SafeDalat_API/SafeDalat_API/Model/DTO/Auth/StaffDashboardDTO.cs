namespace SafeDalat_API.Model.DTO.Auth
{
    public class StaffDashboardDTO
    {
        public int AssignedTasks { get; set; }      // Tổng nhiệm vụ được giao
        public int CompletedTasks { get; set; }     // Đã hoàn thành
        public int InProgressTasks { get; set; }    // Đang xử lý
        public int OverdueTasks { get; set; }       // Quá hạn (nếu có logic hạn deadline)
        public double CompletionRate { get; set; }  // Tỷ lệ hoàn thành (%)

        // Thống kê theo mức độ ưu tiên
        public int HighPriorityTasks { get; set; }  // Mức đỏ

        // Gần đây nhất
        public DateTime? LastActive { get; set; }

        public int? DepartmentId { get; set; }
    }
}
