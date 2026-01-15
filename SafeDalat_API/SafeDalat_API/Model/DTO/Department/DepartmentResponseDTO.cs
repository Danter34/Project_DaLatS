namespace SafeDalat_API.Model.DTO.Department
{
    public class DepartmentResponseDTO
    {
        public int DepartmentId { get; set; }
        public string Name { get; set; } = null!;
        public string? Description { get; set; }
        public string? PhoneNumber { get; set; }

        // (Tuỳ chọn) Đếm số lượng nhân viên đang có
        public int StaffCount { get; set; }
    }
}