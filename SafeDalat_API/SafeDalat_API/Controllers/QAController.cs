using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using SafeDalat_API.Helpers;
using SafeDalat_API.Model.DTO.QA;
using SafeDalat_API.Repositories.Interface;

namespace SafeDalat_API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class QAController : ControllerBase
    {
        private readonly IQARepository _repo;

        public QAController(IQARepository repo)
        {
            _repo = repo;
        }

        [HttpGet]
        [AllowAnonymous]
        public async Task<IActionResult> GetAll()
        {
            return Ok(await _repo.GetAllQuestionsAsync());
        }

        [HttpPost]
        [Authorize]
        public async Task<IActionResult> CreateQuestion(CreateQuestionDTO dto)
        {
            int userId = User.GetUserId();
            return Ok(await _repo.CreateQuestionAsync(userId, dto));
        }

        [HttpPost("{id}/answer")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> Answer(
            int id,
            CreateAnswerDTO dto)
        {
            int adminId = User.GetUserId();

            if (!await _repo.CreateAnswerAsync(id, adminId, dto))
                return NotFound();

            return NoContent();
        }
    }
}
