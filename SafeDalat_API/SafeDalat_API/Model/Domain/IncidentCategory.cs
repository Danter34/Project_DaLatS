using System.ComponentModel.DataAnnotations;

namespace SafeDalat_API.Model.Domain
{
    public class IncidentCategory
    {
        [Key]
        public int CategoryId { get; set; }
        public string Name { get; set; } = null!;
    }
}
