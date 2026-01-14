namespace SafeDalat_API.Model.DTO.QA
{
    public class AnswerResponseDTO
    {
        public int AnswerId { get; set; }
        public string Content { get; set; } = null!;
        public DateTime CreatedAt { get; set; }

        public int AdminId { get; set; }
        public string AdminName { get; set; } = null!;
    }
}
