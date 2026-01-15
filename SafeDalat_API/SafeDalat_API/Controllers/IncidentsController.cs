using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
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

        public IncidentsController(IIncidentRepository repo)
        {
            _repo = repo;
        }
        [Authorize]
        [HttpPost("create")]
        public async Task<IActionResult> Create(CreateIncidentDTO dto)
        {
            int userId = User.GetUserId();

            return Ok(await _repo.CreateAsync(userId, dto));
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
