using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using SafeDalat_API.Repositories.Interface;

namespace SafeDalat_API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class IncidentImagesController : ControllerBase
    {
        private readonly IIncidentImageRepository _repo;

        public IncidentImagesController(IIncidentImageRepository repo)
        {
            _repo = repo;
        }

        [HttpPost]
        public async Task<IActionResult> Upload(
            int incidentId,
            [FromForm] List<IFormFile> files)
        {
            try
            {
                
                var result = await _repo.UploadAsync(incidentId, files);
                return Ok(result);
            }
            catch (Exception ex)
            {
               
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}