using System.ComponentModel.DataAnnotations;

namespace SafeDalat_API.Model.Domain
{
    public class IncidentStatusHistory
    {
        [Key]
        public int HistoryId { get; set; }

        public string Status { get; set; } = null!;
        public string? Note { get; set; }

        public DateTime UpdatedAt { get; set; } = DateTime.Now;

        public int IncidentId { get; set; }
        public Incident Incident { get; set; }

        public int AdminId { get; set; }
        public User Admin { get; set; }
    }
}
