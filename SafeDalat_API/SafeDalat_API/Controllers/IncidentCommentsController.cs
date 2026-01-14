using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using SafeDalat_API.Helpers;
using SafeDalat_API.Model.DTO.Comment;
using SafeDalat_API.Repositories.Interface;

namespace SafeDalat_API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class IncidentCommentsController : ControllerBase
    {
        private readonly IIncidentCommentRepository _repo;

        public IncidentCommentsController(IIncidentCommentRepository repo)
        {
            _repo = repo;
        }

        [HttpGet("Get")]
        public async Task<IActionResult> Get(int incidentId)
        {
            return Ok(await _repo.GetByIncidentAsync(incidentId));
        }

        [HttpPost("Create")]
        [Authorize]
        public async Task<IActionResult> Create(
            int incidentId,
            CreateCommentDTO dto)
        {
            int userId = User.GetUserId();
            return Ok(await _repo.CreateAsync(incidentId, userId, dto));
        }
    }
}
