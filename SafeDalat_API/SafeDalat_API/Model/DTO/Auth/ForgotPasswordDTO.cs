namespace SafeDalat_API.Model.DTO.Auth
{
    public class ForgotPasswordDTO
    {
        public string Email { get; set; } = null!;
    }
    public class ResetPasswordDTO
    {
        public string Email { get; set; } = null!;
        public string Code { get; set; } = null!;
        public string NewPassword { get; set; } = null!;
    }

    public class CheckResetCodeDTO
    {
        public string Email { get; set; } = null!;
        public string Code { get; set; } = null!;
    }
}
