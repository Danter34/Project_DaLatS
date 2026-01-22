using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using SafeDalat_API.Data;
using SafeDalat_API.Model.Domain;
using SafeDalat_API.Model.DTO.Auth;
using SafeDalat_API.Repositories.Interface;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
namespace SafeDalat_API.Repositories.Services
{
    public class AuthRepository:IAuthRepository
    {
        private readonly IConfiguration _config;
        private readonly AppDbContext _context;
        private readonly IEmailService _emailService;

        public AuthRepository(IConfiguration config, AppDbContext context, IEmailService emailService)
        {
            _config = config;
            _context = context;
            _emailService = emailService;
        }

        public string GenerateToken(User user)
        {
            var claims = new List<Claim>
        {
            new Claim(ClaimTypes.NameIdentifier, user.UserId.ToString()),
            new Claim(ClaimTypes.Email, user.Email),
            new Claim(ClaimTypes.Role, user.Role)
        };

            var key = new SymmetricSecurityKey(
                Encoding.UTF8.GetBytes(_config["Jwt:Key"]!)
            );

            var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

            var token = new JwtSecurityToken(
                issuer: _config["Jwt:Issuer"],
                audience: _config["Jwt:Audience"],
                claims: claims,
                expires: DateTime.Now.AddHours(
                    double.Parse(_config["Jwt:ExpireHours"]!)
                ),
                signingCredentials: creds
            );

            return new JwtSecurityTokenHandler().WriteToken(token);
        }
        public async Task<bool> ChangePasswordAsync(int userId, ChangePasswordDTO dto)
        {
            var user = await _context.Users.FindAsync(userId);
            if (user == null) return false;

            if (!BCrypt.Net.BCrypt.Verify(dto.OldPassword, user.Password))
                throw new Exception("Mật khẩu cũ không đúng");

            user.Password = BCrypt.Net.BCrypt.HashPassword(dto.NewPassword);
            await _context.SaveChangesAsync();

            return true;
        }
        public async Task<UserProfileDTO> GetProfileAsync(int userId)
        {
            return await _context.Users
                .AsNoTracking()
                .Include(u => u.Department)
                .Where(x => x.UserId == userId)
                .Select(x => new UserProfileDTO
                {
                    FullName = x.FullName,
                    Email = x.Email,
                    CreatedAt = x.CreatedAt,
                    DepartmentName = x.Department != null ? x.Department.Name : "Chưa phân bổ",
                    DepartmentId = x.DepartmentId
                })
                .FirstAsync();
        }
        public async Task<string> UpdateProfileAsync(int userId, UpdateProfileDTO dto)
        {
            var user = await _context.Users.FindAsync(userId);
            if (user == null) return "Người dùng không tồn tại";

            user.FullName = dto.FullName;

            // Nếu Email thay đổi
            if (user.Email != dto.Email)
            {
                // Kiểm tra email mới có trùng ai không
                if (await _context.Users.AnyAsync(x => x.Email == dto.Email && x.UserId != userId))
                    return "Email mới đã được sử dụng bởi tài khoản khác";

                // Cập nhật email mới
                user.Email = dto.Email;

                // Reset trạng thái xác minh
                user.EmailVerified = false;
                user.IsLocked = true; 

                // Tạo token xác minh mới
                var token = Guid.NewGuid().ToString();
                user.VerifyToken = token;
                user.VerifyTokenExpire = DateTime.UtcNow.AddHours(24);

                // Gửi mail xác minh
                try
                {
                    await _emailService.SendVerifyEmail(user.Email, token);
                }
                catch
                {
                    return "Lỗi gửi email xác minh. Vui lòng thử lại.";
                }
            }

            await _context.SaveChangesAsync();

            if (!user.EmailVerified)
                return "Cập nhật thành công. Vui lòng kiểm tra email để xác minh lại tài khoản mới.";

            return "Cập nhật thông tin thành công.";
        }
        public async Task<UserDashboardDTO> GetDashboardAsync(int userId)
        {
            var incidents = _context.Incidents.AsNoTracking().Where(x => x.UserId == userId);

            // Lấy thông tin User để xem điểm
            var user = await _context.Users.FindAsync(userId);
            int currentScore = user?.TrustScore ?? 0;

           
            // Mặc định: Uy tín thấp (Limit 1)
            int dailyLimit = 1;
            string status = "Thành viên mới";

            if (currentScore < 0)
            {
                dailyLimit = 0; // Shadow ban
                status = "Bị khóa quyền báo cáo";
            }
            else if (currentScore >= 50) 
            {
                dailyLimit = 5;
                status = "Thành viên uy tín";
            }


            int postedToday = await incidents
                .CountAsync(x => x.CreatedAt.Date == DateTime.UtcNow.Date);

            return new UserDashboardDTO
            {
                TotalIncidents = await incidents.CountAsync(),
                PendingIncidents = await incidents.CountAsync(x => x.Status == "Chờ xử lý"),
                ProcessingIncidents = await incidents.CountAsync(x => x.Status == "Đang xử lý"),
                CompletedIncidents = await incidents.CountAsync(x => x.Status == "Đã hoàn thành"),
                RejectedIncidents = await incidents.CountAsync(x => x.Status == "Từ chối"),
                UnreadNotifications = await _context.Notifications.CountAsync(x => x.UserId == userId && !x.IsRead),

                // Map dữ liệu Trust Score
                TrustScore = currentScore,
                DailyReportLimit = dailyLimit,
                UsedDailyQuota = postedToday,
                TrustStatus = status
            };
        }
        public async Task<List<AdminUserDTO>> GetAllUsersAsync()
        {
            return await _context.Users
                .AsNoTracking()
                .Include(u => u.Department) 
                .Include(u => u.Incidents)
                .OrderByDescending(x => x.CreatedAt)
                .Select(x => new AdminUserDTO
                {
                    UserId = x.UserId,
                    FullName = x.FullName,
                    Email = x.Email,
                    Role = x.Role,
                    IsLocked = x.IsLocked,
                    EmailVerified = x.EmailVerified,
                    CreatedAt = x.CreatedAt,
                    IncidentCount = x.Incidents.Count(),

                    
                    DepartmentId = x.DepartmentId,
                    DepartmentName = x.Department != null ? x.Department.Name : null
                })
                .ToListAsync();
        }
        public async Task<bool> LockUserAsync(int userId, string reason)
        {
            var user = await _context.Users.FindAsync(userId);
            if (user == null) return false;

            user.IsLocked = true;


            await _context.SaveChangesAsync();

            // Gửi mail kèm lý do
            try
            {
                await _emailService.SendAccountLockedNotification(user.Email, reason);
            }
            catch { }

            return true;
        }

        public async Task<bool> UnlockUserAsync(int userId, string reason)
        {
            var user = await _context.Users.FindAsync(userId);
            if (user == null) return false;

            user.IsLocked = false;
            await _context.SaveChangesAsync();

            // Gửi mail kèm lý do (hoặc lời nhắn)
            try
            {
                await _emailService.SendAccountUnlockedNotification(user.Email, reason);
            }
            catch { }

            return true;
        }
        public async Task<StaffDashboardDTO> GetStaffDashboardAsync(int userId)
        {
            // Lấy user để biết phòng ban
            var user = await _context.Users.FindAsync(userId);
            if (user == null || user.DepartmentId == null) return new StaffDashboardDTO();

            // Lấy các sự cố được giao cho phòng ban của staff này (Hoặc giao trực tiếp nếu có logic đó)
            // Ở đây giả sử Staff thấy việc của cả phòng ban
            var tasks = _context.Incidents.AsNoTracking()
                .Where(x => x.AssignedDepartmentId == user.DepartmentId);

            int total = await tasks.CountAsync();
            int completed = await tasks.CountAsync(x => x.Status == "Đã hoàn thành");
            int processing = await tasks.CountAsync(x => x.Status == "Đang xử lý");
            int highPriority = await tasks.CountAsync(x => (int)x.AlertLevel == 3);

            return new StaffDashboardDTO
            {
                AssignedTasks = total,
                CompletedTasks = completed,
                InProgressTasks = processing,
                HighPriorityTasks = highPriority,
                CompletionRate = total > 0 ? Math.Round((double)completed / total * 100, 1) : 0,
                LastActive = DateTime.UtcNow 
            };
        }


    }
}
