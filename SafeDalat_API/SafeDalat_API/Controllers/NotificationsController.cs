using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using SafeDalat_API.Data;
using SafeDalat_API.Helpers;
using SafeDalat_API.Model.DTO.Notification;
using SafeDalat_API.Repositories.Interface;
using SafeDalat_API.Repositories.Services;

namespace SafeDalat_API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class NotificationsController : ControllerBase
    {
        private readonly INotificationRepository _repo;
        private readonly IFcmService _fcmService; 
        private readonly AppDbContext _context;

        public NotificationsController(INotificationRepository repo, IFcmService fcmService, AppDbContext context)
        {
            _repo = repo;
            _fcmService = fcmService;
            _context = context;
        }

        [HttpGet("get")]
        public async Task<IActionResult> MyNotifications()
        {
            int userId = User.GetUserId();
            return Ok(await _repo.GetMyAsync(userId));
        }

        [HttpPut("read/{id}")]
        public async Task<IActionResult> MarkRead(int id)
        {
            int userId = User.GetUserId();
            await _repo.MarkAsReadAsync(id, userId);
            return NoContent();
        }
        [Authorize(Roles = "Admin")]
        [HttpPost("broadcast")]
        public async Task<IActionResult> Broadcast([FromBody] SendNotificationDTO dto)
        {
            await _repo.BroadcastAsync(dto);
            return Ok("Đã gửi thông báo thành công.");
        }
        [Authorize]
        [HttpPost("test-push-me")]
        public async Task<IActionResult> TestPushMe()
        {
        
            int userId = User.GetUserId();

            var user = await _context.Users.FindAsync(userId);
            if (user == null) return NotFound("Không tìm thấy User này trong DB");

            if (string.IsNullOrEmpty(user.FcmToken))
            {
                return BadRequest($"LỖI: User ID {userId} chưa có FCM Token trong Database. Hãy đăng xuất và đăng nhập lại trên App.");
            }

          
            try
            {
                
                await _fcmService.SendToTokenAsync(user.FcmToken, "Test Backend", "Backend gửi thành công rồi nè!");

                return Ok(new
                {
                    message = "Đã gửi lệnh sang Firebase!",
                    tokenInDb = user.FcmToken
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, $"LỖI FIREBASE: {ex.Message}");
            }
        }
    }
}
