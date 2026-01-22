using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SafeDalat_API.Model.DTO.Department;
using SafeDalat_API.Repositories.Interface;

namespace SafeDalat_API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class DepartmentsController : ControllerBase
    {
        private readonly IDepartmentRepository _repo;

        public DepartmentsController(IDepartmentRepository repo)
        {
            _repo = repo;
        }

        [HttpGet("get-all")]
        public async Task<IActionResult> GetAll()
        {
            return Ok(await _repo.GetAllAsync());
        }

        [HttpGet("get-by-id/{id}")]
        public async Task<IActionResult> GetById(int id)
        {
            var result = await _repo.GetByIdAsync(id);
            if (result == null) return NotFound();
            return Ok(result);
        }

        [HttpPost("create")]
        public async Task<IActionResult> Create(CreateDepartmentDTO dto)
        {
            return Ok(await _repo.CreateAsync(dto));
        }

        [HttpPut("update-by-id/{id}")]
        public async Task<IActionResult> Update(int id, UpdateDepartmentDTO dto)
        {
            var result = await _repo.UpdateAsync(id, dto);
            if (result == null) return NotFound();
            return Ok(result);
        }

        [HttpDelete("delete-by-id/{id}")]
        public async Task<IActionResult> Delete(int id)
        {
            if (!await _repo.DeleteAsync(id)) return NotFound();
            return NoContent();
        }
    }
}