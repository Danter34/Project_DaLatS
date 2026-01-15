using Microsoft.EntityFrameworkCore;
using SafeDalat_API.Model.Domain;

namespace SafeDalat_API.Data
{
    public class AppDbContext : DbContext
    {
        public AppDbContext(DbContextOptions<AppDbContext> options) : base(options)
        {
        }

        public DbSet<User> Users { get; set; }
        public DbSet<Incident> Incidents { get; set; }
        public DbSet<IncidentCategory> IncidentCategories { get; set; }
        public DbSet<IncidentImage> IncidentImages { get; set; }
        public DbSet<IncidentDuplicate> IncidentDuplicates { get; set; }
        public DbSet<IncidentStatusHistory> IncidentStatusHistories { get; set; }
        public DbSet<Question> Questions { get; set; }
        public DbSet<Answer> Answers { get; set; }
        public DbSet<Notification> Notifications { get; set; }
        public DbSet<IncidentComment> IncidentComments { get; set; }

        // --- CÁC BẢNG MỚI THÊM ---
        public DbSet<Department> Departments { get; set; }
        public DbSet<QuestionCategory> QuestionCategories { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            // --- USER ---
            modelBuilder.Entity<User>()
                .HasIndex(x => x.Email)
                .IsUnique();

            // Cấu hình: 1 Phòng ban có nhiều Nhân viên (User)
            modelBuilder.Entity<User>()
                .HasOne(x => x.Department)
                .WithMany(d => d.Staffs)
                .HasForeignKey(x => x.DepartmentId)
                .OnDelete(DeleteBehavior.SetNull); // Xóa phòng ban, user trở về tự do (không bị xóa)


            // --- DEPARTMENT ---
            // (Không cần config gì đặc biệt nếu dùng quy ước chuẩn, nhưng đã config trong User/Incident/Question rồi)


            // --- INCIDENT ---
            modelBuilder.Entity<Incident>()
                .Property(x => x.AlertLevel)
                .HasConversion<int>();

            modelBuilder.Entity<Incident>()
                .HasOne(x => x.User)
                .WithMany(x => x.Incidents)
                .HasForeignKey(x => x.UserId)
                .OnDelete(DeleteBehavior.Restrict);

            modelBuilder.Entity<Incident>()
                .HasOne(x => x.Category)
                .WithMany()
                .HasForeignKey(x => x.CategoryId);

            // Cấu hình: 1 Phòng ban chịu trách nhiệm nhiều Sự cố (Điều phối)
            modelBuilder.Entity<Incident>()
                .HasOne(x => x.AssignedDepartment)
                .WithMany(d => d.AssignedIncidents)
                .HasForeignKey(x => x.AssignedDepartmentId)
                .OnDelete(DeleteBehavior.SetNull); // Xóa phòng ban, sự cố vẫn còn nhưng chưa ai nhận

            // --- INCIDENT IMAGE ---
            modelBuilder.Entity<IncidentImage>()
                .HasOne(x => x.Incident)
                .WithMany(x => x.Images)
                .HasForeignKey(x => x.IncidentId)
                .OnDelete(DeleteBehavior.Cascade);

            // --- INCIDENT DUPLICATE ---
            modelBuilder.Entity<IncidentDuplicate>()
                .HasOne(x => x.MasterIncident)
                .WithMany(x => x.AsMasterDuplicates)
                .HasForeignKey(x => x.MasterIncidentId)
                .OnDelete(DeleteBehavior.Restrict);

            modelBuilder.Entity<IncidentDuplicate>()
                .HasOne(x => x.DuplicateIncident)
                .WithMany(x => x.AsDuplicate)
                .HasForeignKey(x => x.DuplicateIncidentId)
                .OnDelete(DeleteBehavior.Restrict);

            modelBuilder.Entity<IncidentDuplicate>()
                .HasIndex(x => new { x.MasterIncidentId, x.DuplicateIncidentId })
                .IsUnique();

            // --- INCIDENT STATUS HISTORY ---
            modelBuilder.Entity<IncidentStatusHistory>()
                .HasOne(x => x.Incident)
                .WithMany()
                .HasForeignKey(x => x.IncidentId)
                .OnDelete(DeleteBehavior.Cascade);

            modelBuilder.Entity<IncidentStatusHistory>()
                .HasOne(x => x.Admin)
                .WithMany(x => x.IncidentStatusHistories)
                .HasForeignKey(x => x.AdminId)
                .OnDelete(DeleteBehavior.Restrict);

            // --- QUESTION ---
            modelBuilder.Entity<Question>()
                .HasOne(x => x.User)
                .WithMany()
                .HasForeignKey(x => x.UserId);

            // Cấu hình: 1 Câu hỏi thuộc 1 Chủ đề
            modelBuilder.Entity<Question>()
                .HasOne(x => x.QuestionCategory)
                .WithMany() // (Nếu bên QuestionCategory chưa có collection Questions thì để trống)
                .HasForeignKey(x => x.QuestionCategoryId)
                .OnDelete(DeleteBehavior.Restrict);

            // Cấu hình: 1 Câu hỏi được gán cho 1 Phòng ban
            modelBuilder.Entity<Question>()
                .HasOne(x => x.AssignedDepartment)
                .WithMany(d => d.AssignedQuestions)
                .HasForeignKey(x => x.AssignedDepartmentId)
                .OnDelete(DeleteBehavior.SetNull);

            // --- ANSWER ---
            modelBuilder.Entity<Answer>()
                .HasOne(x => x.Question)
                .WithMany(x => x.Answers)
                .HasForeignKey(x => x.QuestionId)
                .OnDelete(DeleteBehavior.Cascade);

            // Cấu hình Responder (Người trả lời)
            modelBuilder.Entity<Answer>()
                .HasOne(x => x.Responder)
                .WithMany()
                .HasForeignKey(x => x.ResponderId)
                .OnDelete(DeleteBehavior.Restrict);

            // --- NOTIFICATION ---
            modelBuilder.Entity<Notification>()
                .HasOne(x => x.User)
                .WithMany()
                .HasForeignKey(x => x.UserId);

            // --- INCIDENT COMMENT ---
            modelBuilder.Entity<IncidentComment>()
                .HasOne(x => x.Incident)
                .WithMany()
                .HasForeignKey(x => x.IncidentId)
                .OnDelete(DeleteBehavior.Cascade);

            modelBuilder.Entity<IncidentComment>()
                .HasOne(x => x.User)
                .WithMany()
                .HasForeignKey(x => x.UserId)
                .OnDelete(DeleteBehavior.Restrict);
        }
    }
}