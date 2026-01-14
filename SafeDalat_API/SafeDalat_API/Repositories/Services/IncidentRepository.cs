using Microsoft.EntityFrameworkCore;
using SafeDalat_API.Data;
using SafeDalat_API.Model.Domain;
using SafeDalat_API.Model.DTO;
using SafeDalat_API.Model.DTO.Icident;
using SafeDalat_API.Model.DTO.Incident;
using SafeDalat_API.Repositories.Interface;

namespace SafeDalat_API.Repositories.Services
{
    public class IncidentRepository : IIncidentRepository
    {
        private readonly AppDbContext _context;
        private readonly INotificationRepository _notificationRepo;

        public IncidentRepository(
            AppDbContext context,
            INotificationRepository notificationRepo)
        {
            _context = context;
            _notificationRepo = notificationRepo;
        }

        // CREATE 
        public async Task<IncidentResponseDTO> CreateAsync(int userId, CreateIncidentDTO dto)
        {
            var incident = new Incident
            {
                Title = dto.Title,
                Description = dto.Description,
                Address = dto.Address,
                Ward = dto.Ward,
                StreetName = dto.StreetName,
                Latitude = dto.Latitude,
                Longitude = dto.Longitude,

                CategoryId = dto.CategoryId,
                UserId = userId,
                IsPublic = false,
                Status = "Chờ xử lý",
                AlertLevel = AlertLevel.Green,
                IsMaster = true,
                CreatedAt = DateTime.UtcNow
            };

            _context.Incidents.Add(incident);
            await _context.SaveChangesAsync();

            return (await GetDetailAsync(incident.IncidentId))!;
        }

        //  GET BY USER 
        public async Task<List<IncidentResponseDTO>> GetByUserAsync(int userId)
        {
            var incidents = await _context.Incidents
                .AsNoTracking()
                .Where(x => x.UserId == userId)
                .Include(x => x.Category)
                .Include(x => x.Images)
                .OrderByDescending(x => x.CreatedAt)
                .ToListAsync();

            return incidents.Select(MapToDto).ToList();
        }

        //  GET ALL
        public async Task<List<IncidentResponseDTO>> GetAllAsync()
        {
            var incidents = await _context.Incidents
                .AsNoTracking()
                .Include(x => x.Category)
                .Include(x => x.Images)
                .OrderByDescending(x => x.CreatedAt)
                .ToListAsync();

            return incidents.Select(MapToDto).ToList();
        }

        // DETAIL 
        public async Task<IncidentResponseDTO?> GetDetailAsync(int id)
        {
            var incident = await _context.Incidents
                .AsNoTracking()
                .Include(x => x.Category)
                .Include(x => x.Images)
                .FirstOrDefaultAsync(x => x.IncidentId == id);

            return incident == null ? null : MapToDto(incident);
        }

        //  UPDATE STATUS 
        public async Task<bool> UpdateStatusAsync(
            int incidentId,
            int adminId,
            UpdateIncidentStatusDTO dto)
        {
            var incident = await _context.Incidents.FindAsync(incidentId);
            if (incident == null) return false;

            incident.Status = dto.Status;

            if (dto.Status == "Đang xử lý" || dto.Status == "Đã hoàn thành")
                incident.IsPublic = true;

            if (dto.Status == "Từ chối")
                incident.IsPublic = false;

            _context.IncidentStatusHistories.Add(new IncidentStatusHistory
            {
                IncidentId = incidentId,
                Status = dto.Status,
                Note = dto.Note,
                AdminId = adminId,
                UpdatedAt = DateTime.UtcNow
            });

            await _notificationRepo.CreateAsync(
                incident.UserId,
                $"Sự cố \"{incident.Title}\" đã được cập nhật trạng thái: {dto.Status}"
            );

            await _context.SaveChangesAsync();
            return true;
        }

        // MAP TO DTO 
        private static IncidentResponseDTO MapToDto(Incident x)
        {
            return new IncidentResponseDTO
            {
                IncidentId = x.IncidentId,
                Title = x.Title,
                Description = x.Description,
                Address = x.Address,
                Ward = x.Ward,
                StreetName = x.StreetName,
                Latitude = x.Latitude,
                Longitude = x.Longitude,
                Status = x.Status,
                AlertLevel = (int)x.AlertLevel,
                IsMaster = x.IsMaster,
                CreatedAt = x.CreatedAt,
                IsPublic = x.IsPublic,
                UserId = x.UserId,
                CategoryName = x.Category?.Name ?? string.Empty,

                Images = x.Images?.Select(i => new IncidentImageDTO
                {
                    ImageId = i.ImageId,
                    FilePath = i.FilePath
                }).ToList() ?? new List<IncidentImageDTO>()
            };
        }

        //  PUBLIC FEED 
        public async Task<List<IncidentFeedDTO>> GetPublicFeedAsync()
        {
            var incidents = await _context.Incidents
                .AsNoTracking()
                .Include(x => x.Category)
                .Include(x => x.Images)
                .Where(x =>
                    x.IsMaster &&
                    x.Status != "Chờ xử lý" &&
                    x.Status != "Từ chối")
                .OrderByDescending(x => x.CreatedAt)
                .ToListAsync();

            return incidents.Select(x => new IncidentFeedDTO
            {
                IncidentId = x.IncidentId,
                Title = x.Title,
                Description = x.Description,
                Ward = x.Ward,
                StreetName = x.StreetName,
                Latitude = x.Latitude,
                Longitude = x.Longitude,
                Status = x.Status,
                AlertLevel = (int)x.AlertLevel,
                CreatedAt = x.CreatedAt,
                CategoryName = x.Category!.Name,
                Images = x.Images.Select(i => new IncidentImageDTO
                {
                    ImageId = i.ImageId,
                    FilePath = i.FilePath
                }).ToList()
            }).ToList();
        }

        //  SUGGEST DUPLICATES 
        public async Task<List<IncidentResponseDTO>> SuggestDuplicatesAsync(int incidentId)
        {
            var master = await _context.Incidents.FindAsync(incidentId);
            if (master == null) return new();

            const double maxDistance = 0.001;

            var candidates = await _context.Incidents
                .AsNoTracking()
                .Include(x => x.Category)
                .Include(x => x.Images)
                .Where(x =>
                    x.IncidentId != incidentId &&
                    x.CategoryId == master.CategoryId &&
                    x.Ward == master.Ward &&
                    x.Status == "Chờ xử lý" &&
                    Math.Abs(x.Latitude - master.Latitude) <= maxDistance &&
                    Math.Abs(x.Longitude - master.Longitude) <= maxDistance)
                .ToListAsync();

            return candidates.Select(MapToDto).ToList();
        }

        // MERGE 
        public async Task<bool> MergeAsync(MergeIncidentDTO dto, int adminId)
        {
            var master = await _context.Incidents.FindAsync(dto.MasterIncidentId);
            if (master == null) return false;

            foreach (var dupId in dto.DuplicateIncidentIds)
            {
                if (dupId == master.IncidentId) continue;

                var duplicate = await _context.Incidents.FindAsync(dupId);
                if (duplicate == null) continue;

                duplicate.IsMaster = false;
                duplicate.Status = "Đã gộp";

                _context.IncidentDuplicates.Add(new IncidentDuplicate
                {
                    MasterIncidentId = master.IncidentId,
                    DuplicateIncidentId = duplicate.IncidentId
                });
            }

            master.Status = "Đang xử lý";

            _context.IncidentStatusHistories.Add(new IncidentStatusHistory
            {
                IncidentId = master.IncidentId,
                Status = "Đã gộp sự cố",
                Note = "Gộp các sự cố trùng",
                AdminId = adminId
            });

            await _context.SaveChangesAsync();
            return true;
        }

        //  MAP 
        public async Task<List<IncidentMapDTO>> GetMapAsync()
        {
            return await _context.Incidents
                .AsNoTracking()
                .Include(x => x.Category)
                .Where(x =>
                    x.IsMaster &&
                    x.Status != "Chờ xử lý" &&
                    x.Status != "Từ chối")
                .Select(x => new IncidentMapDTO
                {
                    IncidentId = x.IncidentId,
                    Title = x.Title,
                    Latitude = x.Latitude,
                    Longitude = x.Longitude,
                    Status = x.Status,
                    AlertLevel = (int)x.AlertLevel,
                    CategoryName = x.Category!.Name
                })
                .ToListAsync();
        }

        //  SEARCH
        public async Task<List<IncidentFeedDTO>> SearchAsync(IncidentSearchDTO dto)
        {
            var query = _context.Incidents
                .AsNoTracking()
                .Include(x => x.Category)
                .Include(x => x.Images)
                .Where(x =>
                    x.IsMaster &&
                    x.Status != "Chờ xử lý" &&
                    x.Status != "Từ chối")
                .AsQueryable();

            if (!string.IsNullOrWhiteSpace(dto.Keyword))
                query = query.Where(x =>
                    x.Title.Contains(dto.Keyword) ||
                    x.Description.Contains(dto.Keyword));

            if (!string.IsNullOrWhiteSpace(dto.Ward))
                query = query.Where(x => x.Ward == dto.Ward);

            if (dto.CategoryId.HasValue)
                query = query.Where(x => x.CategoryId == dto.CategoryId);

            if (dto.AlertLevel.HasValue)
                query = query.Where(x => (int)x.AlertLevel == dto.AlertLevel);

            if (dto.Latitude.HasValue && dto.Longitude.HasValue && dto.RadiusKm.HasValue)
            {
                double r = dto.RadiusKm.Value / 111;
                query = query.Where(x =>
                    Math.Abs((double)(x.Latitude - dto.Latitude)) <= r &&
                    Math.Abs((double)(x.Longitude - dto.Longitude)) <= r);
            }

            var incidents = await query
                .OrderByDescending(x => x.CreatedAt)
                .ToListAsync();

            return incidents.Select(x => new IncidentFeedDTO
            {
                IncidentId = x.IncidentId,
                Title = x.Title,
                Description = x.Description,
                Ward = x.Ward,
                StreetName = x.StreetName,
                Latitude = x.Latitude,
                Longitude = x.Longitude,
                Status = x.Status,
                AlertLevel = (int)x.AlertLevel,
                CreatedAt = x.CreatedAt,
                CategoryName = x.Category!.Name,
                Images = x.Images.Select(i => new IncidentImageDTO
                {
                    ImageId = i.ImageId,
                    FilePath = i.FilePath
                }).ToList()
            }).ToList();
        }
    }
}
