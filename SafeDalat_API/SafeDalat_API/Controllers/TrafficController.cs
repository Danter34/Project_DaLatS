using Microsoft.AspNetCore.Mvc;
using SafeDalat_API.Repositories.Interface;

namespace SafeDalat_API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class TrafficController : ControllerBase
    {
        private readonly ITrafficRepository _repo;

        public TrafficController(ITrafficRepository repo)
        {
            _repo = repo;
        }

        [HttpGet("hotspots")]
        public async Task<IActionResult> GetTrafficHotspots()
        {
            var data = await _repo.GetHotspotsAsync();
            return Ok(data);
        }
    }
}