using System.ComponentModel.DataAnnotations;

namespace SafeDalat_API.Model.DTO.Auth
{
    public class CreateStaffDTO
    {
        [Required(ErrorMessage = "Họ tên là bắt buộc")]
        public string FullName { get; set; } = null!;

        [Required(ErrorMessage = "Email là bắt buộc")]
        [EmailAddress(ErrorMessage = "Email không hợp lệ")]
        public string Email { get; set; } = null!;

        [Required(ErrorMessage = "Mật khẩu là bắt buộc")]
        [MinLength(6, ErrorMessage = "Mật khẩu phải từ 6 ký tự")]
        public string Password { get; set; } = null!;

        [Required(ErrorMessage = "Vui lòng chọn vai trò")]
        public string Role { get; set; } = "Staff"; 

        public int? DepartmentId { get; set; }
    }
}