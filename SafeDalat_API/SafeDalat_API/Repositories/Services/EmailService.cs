using SafeDalat_API.Repositories.Interface;
using System.Net;
using System.Net.Mail;
using System.Text;

namespace SafeDalat_API.Repositories.Services
{
    public class EmailService : IEmailService
    {
        private readonly IConfiguration _config;

      
        private string LogoUrl => _config["Email:LogoUrl"] ?? "http://localhost:5084/assets/email/logo.png";

        private const string BrandColor = "#2E7D32";
        private const string BrandName = "Dalats";

        public EmailService(IConfiguration config)
        {
            _config = config;
        }

        public async Task SendVerifyEmail(string email, string token)
        {
            var link = $"{_config["ClientUrl"]}/verify-email?token={token}";

            var content = $@"
                <p>Xin chào,</p>
                <p>Cảm ơn bạn đã đăng ký tài khoản tại <b>{BrandName}</b>. Để bắt đầu sử dụng dịch vụ, vui lòng xác minh địa chỉ email của bạn bằng cách nhấn vào nút bên dưới:</p>
                <br/>
            ";

            var body = GetHtmlTemplate("Xác minh tài khoản", content, "Xác minh ngay", link);
            await Send(email, $"[SafeDalat] Xác minh tài khoản của bạn", body);
        }
        public async Task SendResetCode(string email, string code)
        {
            var content = $@"
                <p>Xin chào,</p>
                <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản liên kết với email này.</p>
                <p>Đây là mã xác minh của bạn (có hiệu lực trong 10 phút):</p>
                
                <div style='background-color: #f3f4f6; padding: 15px; text-align: center; border-radius: 8px; margin: 20px 0;'>
                    <span style='font-size: 32px; font-weight: bold; letter-spacing: 5px; color: {BrandColor};'>{code}</span>
                </div>

                <p>Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.</p>
            ";

            var body = GetHtmlTemplate("Đặt lại mật khẩu", content);
            await Send(email, $"[SafeDalat] Mã xác minh: {code}", body);
        }

        public async Task SendAccountLockedNotification(string email, string reason)
        {
            var content = $@"
                <p>Xin chào,</p>
                <p>Tài khoản của bạn tại <b>{BrandName}</b> vừa bị tạm khóa bởi quản trị viên.</p>
                
                <div style='border-left: 4px solid #d32f2f; background-color: #ffebee; padding: 15px; margin: 20px 0;'>
                    <strong style='color: #d32f2f;'>Lý do khóa:</strong>
                    <p style='margin: 5px 0 0 0;'>{reason}</p>
                </div>

                <p>Nếu bạn cho rằng đây là sự nhầm lẫn, vui lòng phản hồi lại email này để được hỗ trợ.</p>
            ";

            var body = GetHtmlTemplate("Tài khoản bị khóa", content);
            await Send(email, "[SafeDalat] Thông báo quan trọng về tài khoản", body);
        }

        public async Task SendAccountUnlockedNotification(string email, string reason)
        {
            var content = $@"
                <p>Xin chào,</p>
                <p>Tin vui! Tài khoản của bạn tại <b>{BrandName}</b> đã được kích hoạt trở lại.</p>
                
                <div style='border-left: 4px solid {BrandColor}; background-color: #e8f5e9; padding: 15px; margin: 20px 0;'>
                    <strong style='color: {BrandColor};'>Ghi chú từ Admin:</strong>
                    <p style='margin: 5px 0 0 0;'>{reason}</p>
                </div>

                <p>Bạn có thể đăng nhập và sử dụng dịch vụ bình thường ngay bây giờ.</p>
            ";

            var body = GetHtmlTemplate("Tài khoản đã được mở khóa", content, "Đăng nhập ngay", "https://safedalat.com/login"); 
            await Send(email, "[SafeDalat] Tài khoản của bạn đã được mở", body);
        }

        private string GetHtmlTemplate(string title, string content, string? buttonText = null, string? buttonUrl = null)
        {
            var buttonHtml = string.Empty;
            if (!string.IsNullOrEmpty(buttonText) && !string.IsNullOrEmpty(buttonUrl))
            {
                buttonHtml = $@"
                    <table role='presentation' border='0' cellpadding='0' cellspacing='0' style='margin: 30px auto;'>
                        <tr>
                            <td align='center' bgcolor='{BrandColor}' style='border-radius: 5px;'>
                                <a href='{buttonUrl}' style='font-size: 16px; font-family: Helvetica, Arial, sans-serif; color: #ffffff; text-decoration: none; padding: 12px 25px; border-radius: 5px; border: 1px solid {BrandColor}; display: inline-block; font-weight: bold;'>
                                    {buttonText}
                                </a>
                            </td>
                        </tr>
                    </table>";
            }

            return $@"
            <!DOCTYPE html>
            <html>
            <head>
                <meta name='viewport' content='width=device-width, initial-scale=1.0' />
                <meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />
                <style>
                    body {{ font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; -webkit-font-smoothing: antialiased; font-size: 14px; line-height: 1.4; margin: 0; padding: 0; -ms-text-size-adjust: 100%; -webkit-text-size-adjust: 100%; }}
                    .container {{ padding: 20px; }}
                    .content {{ max-width: 600px; margin: 0 auto; display: block; background: #ffffff; border-radius: 8px; border: 1px solid #e0e0e0; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }}
                    .header {{ text-align: center; padding: 30px 20px 20px 20px; border-bottom: 1px solid #f0f0f0; }}
                    .main {{ padding: 30px; }}
                    .footer {{ text-align: center; padding-top: 20px; color: #999999; font-size: 12px; }}
                    @media only screen and (max-width: 620px) {{
                        .body .content {{ width: 100% !important; }}
                    }}
                </style>
            </head>
            <body style='background-color: #f6f6f6; margin: 0; padding: 0;'>
                <table role='presentation' border='0' cellpadding='0' cellspacing='0' class='body' width='100%' style='background-color: #f6f6f6; width: 100%;'>
                    <tr>
                        <td class='container'>
                            <div class='content'>
                                <div class='header'>
                                    <img src='{LogoUrl}' alt='{BrandName}' width='120' style='border: none; -ms-interpolation-mode: bicubic; max-width: 100%;'>
                                </div>

                                <div class='main'>
                                    <h2 style='color: #333333; margin: 0 0 20px 0; font-size: 20px; text-align: center;'>{title}</h2>
                                    <div style='color: #555555; font-size: 16px; line-height: 1.6;'>
                                        {content}
                                    </div>
                                    {buttonHtml}
                                    <p style='margin-top: 30px; font-size: 14px; color: #888888;'>Trân trọng,<br>Đội ngũ {BrandName}</p>
                                </div>
                            </div>

                            <div class='footer'>
                                <p>Email này được gửi tự động từ hệ thống Dalats.</p>
                                <p>Đà Lạt, Lâm Đồng, Việt Nam</p>
                            </div>
                        </td>
                    </tr>
                </table>
            </body>
            </html>";
        }


        private async Task Send(string to, string subject, string body)
        {
            if (string.IsNullOrWhiteSpace(to)) throw new Exception("Email người nhận không hợp lệ");

            var host = _config["Email:Host"];
            var port = int.Parse(_config["Email:Port"]!);
            var fromEmail = _config["Email:Username"];
            var password = _config["Email:Password"];

            if (string.IsNullOrWhiteSpace(host) || string.IsNullOrWhiteSpace(fromEmail) || string.IsNullOrWhiteSpace(password))
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
                From = new MailAddress(fromEmail, BrandName), 
                Subject = subject,
                Body = body,
                IsBodyHtml = true
            };

            mail.To.Add(to);
            await smtp.SendMailAsync(mail);
        }
    }
}