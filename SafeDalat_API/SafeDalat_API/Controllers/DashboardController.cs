using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using SafeDalat_API.Repositories.Interface;

namespace SafeDalat_API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class DashboardController : ControllerBase
    {
        private readonly IDashboardRepository _repo;

        public DashboardController(IDashboardRepository repo)
        {
            _repo = repo;
        }
        [Authorize(Roles = "Admin")]
        [HttpGet("summary")]
        public async Task<IActionResult> Summary()
        {
            return Ok(await _repo.GetSummaryAsync());
        }
        [Authorize(Roles = "Admin")]
        [HttpGet("by-alert")]
        public async Task<IActionResult> ByAlert()
        {
            return Ok(await _repo.GetByAlertAsync());
        }
        [Authorize(Roles = "Admin")]
        [HttpGet("by-category")]
        public async Task<IActionResult> ByCategory()
        {
            return Ok(await _repo.GetByCategoryAsync());
        }
    }
}
