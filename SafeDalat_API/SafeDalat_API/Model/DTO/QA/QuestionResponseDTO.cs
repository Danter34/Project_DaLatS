namespace SafeDalat_API.Model.DTO.QA
{
    public class QuestionResponseDTO
    {
        public int QuestionId { get; set; }
        public string Content { get; set; } = null!;
        public DateTime CreatedAt { get; set; }

        public int UserId { get; set; }
        public string UserName { get; set; } = null!;

        public string QuestionCategoryName { get; set; } = null!;
        public string? AssignedDepartmentName { get; set; } // Nếu đã được điều phối

        public List<AnswerResponseDTO> Answers { get; set; } = new();
    }
}