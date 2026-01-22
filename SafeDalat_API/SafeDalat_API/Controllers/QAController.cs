using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SafeDalat_API.Helpers;
using SafeDalat_API.Model.DTO.QA;
using SafeDalat_API.Repositories.Interface;
using System.Security.Claims;

namespace SafeDalat_API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class QAController : ControllerBase
    {
        private readonly IQARepository _repo;
        private readonly IIncidentRepository _incidentRepo;

        public QAController(IQARepository repo, IIncidentRepository incidentRepo)
        {
            _repo = repo;
            _incidentRepo = incidentRepo;
        }

        [HttpGet]
        [AllowAnonymous]
        public async Task<IActionResult> GetAll()
        {
           
            if (User.Identity.IsAuthenticated)
            {
                var role = User.FindFirst(ClaimTypes.Role)?.Value;
                var userId = User.GetUserId();

              
                if (role == "Staff")
                {
                    
                    var deptId = await _incidentRepo.GetUserDepartmentIdAsync(userId);

                    if (deptId != null)
                    {
                        
                        return Ok(await _repo.GetQuestionsByDepartmentAsync(deptId.Value));
                    }
                }
            }

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
        [Authorize(Roles = "Admin, Staff")] 
        public async Task<IActionResult> Answer(int id, CreateAnswerDTO dto)
        {
            int responderId = User.GetUserId(); 

            if (!await _repo.CreateAnswerAsync(id, responderId, dto))
                return NotFound();

            return NoContent();
        }
        [HttpGet("categories")]
        [AllowAnonymous] 
        public async Task<IActionResult> GetCategories()
        {
            return Ok(await _repo.GetAllCategoriesAsync());
        }
        [HttpGet("my-questions")]
        [Authorize] 
        public async Task<IActionResult> GetMyQuestions()
        {
            int userId = User.GetUserId();
            return Ok(await _repo.GetQuestionsByUserIdAsync(userId));
        }
    }
}