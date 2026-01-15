namespace SafeDalat_API.Model.DTO.QA
{
    public class AnswerResponseDTO
    {
        public int AnswerId { get; set; }
        public string Content { get; set; } = null!;
        public DateTime CreatedAt { get; set; }

        public int ResponderId { get; set; }
        public string ResponderName { get; set; } = null!;


        public string? DepartmentName { get; set; }
    }
}