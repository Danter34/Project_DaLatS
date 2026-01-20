namespace SafeDalat_API.Model.DTO.Icident
{
    public class CreateIncidentDTO
    {
        public string Title { get; set; } = null!;
        public string Description { get; set; } = null!;
        public string Address { get; set; } = null!;
        public string Ward { get; set; } = null!;
        public string StreetName { get; set; } = null!;
        public double Latitude { get; set; }
        public double Longitude { get; set; }

        public int CategoryId { get; set; }

        public bool IsForceCreate { get; set; } = false;

        public double DeviceLatitude { get; set; }
        public double DeviceLongitude { get; set; }
    }
}
