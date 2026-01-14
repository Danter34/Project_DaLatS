using Microsoft.EntityFrameworkCore;
using SafeDalat_API.Data;
using SafeDalat_API.Model.Domain;
using SafeDalat_API.Model.DTO.QA;
using SafeDalat_API.Repositories.Interface;

namespace SafeDalat_API.Repositories.Services
{
    public class QARepository : IQARepository
    {
        private readonly AppDbContext _context;

        public QARepository(AppDbContext context)
        {
            _context = context;
        }

        public async Task<QuestionResponseDTO> CreateQuestionAsync(
            int userId,
            CreateQuestionDTO dto)
        {
            var question = new Question
            {
                Content = dto.Content,
                UserId = userId,
                CreatedAt = DateTime.UtcNow
            };

            _context.Questions.Add(question);
            await _context.SaveChangesAsync();

            var user = await _context.Users.FindAsync(userId);

            return new QuestionResponseDTO
            {
                QuestionId = question.QuestionId,
                Content = question.Content,
                CreatedAt = question.CreatedAt,
                UserId = userId,
                UserName = user!.FullName
            };
        }

        public async Task<List<QuestionResponseDTO>> GetAllQuestionsAsync()
        {
            return await _context.Questions
                .AsNoTracking()
                .Include(q => q.User)
                .Include(q => q.Answers)
                    .ThenInclude(a => a.Admin)
                .OrderByDescending(q => q.CreatedAt)
                .Select(q => new QuestionResponseDTO
                {
                    QuestionId = q.QuestionId,
                    Content = q.Content,
                    CreatedAt = q.CreatedAt,
                    UserId = q.UserId,
                    UserName = q.User.FullName,
                    Answers = q.Answers.Select(a => new AnswerResponseDTO
                    {
                        AnswerId = a.AnswerId,
                        Content = a.Content,
                        CreatedAt = a.CreatedAt,
                        AdminId = a.AdminId,
                        AdminName = a.Admin.FullName
                    }).ToList()
                })
                .ToListAsync();
        }

        public async Task<bool> CreateAnswerAsync(
            int questionId,
            int adminId,
            CreateAnswerDTO dto)
        {
            var question = await _context.Questions.FindAsync(questionId);
            if (question == null) return false;

            var answer = new Answer
            {
                Content = dto.Content,
                QuestionId = questionId,
                AdminId = adminId,
                CreatedAt = DateTime.UtcNow
            };

            _context.Answers.Add(answer);
            await _context.SaveChangesAsync();
            return true;
        }
    }
}
