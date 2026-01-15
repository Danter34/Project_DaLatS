using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema; // Thêm thư viện này

namespace SafeDalat_API.Model.Domain
{
    public class User
    {
        [Key]
        public int UserId { get; set; }

        public string FullName { get; set; } = null!;
        public string Email { get; set; } = null!;
        public string Password { get; set; } = null!;

        public string Role { get; set; } = "User";

        public bool IsLocked { get; set; } = true;
        public bool EmailVerified { get; set; } = false;
        public string? VerifyToken { get; set; }
        public DateTime? VerifyTokenExpire { get; set; }

        public string? ResetPasswordCode { get; set; }
        public DateTime? ResetCodeExpire { get; set; }
        public DateTime CreatedAt { get; set; } = DateTime.Now;

        public int? DepartmentId { get; set; }
        [ForeignKey("DepartmentId")]
        public Department? Department { get; set; }

        public ICollection<Incident> Incidents { get; set; }
        public ICollection<IncidentStatusHistory> IncidentStatusHistories { get; set; }
    }
}