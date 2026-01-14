using SafeDalat_API.Model.DTO.Comment;

namespace SafeDalat_API.Repositories.Interface
{
    public interface IIncidentCommentRepository
    {
        Task<List<CommentResponseDTO>> GetByIncidentAsync(int incidentId);
        Task<CommentResponseDTO> CreateAsync(int incidentId, int userId, CreateCommentDTO dto);
    }
}
