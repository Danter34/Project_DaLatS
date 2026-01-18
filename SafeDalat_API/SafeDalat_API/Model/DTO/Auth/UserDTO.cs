namespace SafeDalat_API.Model.DTO.Auth
{
    public class UserProfileDTO
    {
        public string FullName { get; set; } = null!;
        public string Email { get; set; } = null!;
        public DateTime CreatedAt { get; set; }

        public string? DepartmentName { get; set; }
    }

    public class UpdateProfileDTO
    {
        public string FullName { get; set; } = null!;
        public string Email { get; set; } = null!; // Cho phép sửa Email
    }
}
