using System.ComponentModel.DataAnnotations;

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
        public bool IsLocked { get; set; } = false;

        public DateTime CreatedAt { get; set; } = DateTime.Now;

        // Navigation
        public ICollection<Incident> Incidents { get; set; }
        public ICollection<IncidentStatusHistory> IncidentStatusHistories { get; set; }
    }
}
