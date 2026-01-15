using SafeDalat_API.Model.DTO.QA;

namespace SafeDalat_API.Repositories.Interface
{
    public interface IQARepository
    {
        Task<QuestionResponseDTO> CreateQuestionAsync(int userId, CreateQuestionDTO dto);
        Task<List<QuestionResponseDTO>> GetAllQuestionsAsync();

        Task<bool> CreateAnswerAsync(int questionId, int responderId, CreateAnswerDTO dto);
        Task<List<QuestionResponseDTO>> GetQuestionsByDepartmentAsync(int departmentId);
    }
}
