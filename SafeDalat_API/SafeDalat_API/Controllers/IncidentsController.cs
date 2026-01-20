using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using SafeDalat_API.Data;
using SafeDalat_API.Helpers;
using SafeDalat_API.Model.DTO;
using SafeDalat_API.Model.DTO.Icident;
using SafeDalat_API.Model.DTO.Incident;
using SafeDalat_API.Repositories.Interface;
using System.Security.Claims;

namespace SafeDalat_API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class IncidentsController : ControllerBase
    {
        private readonly IIncidentRepository _repo;
        private readonly AppDbContext _context;

        public IncidentsController(IIncidentRepository repo, AppDbContext context)
        {
            _repo = repo;
            _context = context;
        }
        [Authorize]
        [HttpPost("create")]
        public async Task<IActionResult> Create(CreateIncidentDTO dto)
        {
            try
            {
                int userId = User.GetUserId();
                var user = await _context.Users.FindAsync(userId);

                if (user != null)
                {
                    // 1. Check khóa vĩnh viễn (Admin khóa)
                    if (user.IsLocked)
                        return BadRequest(new { message = "Tài khoản của bạn đã bị khóa vĩnh viễn do vi phạm chính sách." });

                    // 2. Check khóa tạm thời (Do spam ảnh)
                    if (user.LockUntil.HasValue && user.LockUntil > DateTime.UtcNow)
                    {
                        var minutesLeft = (int)(user.LockUntil.Value - DateTime.UtcNow).TotalMinutes;
                        return BadRequest(new { message = $"Tài khoản đang trong thời gian tạm khóa. Vui lòng quay lại sau {minutesLeft} phút." });
                    }
                }
                var result = await _repo.CreateAsync(userId, dto);
                return Ok(result);
            }
            catch (Exception ex)
            {
                // 1. Lỗi gợi ý Vote (Do đông người báo)
                if (ex.Message.StartsWith("SUGGEST_VOTE:"))
                {
                    return Conflict(new
                    {
                        type = "vote", // App sẽ hiện Popup hỏi: "Muốn Vote hay Tạo tiếp?"
                        message = ex.Message.Replace("SUGGEST_VOTE: ", "")
                    });
                }

                // 2. Lỗi cảnh báo khoảng cách (Báo cáo nguội)
                if (ex.Message.StartsWith("DISTANCE_WARNING:"))
                {
                    return Conflict(new
                    {
                        type = "distance", // App sẽ hiện Popup hỏi: "Bạn đang báo cáo từ xa?"
                        message = ex.Message.Replace("DISTANCE_WARNING: ", "")
                    });
                }

                // 3. Các lỗi chặn cứng (Spam cá nhân, Rate limit...) -> 400 Bad Request
                return BadRequest(new { message = ex.Message });
            }
        }
        // [FIX] Thêm API để Admin xem lịch sử của User khác
        [Authorize(Roles = "Admin")]
        [HttpGet("admin/get-by-user/{userId}")]
        public async Task<IActionResult> GetIncidentsByUserIdForAdmin(int userId)
        {
            var incidents = await _repo.GetByUserAsync(userId);
            return Ok(incidents);
        }
        [Authorize(Roles = "Admin")]
        [HttpGet("admin/get-by-department/{deptId}")]
        public async Task<IActionResult> GetIncidentsByDepartmentForAdmin(int deptId)
        {
            // Tái sử dụng hàm GetByDepartmentAsync có sẵn trong Repo
            var incidents = await _repo.GetByDepartmentAsync(deptId);
            return Ok(incidents);
        }
        [Authorize]
        [HttpGet("my-incident")]
        public async Task<IActionResult> MyIncidents()
        {
            int userId = User.GetUserId();
            return Ok(await _repo.GetByUserAsync(userId));
        }

        [Authorize(Roles = "Admin, Staff")] 
        [HttpGet]
        public async Task<IActionResult> GetAll()
        {
            var role = User.FindFirst(ClaimTypes.Role)?.Value;
            var userId = User.GetUserId();


            if (role == "Admin")
            {
                return Ok(await _repo.GetAllAsync());
            }


            if (role == "Staff")
            {

                var deptId = await _repo.GetUserDepartmentIdAsync(userId);

                if (deptId == null)
                {

                    return BadRequest("Tài khoản nhân viên này chưa được gán vào phòng ban nào.");
                }

                return Ok(await _repo.GetByDepartmentAsync(deptId.Value));
            }

            return Forbid();
        }

        [HttpGet("get-by-id/{id}")]
        public async Task<IActionResult> Detail(int id)
        {
            var result = await _repo.GetDetailAsync(id);
            if (result == null) return NotFound();


            if (!result.IsPublic)
            {
                if (!User.Identity!.IsAuthenticated)
                    return Forbid();

                var userId = User.GetUserId();
                var role = User.FindFirst(ClaimTypes.Role)!.Value;

               
                bool isOwner = result.UserId == userId;
                bool isAdmin = role == "Admin";
                bool isStaff = role == "Staff"; 

                if (!isAdmin && !isOwner && !isStaff)
                    return Forbid();
            }

            return Ok(result);
        }


        [Authorize(Roles = "Admin, Staff")]
        [HttpPut("update-by-id/{id}")]
        public async Task<IActionResult> UpdateStatus(int id, UpdateIncidentStatusDTO dto)
        {
            int responderId = User.GetUserId(); 

            if (!await _repo.UpdateStatusAsync(id, responderId, dto))
                return NotFound();

            return NoContent();
        }
        [HttpGet("feed")]
        public async Task<IActionResult> PublicFeed()
        {
            return Ok(await _repo.GetPublicFeedAsync());
        }
        [Authorize(Roles = "Admin")]
        [HttpGet("suggest-duplicates/{id}")]
        public async Task<IActionResult> Suggest(int id)
        {
            return Ok(await _repo.SuggestDuplicatesAsync(id));
        }
        [Authorize(Roles = "Admin")]
        [HttpPost("merge")]
        public async Task<IActionResult> Merge(MergeIncidentDTO dto)
        {
            int adminId = User.GetUserId();
            if (!await _repo.MergeAsync(dto, adminId))
                return BadRequest();

            return NoContent();
        }
        [HttpGet("map")]
        public async Task<IActionResult> Map()
        {
            return Ok(await _repo.GetMapAsync());
        }
        [HttpPost("search")]
        public async Task<IActionResult> Search(IncidentSearchDTO dto)
        {
            return Ok(await _repo.SearchAsync(dto));
        }

    }
}
