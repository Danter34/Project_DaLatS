namespace SafeDalat_API.Model.DTO.Comment
{
    public class CommentResponseDTO
    {
        public int CommentId { get; set; }
        public string Content { get; set; } = null!;
        public DateTime CreatedAt { get; set; }

        public int UserId { get; set; }
        public string FullName { get; set; } = null!;
        public string Role { get; set; } = null!;
    }
}
