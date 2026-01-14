using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using SafeDalat_API.Repositories.Interface;

namespace SafeDalat_API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class EnvironmentController : ControllerBase
    {
        private readonly IEnvironmentRepository _repo;

        public EnvironmentController(IEnvironmentRepository repo)
        {
            _repo = repo;
        }

        [HttpGet("air-quality")]
        public async Task<IActionResult> AirQuality()
            => Ok(await _repo.GetAirQualityAsync());

        [HttpGet("weather")]
        public async Task<IActionResult> Weather()
            => Ok(await _repo.GetWeatherAsync());
    }
}
