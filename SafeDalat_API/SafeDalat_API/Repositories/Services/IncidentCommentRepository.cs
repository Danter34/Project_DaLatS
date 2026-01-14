using Microsoft.EntityFrameworkCore;
using SafeDalat_API.Data;
using SafeDalat_API.Model.Domain;
using SafeDalat_API.Model.DTO.Comment;
using SafeDalat_API.Repositories.Interface;

namespace SafeDalat_API.Repositories.Services
{
    public class IncidentCommentRepository : IIncidentCommentRepository
    {
        private readonly AppDbContext _context;

        public IncidentCommentRepository(AppDbContext context)
        {
            _context = context;
        }

        public async Task<List<CommentResponseDTO>> GetByIncidentAsync(int incidentId)
        {
            return await _context.IncidentComments
                .AsNoTracking()
                .Include(x => x.User)
                .Where(x => x.IncidentId == incidentId)
                .OrderBy(x => x.CreatedAt)
                .Select(x => new CommentResponseDTO
                {
                    CommentId = x.CommentId,
                    Content = x.Content,
                    CreatedAt = x.CreatedAt,
                    UserId = x.UserId,
                    FullName = x.User.FullName,
                    Role = x.User.Role
                })
                .ToListAsync();
        }

        public async Task<CommentResponseDTO> CreateAsync(
            int incidentId,
            int userId,
            CreateCommentDTO dto)
        {
            var incident = await _context.Incidents.FindAsync(incidentId);
            if (incident == null || incident.Status == "Chờ xử lý")
                throw new Exception("Incident chưa được duyệt");

            var comment = new IncidentComment
            {
                IncidentId = incidentId,
                UserId = userId,
                Content = dto.Content
            };

            _context.IncidentComments.Add(comment);
            await _context.SaveChangesAsync();

            var user = await _context.Users.FindAsync(userId);

            return new CommentResponseDTO
            {
                CommentId = comment.CommentId,
                Content = comment.Content,
                CreatedAt = comment.CreatedAt,
                UserId = userId,
                FullName = user!.FullName,
                Role = user.Role
            };
        }
    }
}
