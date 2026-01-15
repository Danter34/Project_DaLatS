using System.ComponentModel.DataAnnotations;

namespace SafeDalat_API.Model.DTO.QA
{
    public class CreateQuestionDTO
    {
        [Required]
        public string Content { get; set; } = null!;

        [Required]
        public int QuestionCategoryId { get; set; }
    }
}