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
            var user = await _context.Users.FindAsync(userId);
            if (user == null) throw new Exception("User not found");

            // 1. CHECK SHADOW BAN (Điểm âm)
            if (user.TrustScore < 0)
            {
                throw new Exception("Tài khoản của bạn đã bị khóa tính năng báo cáo do điểm tin cậy quá thấp (Spam/Báo cáo sai).");
            }

            // 2. CHECK DAILY QUOTA (Giới hạn ngày)
            // Quy định: < 50 điểm -> 1 bài/ngày. >= 50 điểm -> 5 bài/ngày
            int limit = user.TrustScore >= 50 ? 5 : 1;

            var countToday = await _context.Incidents
                .CountAsync(x => x.UserId == userId && x.CreatedAt.Date == DateTime.UtcNow.Date);

            if (countToday >= limit)
            {
                throw new Exception($"Bạn đã đạt giới hạn báo cáo trong ngày ({limit} lượt). Hãy tích cực đóng góp để nâng điểm tin cậy!");
            }
            // --- 1. RATE LIMITING: Chặn Spam tần suất (2 phút/lần) ---
            var lastIncident = await _context.Incidents
                .Where(x => x.UserId == userId)
                .OrderByDescending(x => x.CreatedAt)
                .FirstOrDefaultAsync();

            if (lastIncident != null)
            {
                var timeSinceLastPost = DateTime.UtcNow - lastIncident.CreatedAt;
                if (timeSinceLastPost.TotalMinutes < 5)
                {
                    throw new Exception($"Bạn gửi quá nhanh. Vui lòng đợi {5 - (int)timeSinceLastPost.TotalMinutes} phút nữa.");
                }
            }

            // --- 2. DUPLICATE CHECK: Chặn trùng lặp CÁ NHÂN ---
            // (Ngăn user tự spam lại bài của chính mình khi chưa được duyệt)
            double latDiff = 0.001;
            double lngDiff = 0.001;

            bool isPersonalDuplicate = await _context.Incidents.AnyAsync(x =>
                x.UserId == userId &&             // Của chính user này
                x.CategoryId == dto.CategoryId && // Cùng loại
                x.Status == "Chờ xử lý" &&        // Đang chờ duyệt
                Math.Abs(x.Latitude - dto.Latitude) < latDiff &&
                Math.Abs(x.Longitude - dto.Longitude) < lngDiff
            );

            if (isPersonalDuplicate)
            {
                throw new Exception("Bạn đã báo cáo sự cố này rồi. Vui lòng đợi Admin duyệt.");
            }

            // NẾU USER CHƯA XÁC NHẬN (IsForceCreate == false) -> CHẠY CÁC CHECK CẢNH BÁO
            if (!dto.IsForceCreate)
            {
                // --- 3. DISTANCE CHECK: Cảnh báo khoảng cách (Anti-Fake / Offline Report) ---
                double distance = CalculateDistance(dto.Latitude, dto.Longitude, dto.DeviceLatitude, dto.DeviceLongitude);
                double maxDistanceMeters = 500;

                if (distance > maxDistanceMeters)
                {
                    throw new Exception("DISTANCE_WARNING: Vị trí báo cáo cách xa vị trí đứng của bạn. Nếu bạn đang báo cáo nguội (đã chụp ảnh trước đó), hãy xác nhận để tiếp tục.");
                }

                // --- 4. CROWD CHECK: Gợi ý Vote nếu đông người báo ---
                int nearbyCount = await _context.Incidents.CountAsync(x =>
                    x.CategoryId == dto.CategoryId &&
                    (x.Status == "Chờ xử lý" || x.Status == "Đang xử lý") &&
                    Math.Abs(x.Latitude - dto.Latitude) < latDiff &&
                    Math.Abs(x.Longitude - dto.Longitude) < lngDiff
                );

                if (nearbyCount >= 5)
                {
                    throw new Exception("SUGGEST_VOTE: Có vẻ sự cố này đã được báo cáo bởi nhiều người. Bạn có muốn 'Vote' (Ủng hộ) thay vì tạo mới không?");
                }
            }

            // --- LOGIC TẠO MỚI (Khi đã qua hết các ải hoặc đã ForceCreate) ---

            // Tính lại khoảng cách để lưu log (nếu cần)
            double finalDist = CalculateDistance(dto.Latitude, dto.Longitude, dto.DeviceLatitude, dto.DeviceLongitude);

            var incident = new Incident
            {
                Title = dto.Title,
                // Tự động thêm chú thích nếu báo cáo từ xa
                Description = dto.Description + (finalDist > 500 ? "\n[System: Báo cáo từ xa]" : ""),

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
                .Include(x => x.AssignedDepartment)
                .Include(x => x.AsMasterDuplicates)
                .OrderByDescending(x => x.CreatedAt)
                .ToListAsync();

            return incidents.Select(MapToDto).ToList();
        }
        public async Task<List<IncidentResponseDTO>> GetByDepartmentAsync(int departmentId)
        {
            var incidents = await _context.Incidents
                .AsNoTracking()
                .Where(x => x.AssignedDepartmentId == departmentId)
                .Include(x => x.Category)
                .Include(x => x.Images)
                .Include(x => x.AssignedDepartment)
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
                .Include(x => x.AssignedDepartment)
                .Include(x => x.AsMasterDuplicates)
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
                .Include(x => x.AssignedDepartment)
                .Include(x => x.AsMasterDuplicates)
                .FirstOrDefaultAsync(x => x.IncidentId == id);

            return incident == null ? null : MapToDto(incident);
        }

        //  UPDATE STATUS 
        public async Task<bool> UpdateStatusAsync(int incidentId, int responderId, UpdateIncidentStatusDTO dto)
        {
            // [EDIT] Sửa FindAsync thành Include(x => x.User) để lấy được thông tin User (cần để cộng/trừ điểm)
            var incident = await _context.Incidents
                .Include(x => x.User)
                .FirstOrDefaultAsync(x => x.IncidentId == incidentId);

            if (incident == null) return false;

            // [NEW] Lưu lại trạng thái cũ để so sánh (chỉ tính điểm khi chuyển từ "Chờ xử lý")
            string oldStatus = incident.Status;

            // --- LOGIC CŨ GIỮ NGUYÊN ---
            incident.Status = dto.Status;

            if (dto.AlertLevel.HasValue)
            {
                incident.AlertLevel = dto.AlertLevel.Value;
            }

            string deptNameForUser = "";

            if (dto.AssignedDepartmentId.HasValue)
            {
                incident.AssignedDepartmentId = dto.AssignedDepartmentId.Value;

                var department = await _context.Departments.FindAsync(dto.AssignedDepartmentId.Value);
                string deptName = department?.Name ?? "Bộ phận chuyên môn";
                deptNameForUser = $" bởi {deptName}";

                var staffMembers = await _context.Users
                    .Where(u => u.DepartmentId == dto.AssignedDepartmentId.Value && u.Role == "Staff")
                    .ToListAsync();

                foreach (var staff in staffMembers)
                {
                    string urgency = incident.AlertLevel == AlertLevel.Red ? "[KHẨN CẤP] " : "";

                    await _notificationRepo.CreateAsync(
                        staff.UserId,
                        $"{urgency}NHIỆM VỤ MỚI: Sự cố #{incidentId} tại {incident.Ward} đã được giao cho {deptName}."
                    );
                }
            }

            if (dto.Status == "Đang xử lý" || dto.Status == "Đã hoàn thành")
                incident.IsPublic = true;
            else if (dto.Status == "Từ chối" || dto.Status == "Chờ xử lý")
                incident.IsPublic = false;

            _context.IncidentStatusHistories.Add(new IncidentStatusHistory
            {
                IncidentId = incidentId,
                Status = dto.Status,
                Note = dto.Note,
                AdminId = responderId,
                UpdatedAt = DateTime.UtcNow
            });

            // Tạo biến thông báo điểm để ghép vào tin nhắn gửi User
            string scoreMsg = "";

            // Chỉ tính điểm lần đầu tiên khi Admin xử lý (trạng thái cũ là Chờ xử lý)
            if (oldStatus == "Chờ xử lý" && incident.User != null)
            {
                if (dto.Status == "Đang xử lý" || dto.Status == "Đã hoàn thành")
                {
                    // Báo cáo ĐÚNG -> Cộng 10 điểm
                    incident.User.TrustScore += 10;
                    scoreMsg = " (+10 điểm uy tín)";
                }
                else if (dto.Status == "Từ chối")
                {
                    // Báo cáo SAI -> Trừ 20 điểm
                    incident.User.TrustScore -= 20;
                    scoreMsg = " (-20 điểm uy tín)";
                }
            }

            // --- CẬP NHẬT NỘI DUNG THÔNG BÁO CHO USER (GHÉP THÊM ĐIỂM) ---
            string userMessage = "";
            switch (dto.Status)
            {
                case "Từ chối":
                    userMessage = $"Sự cố \"{incident.Title}\" đã bị từ chối{scoreMsg}. Lý do: {dto.Note ?? "Không đúng quy định"}";
                    break;

                case "Đã hoàn thành":
                    userMessage = $"Tin vui! Sự cố \"{incident.Title}\" đã được xử lý hoàn tất{scoreMsg}.";
                    break;

                case "Đang xử lý":
                    userMessage = $"Sự cố \"{incident.Title}\" đang được xử lý{deptNameForUser}{scoreMsg}.";
                    break;

                default:
                    userMessage = $"Sự cố \"{incident.Title}\": Trạng thái cập nhật thành {dto.Status}.";
                    break;
            }

            await _notificationRepo.CreateAsync(incident.UserId, userMessage);

            // Lưu tất cả thay đổi (Sự cố + Lịch sử + Điểm User)
            await _context.SaveChangesAsync();
            return true;
        }

        // MAP TO DTO 
        private static IncidentResponseDTO MapToDto(Incident x)
        {
            int duplicateCount = x.AsMasterDuplicates?.Count ?? 0;
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
                VoteCount = 1 + duplicateCount,

                // BỔ SUNG MAP
                AssignedDepartmentId = x.AssignedDepartmentId,
                AssignedDepartmentName = x.AssignedDepartment?.Name,

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
                .Include(x => x.AssignedDepartment) 
                .Where(x =>
                    x.IsMaster &&
                    x.Status != "Chờ xử lý" &&
                    x.Status != "Từ chối")
                .OrderByDescending(x => x.CreatedAt)
                .Take(5)
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

                
                AssignedDepartmentName = x.AssignedDepartment?.Name,

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
            // 1. Lấy sự cố gốc (Master)
            var master = await _context.Incidents.FindAsync(dto.MasterIncidentId);
            if (master == null) return false;

            // 2. Duyệt qua danh sách sự cố trùng
            foreach (var dupId in dto.DuplicateIncidentIds)
            {
                if (dupId == master.IncidentId) continue;

                // Lấy sự cố trùng KÈM THEO User để cộng điểm
                var duplicate = await _context.Incidents
                    .Include(x => x.User)
                    .FirstOrDefaultAsync(x => x.IncidentId == dupId);

                if (duplicate == null) continue;

                // A. Cập nhật trạng thái sự cố trùng
                duplicate.IsMaster = false;
                duplicate.Status = "Đã gộp";

                // B. Lưu lịch sử liên kết
                _context.IncidentDuplicates.Add(new IncidentDuplicate
                {
                    MasterIncidentId = master.IncidentId,
                    DuplicateIncidentId = duplicate.IncidentId
                });

                // C. CHUYỂN ẢNH TỪ DUPLICATE SANG MASTER
                var dupImages = await _context.IncidentImages
                    .Where(img => img.IncidentId == dupId)
                    .ToListAsync();

                foreach (var img in dupImages)
                {
                    img.IncidentId = master.IncidentId; // Đổi chủ sở hữu ảnh
                }

                // D. [NEW] CỘNG ĐIỂM CHO USER BÁO CÁO TRÙNG
                if (duplicate.User != null)
                {
                    int rewardPoints = 5; // Cộng 5 điểm (ít hơn người gốc 1 chút)
                    duplicate.User.TrustScore += rewardPoints;

                    // Gửi thông báo
                    await _notificationRepo.CreateAsync(
                        duplicate.UserId,
                        $"Báo cáo \"{duplicate.Title}\" của bạn đã được gộp vào sự cố gốc #{master.IncidentId}. Bạn nhận được +{rewardPoints} điểm uy tín nhờ đóng góp chính xác."
                    );
                }
            }

            // 3. Cập nhật trạng thái Master
            master.Status = "Đang xử lý";

            _context.IncidentStatusHistories.Add(new IncidentStatusHistory
            {
                IncidentId = master.IncidentId,
                Status = "Đã gộp sự cố",
                Note = $"Đã gộp {dto.DuplicateIncidentIds.Count} báo cáo trùng lặp.",
                AdminId = adminId,
                UpdatedAt = DateTime.UtcNow
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
        .Skip((dto.PageIndex - 1) * dto.PageSize) 
        .Take(dto.PageSize)                   
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
        public async Task<int?> GetUserDepartmentIdAsync(int userId)
        {
            var user = await _context.Users
                .AsNoTracking()
                .FirstOrDefaultAsync(x => x.UserId == userId);

            return user?.DepartmentId;
        }



        private double CalculateDistance(double lat1, double lon1, double lat2, double lon2)
        {
            var R = 6371e3; // Bán kính trái đất (m)
            var rLat1 = lat1 * (Math.PI / 180);
            var rLat2 = lat2 * (Math.PI / 180);
            var dLat = (lat2 - lat1) * (Math.PI / 180);
            var dLon = (lon2 - lon1) * (Math.PI / 180);

            var a = Math.Sin(dLat / 2) * Math.Sin(dLat / 2) +
                    Math.Cos(rLat1) * Math.Cos(rLat2) *
                    Math.Sin(dLon / 2) * Math.Sin(dLon / 2);
            var c = 2 * Math.Atan2(Math.Sqrt(a), Math.Sqrt(1 - a));
            return R * c;
        }
    }
}
