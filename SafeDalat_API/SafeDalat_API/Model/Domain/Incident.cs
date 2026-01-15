using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SafeDalat_API.Model.Domain
{
    public class Incident
    {
        [Key]
        public int IncidentId { get; set; }

        public string Title { get; set; } = null!;
        public string Description { get; set; } = null!;

        //  Địa chỉ chi tiết
        public string Address { get; set; } = null!;
        public string Ward { get; set; } = null!;
        public string StreetName { get; set; } = null!;

        // Tọa độ
        public double Latitude { get; set; }
        public double Longitude { get; set; }

        //  Trạng thái xử lý: "Pending", "Assigned", "InProgress", "Resolved", "Closed"
        public string Status { get; set; }

        // Mức cảnh báo
        public AlertLevel AlertLevel { get; set; } = AlertLevel.Green;

        // Gộp sự cố
        public bool IsMaster { get; set; } = true;

        public DateTime CreatedAt { get; set; } = DateTime.Now;

        // FK Người gửi (Dân)
        public int UserId { get; set; }
        public User User { get; set; }

        public int CategoryId { get; set; }
        public IncidentCategory Category { get; set; }
        public int? AssignedDepartmentId { get; set; }
        [ForeignKey("AssignedDepartmentId")]
        public Department? AssignedDepartment { get; set; }

        public ICollection<IncidentImage> Images { get; set; }
        public ICollection<IncidentDuplicate> AsMasterDuplicates { get; set; }
        public ICollection<IncidentDuplicate> AsDuplicate { get; set; }
        public bool IsPublic { get; set; }
    }
    public enum AlertLevel
    {
        Green = 1,   // Bình thường
        Orange = 2,  // Cảnh báo
        Red = 3      // Nguy hiểm
    }
}
