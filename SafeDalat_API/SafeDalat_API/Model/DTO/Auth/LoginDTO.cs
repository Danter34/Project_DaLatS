namespace SafeDalat_API.Model.DTO.Auth
{
    using System.ComponentModel.DataAnnotations;

    public class LoginDTO
    {
        [Required]
        [EmailAddress]
        public string Email { get; set; } = null!;

        [Required]
        public string Password { get; set; } = null!;
    }

}
