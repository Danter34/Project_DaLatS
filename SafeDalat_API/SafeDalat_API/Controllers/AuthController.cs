using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SafeDalat_API.Data;
using SafeDalat_API.Helpers;
using SafeDalat_API.Model.Domain;
using SafeDalat_API.Model.DTO.Auth;
using SafeDalat_API.Model.DTO.Auth.SafeDalat_API.Model.DTO.Auth;
using SafeDalat_API.Repositories.Interface;
using SafeDalat_API.Repositories.Services;
namespace SafeDalat_API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class AuthController : ControllerBase
    {
        private readonly AppDbContext _context;
        private readonly IAuthRepository _jwt;
        private readonly IEmailService _emailService;

        public AuthController(AppDbContext context, IAuthRepository jwt, IEmailService emailService)
        {
            _context = context;
            _jwt = jwt;
            _emailService = emailService;

        }

        [HttpPost("register")]
        public async Task<IActionResult> Register(RegisterDTO dto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            if (_context.Users.Any(x => x.Email == dto.Email))
                return BadRequest("Email đã tồn tại");

            var token = Guid.NewGuid().ToString();

            var user = new User
            {
                FullName = dto.FullName,
                Email = dto.Email,
                Password = BCrypt.Net.BCrypt.HashPassword(dto.Password),

                Role = "User",
                IsLocked = true,
                EmailVerified = false,

                VerifyToken = token,
                VerifyTokenExpire = DateTime.UtcNow.AddHours(24),
                CreatedAt = DateTime.UtcNow
            };

            try
            {
                await _emailService.SendVerifyEmail(user.Email, token);
                _context.Users.Add(user);
                await _context.SaveChangesAsync();
            }
            catch
            {
                return StatusCode(500, "Không gửi được email xác minh");
            }

            return Ok("Vui lòng kiểm tra email để xác minh tài khoản");
        }



        [HttpPost("login")]
        public IActionResult Login(LoginDTO dto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var user = _context.Users.FirstOrDefault(x => x.Email == dto.Email);
            if (user == null || !BCrypt.Net.BCrypt.Verify(dto.Password, user.Password))
                return Unauthorized("Sai tài khoản hoặc mật khẩu");

            if (user.IsLocked || !user.EmailVerified)
                return Unauthorized("Tài khoản chưa xác minh email");

            var token = _jwt.GenerateToken(user);

            return Ok(new { token });
        }

        [Authorize]
        [HttpPut("change-password")]
        public async Task<IActionResult> ChangePassword(ChangePasswordDTO dto)
        {
            int userId = User.GetUserId();
            await _jwt.ChangePasswordAsync(userId, dto);
            return NoContent();
        }
        [HttpPost("forgot-password")]
        public async Task<IActionResult> ForgotPassword(ForgotPasswordDTO dto)
        {
            var user = await _context.Users.FirstOrDefaultAsync(x => x.Email == dto.Email);
            if (user == null) return Ok("Đã gửi mã xác minh (nếu email tồn tại)"); 

            var code = new Random().Next(100000, 999999).ToString();

            user.ResetPasswordCode = code;
            user.ResetCodeExpire = DateTime.UtcNow.AddMinutes(3); 

            await _context.SaveChangesAsync();

            // Gửi email
            try
            {
                await _emailService.SendResetCode(user.Email, code);
            }
            catch
            {
                return StatusCode(500, "Lỗi gửi email");
            }

            return Ok("Đã gửi mã xác minh về email");
        }

        [HttpPost("check-reset-code")]
        public async Task<IActionResult> CheckResetCode(CheckResetCodeDTO dto)
        {
            var isValid = await _context.Users.AnyAsync(x =>
                x.Email == dto.Email &&
                x.ResetPasswordCode == dto.Code &&
                x.ResetCodeExpire > DateTime.UtcNow
            );

            if (!isValid)
                return BadRequest("Mã xác minh không hợp lệ hoặc đã hết hạn");

            return Ok("Mã hợp lệ");
        }

        [HttpPost("reset-password")]
        public async Task<IActionResult> ResetPassword(ResetPasswordDTO dto)
        {
            // Vẫn phải kiểm tra lại Code một lần nữa để bảo mật
            // Tránh trường hợp hacker bỏ qua bước 2 và gọi thẳng API bước 3
            var user = await _context.Users.FirstOrDefaultAsync(x =>
                x.Email == dto.Email &&
                x.ResetPasswordCode == dto.Code &&
                x.ResetCodeExpire > DateTime.UtcNow
            );

            if (user == null)
                return BadRequest("Phiên giao dịch hết hạn, vui lòng xin lại mã mới");

            // Hash mật khẩu mới
            user.Password = BCrypt.Net.BCrypt.HashPassword(dto.NewPassword);

            // Xóa mã OTP để không dùng lại được nữa
            user.ResetPasswordCode = null;
            user.ResetCodeExpire = null;

            await _context.SaveChangesAsync();

            return Ok("Đổi mật khẩu thành công. Vui lòng đăng nhập lại.");
        }
        [Authorize]
        [HttpGet("profile")]
        public async Task<IActionResult> Profile()
        {
            int userId = User.GetUserId();
            return Ok(await _jwt.GetProfileAsync(userId));
        }

        [Authorize]
        [HttpPut("update-profile")]
        public async Task<IActionResult> UpdateProfile(UpdateProfileDTO dto)
        {
            int userId = User.GetUserId();
            var message = await _jwt.UpdateProfileAsync(userId, dto);

            if (message.Contains("thành công"))
                return Ok(new { message }); // Trả về 200 kèm thông báo

            return BadRequest(message); // Trả về 400 nếu lỗi (trùng email...)
        }
        [Authorize]
        [HttpGet("dashboard")]
        public async Task<IActionResult> Dashboard()
        {
            int userId = User.GetUserId();
            return Ok(await _jwt.GetDashboardAsync(userId));
        }
        [HttpGet("get-all")]
        public async Task<IActionResult> GetAll()
        {
            return Ok(await _jwt.GetAllUsersAsync());
        }


        [Authorize(Roles = "Admin")]
        [HttpPut("{id}/lock")]
        public async Task<IActionResult> Lock(int id, [FromBody] LockUserDTO dto)
        {
            // Nếu admin không nhập lý do, lấy mặc định
            string reason = string.IsNullOrEmpty(dto.Reason) ? "Vi phạm điều khoản" : dto.Reason;

            if (!await _jwt.LockUserAsync(id, reason))
                return NotFound("Không tìm thấy người dùng");

            return Ok($"Đã khóa tài khoản và gửi mail với lý do: {reason}");
        }

        [Authorize(Roles = "Admin")]
        [HttpPut("{id}/unlock")]
        public async Task<IActionResult> Unlock(int id, [FromBody] LockUserDTO dto)
        {
            string reason = string.IsNullOrEmpty(dto.Reason) ? "Đã xem xét và mở lại" : dto.Reason;

            if (!await _jwt.UnlockUserAsync(id, reason))
                return NotFound("Không tìm thấy người dùng");

            return Ok("Đã mở khóa tài khoản thành công.");
        }
        [HttpGet("verify-email")]
        public async Task<IActionResult> VerifyEmail([FromQuery] string token)
        {
            var user = await _context.Users.FirstOrDefaultAsync(x =>
                x.VerifyToken == token &&
                x.VerifyTokenExpire > DateTime.UtcNow
            );

            if (user == null)
                return BadRequest("Token không hợp lệ hoặc đã hết hạn");

            user.EmailVerified = true;
            user.IsLocked = false;
            user.VerifyToken = null;
            user.VerifyTokenExpire = null;

            await _context.SaveChangesAsync();

            return Ok("Xác minh email thành công");
        }

    }
}
