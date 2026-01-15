namespace SafeDalat_API.Model.DTO.Auth
{
    public class UserDashboardDTO
    {
        public int TotalIncidents { get; set; }
        public int PendingIncidents { get; set; }
        public int ProcessingIncidents { get; set; }
        public int CompletedIncidents { get; set; }
        public int RejectedIncidents { get; set; }
        public int UnreadNotifications { get; set; }
    }
}
