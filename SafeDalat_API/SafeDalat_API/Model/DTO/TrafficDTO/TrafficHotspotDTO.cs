namespace SafeDalat_API.Model.DTO.TrafficDTO
{
    public class TrafficHotspotDTO
    {
        public string StreetName { get; set; } = string.Empty;
        public double Latitude { get; set; }
        public double Longitude { get; set; }
        public int ReportCount { get; set; } // Số lượng người báo cáo
        public string AlertMessage { get; set; } = string.Empty;
    }
}
