using System.ComponentModel.DataAnnotations;

namespace SafeDalat_API.Model.Domain
{
    public class IncidentImage
    {
        [Key]
        public int ImageId { get; set; }

        public string FileName { get; set; } = null!;

        public string FilePath { get; set; } = null!;

        public string ContentType { get; set; } = null!;

        public long FileSize { get; set; }

        public DateTime UploadedAt { get; set; } = DateTime.Now;

        public int IncidentId { get; set; }
        public Incident Incident { get; set; }
    }

}
