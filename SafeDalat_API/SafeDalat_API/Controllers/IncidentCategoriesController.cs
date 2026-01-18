using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using SafeDalat_API.Model.DTO.IncidentCategory;
using SafeDalat_API.Repositories.Interface;

namespace SafeDalat_API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class IncidentCategoriesController : ControllerBase
    {
        private readonly IIncidentCategoryRepository _repo;

        public IncidentCategoriesController(IIncidentCategoryRepository repo)
        {
            _repo = repo;
        }

   
        [HttpGet("get-all")]
        public async Task<IActionResult> GetAll()
        {
            return Ok(await _repo.GetAllAsync());
        }

        // Admin
        [Authorize(Roles = "Admin")]
        [HttpPost]
        public async Task<IActionResult> Create(CreateIncidentCategoryDTO dto)
        {
            return Ok(await _repo.CreateAsync(dto));
        }

        [Authorize(Roles = "Admin")]
        [HttpPut("update-by-id/{id}")]
        public async Task<IActionResult> Update(int id, CreateIncidentCategoryDTO dto)
        {
            var result = await _repo.UpdateAsync(id, dto);
            if (result == null) return NotFound();

            return Ok(result);
        }

        [Authorize(Roles = "Admin")]
        [HttpDelete("delete-by-id/{id}")]
        public async Task<IActionResult> Delete(int id)
        {
            if (!await _repo.DeleteAsync(id))
                return NotFound();

            return NoContent();
        }
    }
}
