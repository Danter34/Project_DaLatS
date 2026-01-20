using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using SafeDalat_API.Helpers;
using SafeDalat_API.Model.DTO.Notification;
using SafeDalat_API.Repositories.Interface;

namespace SafeDalat_API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class NotificationsController : ControllerBase
    {
        private readonly INotificationRepository _repo;

        public NotificationsController(INotificationRepository repo)
        {
            _repo = repo;
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
    }
}
