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

        
        public async Task<QuestionResponseDTO> CreateQuestionAsync(int userId, CreateQuestionDTO dto)
        {
           
            var category = await _context.QuestionCategories
                .Include(c => c.ResponsibleDepartment)
                .FirstOrDefaultAsync(c => c.CategoryId == dto.QuestionCategoryId);

            
            var user = await _context.Users.FindAsync(userId);
            if (user == null) throw new Exception("User not found");

           
            var question = new Question
            {
                Content = dto.Content,
                UserId = userId,
                QuestionCategoryId = dto.QuestionCategoryId,
                
                AssignedDepartmentId = category?.ResponsibleDepartmentId,
                CreatedAt = DateTime.UtcNow
            };

            _context.Questions.Add(question);

           
            await _context.SaveChangesAsync();

           
            return new QuestionResponseDTO
            {
                QuestionId = question.QuestionId,
                Content = question.Content,
                CreatedAt = question.CreatedAt,
                UserId = userId,
                UserName = user.FullName, 
                QuestionCategoryName = category?.Name ?? "Chung",
                AssignedDepartmentName = category?.ResponsibleDepartment?.Name 
            };
        }

       
        public async Task<List<QuestionResponseDTO>> GetAllQuestionsAsync()
        {
            return await _context.Questions
                .AsNoTracking()
                .Include(q => q.User)
                .Include(q => q.QuestionCategory) 
                .Include(q => q.AssignedDepartment) 
                .Include(q => q.Answers)
                    .ThenInclude(a => a.Responder) 
                        .ThenInclude(u => u.Department) 
                .OrderByDescending(q => q.CreatedAt)
                .Select(q => new QuestionResponseDTO
                {
                    QuestionId = q.QuestionId,
                    Content = q.Content,
                    CreatedAt = q.CreatedAt,
                    UserId = q.UserId,
                    UserName = q.User.FullName,
                    QuestionCategoryName = q.QuestionCategory.Name,
                    AssignedDepartmentName = q.AssignedDepartment != null ? q.AssignedDepartment.Name : null,

                    Answers = q.Answers.Select(a => new AnswerResponseDTO
                    {
                        AnswerId = a.AnswerId,
                        Content = a.Content,
                        CreatedAt = a.CreatedAt,
                        ResponderId = a.ResponderId, 
                        ResponderName = a.Responder.FullName,
                        DepartmentName = a.Responder.Department != null ? a.Responder.Department.Name : "Quản trị viên"
                    }).ToList()
                })
                .ToListAsync();
        }
        public async Task<List<QuestionResponseDTO>> GetQuestionsByDepartmentAsync(int departmentId)
        {
            return await _context.Questions
                .AsNoTracking()
                .Where(q => q.AssignedDepartmentId == departmentId) 
                .Include(q => q.User)
                .Include(q => q.QuestionCategory)
                .Include(q => q.AssignedDepartment)
                .Include(q => q.Answers)
                    .ThenInclude(a => a.Responder)
                .OrderByDescending(q => q.CreatedAt)
                .Select(q => new QuestionResponseDTO
                {
                    QuestionId = q.QuestionId,
                    Content = q.Content,
                    CreatedAt = q.CreatedAt,
                    UserId = q.UserId,
                    UserName = q.User.FullName,
                    QuestionCategoryName = q.QuestionCategory.Name,
                    AssignedDepartmentName = q.AssignedDepartment != null ? q.AssignedDepartment.Name : null,
                    Answers = q.Answers.Select(a => new AnswerResponseDTO
                    {
                        AnswerId = a.AnswerId,
                        Content = a.Content,
                        CreatedAt = a.CreatedAt,
                        ResponderId = a.ResponderId,
                        ResponderName = a.Responder.FullName,
                        DepartmentName = a.Responder.Department != null ? a.Responder.Department.Name : "Quản trị viên"
                    }).ToList()
                })
                .ToListAsync();
        }
        // TRẢ LỜI (Cho phép Staff/Admin)
        public async Task<bool> CreateAnswerAsync(int questionId, int responderId, CreateAnswerDTO dto)
        {
            var question = await _context.Questions.FindAsync(questionId);
            if (question == null) return false;

            var answer = new Answer
            {
                Content = dto.Content,
                QuestionId = questionId,
                ResponderId = responderId, 
                CreatedAt = DateTime.UtcNow
            };

            _context.Answers.Add(answer);
            await _context.SaveChangesAsync();
            return true;
        }
        public async Task<List<QuestionCategory>> GetAllCategoriesAsync()
        {
            return await _context.QuestionCategories
                .AsNoTracking() // Tối ưu hiệu năng, không cần theo dõi thay đổi
                .ToListAsync();
        }
        public async Task<List<QuestionResponseDTO>> GetQuestionsByUserIdAsync(int userId)
        {
            return await _context.Questions
                .AsNoTracking()
                .Where(q => q.UserId == userId) // Lọc theo UserId
                .Include(q => q.User)
                .Include(q => q.QuestionCategory)
                .Include(q => q.AssignedDepartment)
                .Include(q => q.Answers)
                    .ThenInclude(a => a.Responder)
                        .ThenInclude(u => u.Department)
                .OrderByDescending(q => q.CreatedAt)
                .Select(q => new QuestionResponseDTO
                {
                    QuestionId = q.QuestionId,
                    Content = q.Content,
                    CreatedAt = q.CreatedAt,
                    UserId = q.UserId,
                    UserName = q.User.FullName,
                    QuestionCategoryName = q.QuestionCategory.Name,
                    AssignedDepartmentName = q.AssignedDepartment != null ? q.AssignedDepartment.Name : null,
                    Answers = q.Answers.Select(a => new AnswerResponseDTO
                    {
                        AnswerId = a.AnswerId,
                        Content = a.Content,
                        CreatedAt = a.CreatedAt,
                        ResponderId = a.ResponderId,
                        ResponderName = a.Responder.FullName,
                        DepartmentName = a.Responder.Department != null ? a.Responder.Department.Name : "Quản trị viên"
                    }).ToList()
                })
                .ToListAsync();
        }
    }
}