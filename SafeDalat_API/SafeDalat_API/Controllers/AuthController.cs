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

            _context.Users.Add(user);
            await _context.SaveChangesAsync();

            await _emailService.SendVerifyEmail(user.Email, token);

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
            if (user == null) return Ok(); // tránh lộ email

            var code = new Random().Next(100000, 999999).ToString();

            user.ResetPasswordCode = code;
            user.ResetCodeExpire = DateTime.UtcNow.AddMinutes(10);

            await _context.SaveChangesAsync();
            await _emailService.SendResetCode(user.Email, code);

            return Ok("Đã gửi mã xác minh về email");
        }
        [HttpPost("verify-reset-code")]
        public async Task<IActionResult> VerifyResetCode(VerifyResetCodeDTO dto)
        {
            var user = await _context.Users.FirstOrDefaultAsync(x =>
                x.Email == dto.Email &&
                x.ResetPasswordCode == dto.Code &&
                x.ResetCodeExpire > DateTime.UtcNow
            );

            if (user == null)
                return BadRequest("Mã xác minh không hợp lệ hoặc đã hết hạn");

            user.Password = BCrypt.Net.BCrypt.HashPassword(dto.NewPassword);
            user.ResetPasswordCode = null;
            user.ResetCodeExpire = null;

            await _context.SaveChangesAsync();

            return Ok("Đổi mật khẩu thành công");
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
            await _jwt.UpdateProfileAsync(userId, dto);
            return NoContent();
        }
        [Authorize]
        [HttpGet("dashboard")]
        public async Task<IActionResult> Dashboard()
        {
            int userId = User.GetUserId();
            return Ok(await _jwt.GetDashboardAsync(userId));
        }
        [HttpGet]
        public async Task<IActionResult> GetAll()
        {
            return Ok(await _jwt.GetAllUsersAsync());
        }


        [HttpPut("{id}/lock")]
        public async Task<IActionResult> Lock(int id)
        {
            if (!await _jwt.LockUserAsync(id))
                return NotFound();

            return NoContent();
        }

        [HttpPut("{id}/unlock")]
        public async Task<IActionResult> Unlock(int id)
        {
            if (!await _jwt.UnlockUserAsync(id))
                return NotFound();

            return NoContent();
        }

    }
}
