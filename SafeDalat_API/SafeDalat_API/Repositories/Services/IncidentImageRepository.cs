using SafeDalat_API.Data;
using SafeDalat_API.Model.Domain;
using SafeDalat_API.Repositories.Interface;

namespace SafeDalat_API.Repositories.Services
{
    public class IncidentImageRepository : IIncidentImageRepository
    {
        private readonly AppDbContext _context;
        private readonly IWebHostEnvironment _env;

        public IncidentImageRepository(AppDbContext context, IWebHostEnvironment env)
        {
            _context = context;
            _env = env;
        }

        public async Task<List<string>> UploadAsync(int incidentId, List<IFormFile> files)
        {
            var incident = await _context.Incidents.FindAsync(incidentId);
            if (incident == null) throw new Exception("Incident not found");

            var uploadFolder = Path.Combine(_env.WebRootPath, "uploads", "incidents");
            Directory.CreateDirectory(uploadFolder);

            var result = new List<string>();

            foreach (var file in files)
            {
                var fileName = $"{Guid.NewGuid()}{Path.GetExtension(file.FileName)}";
                var filePath = Path.Combine(uploadFolder, fileName);

                using var stream = new FileStream(filePath, FileMode.Create);
                await file.CopyToAsync(stream);

                _context.IncidentImages.Add(new IncidentImage
                {
                    IncidentId = incidentId,
                    FileName = file.FileName,
                    FilePath = $"/uploads/incidents/{fileName}",
                    ContentType = file.ContentType,
                    FileSize = file.Length
                });

                result.Add($"/uploads/incidents/{fileName}");
            }

            await _context.SaveChangesAsync();
            return result;
        }
    }
}
