using System.ComponentModel.DataAnnotations;

namespace SafeDalat_API.Model.Domain
{
    public class IncidentDuplicate
    {
        [Key]
        public int Id { get; set; }

        public int MasterIncidentId { get; set; }
        public Incident MasterIncident { get; set; }

        public int DuplicateIncidentId { get; set; }
        public Incident DuplicateIncident { get; set; }

        public DateTime MergedAt { get; set; } = DateTime.Now;
    }
}
