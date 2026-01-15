namespace SafeDalat_API.Model.DTO.Auth
{
    using System.ComponentModel.DataAnnotations;

    namespace SafeDalat_API.Model.DTO.Auth
    {
        public class RegisterDTO
        {
            [Required]
            public string FullName { get; set; } = null!;

            [Required]
            [EmailAddress]
            public string Email { get; set; } = null!;

            [Required]
            [MinLength(6)]
            public string Password { get; set; } = null!;
        }
    }

}
