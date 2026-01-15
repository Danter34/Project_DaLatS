using SafeDalat_API.Repositories.Interface;
using System.Net;
using System.Net.Mail;

namespace SafeDalat_API.Repositories.Services
{
    public class EmailService : IEmailService
    {
        private readonly IConfiguration _config;

        public EmailService(IConfiguration config)
        {
            _config = config;
        }

        public async Task SendVerifyEmail(string email, string token)
        {
            var link = $"{_config["ClientUrl"]}/verify-email?token={token}";
            var body = $"Click để xác minh tài khoản:<br/><a href='{link}'>Xác minh</a>";

            await Send(email, "Xác minh tài khoản SafeDalat", body);
        }

        public async Task SendResetCode(string email, string code)
        {
            var body = $"Mã đặt lại mật khẩu của bạn là:<br/><b>{code}</b>";
            await Send(email, "Quên mật khẩu - SafeDalat", body);
        }

        private async Task Send(string to, string subject, string body)
        {
            if (string.IsNullOrWhiteSpace(to))
                throw new Exception("Email người nhận không hợp lệ");

            var host = _config["Email:Host"];
            var port = int.Parse(_config["Email:Port"]!);
            var fromEmail = _config["Email:Username"];
            var password = _config["Email:Password"];

            if (string.IsNullOrWhiteSpace(host)
                || string.IsNullOrWhiteSpace(fromEmail)
                || string.IsNullOrWhiteSpace(password))
            {
                throw new Exception("Cấu hình Email chưa đúng");
            }

            var smtp = new SmtpClient(host, port)
            {
                Credentials = new NetworkCredential(fromEmail, password),
                EnableSsl = true
            };

            var mail = new MailMessage
            {
                From = new MailAddress(fromEmail, "SafeDalat"),
                Subject = subject,
                Body = body,
                IsBodyHtml = true
            };

            mail.To.Add(to);

            await smtp.SendMailAsync(mail);
        }
    }
}
