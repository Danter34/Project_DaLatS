namespace SafeDalat_API.Model.DTO.Incident
{
    public class MergeIncidentDTO
    {
        public int MasterIncidentId { get; set; }
        public List<int> DuplicateIncidentIds { get; set; } = new();
    }
}
