using Microsoft.EntityFrameworkCore;
using SafeDalat_API.Data;
using SafeDalat_API.Model.Domain;
using SafeDalat_API.Model.DTO.Department;
using SafeDalat_API.Repositories.Interface;

namespace SafeDalat_API.Repositories.Services
{
    public class DepartmentRepository : IDepartmentRepository
    {
        private readonly AppDbContext _context;

        public DepartmentRepository(AppDbContext context)
        {
            _context = context;
        }

        public async Task<List<DepartmentResponseDTO>> GetAllAsync()
        {
            return await _context.Departments
                .AsNoTracking()
                .Include(d => d.Staffs) // Include để đếm nhân viên
                .Select(d => new DepartmentResponseDTO
                {
                    DepartmentId = d.DepartmentId,
                    Name = d.Name,
                    Description = d.Description,
                    PhoneNumber = d.PhoneNumber,
                    StaffCount = d.Staffs.Count
                })
                .ToListAsync();
        }

        public async Task<DepartmentResponseDTO?> GetByIdAsync(int id)
        {
            var d = await _context.Departments
                .Include(d => d.Staffs)
                .FirstOrDefaultAsync(x => x.DepartmentId == id);

            if (d == null) return null;

            return new DepartmentResponseDTO
            {
                DepartmentId = d.DepartmentId,
                Name = d.Name,
                Description = d.Description,
                PhoneNumber = d.PhoneNumber,
                StaffCount = d.Staffs.Count
            };
        }

        public async Task<DepartmentResponseDTO> CreateAsync(CreateDepartmentDTO dto)
        {
            var department = new Department
            {
                Name = dto.Name,
                Description = dto.Description,
                PhoneNumber = dto.PhoneNumber
            };

            _context.Departments.Add(department);
            await _context.SaveChangesAsync();

            return new DepartmentResponseDTO
            {
                DepartmentId = department.DepartmentId,
                Name = department.Name,
                Description = department.Description,
                PhoneNumber = department.PhoneNumber,
                StaffCount = 0
            };
        }

        public async Task<DepartmentResponseDTO?> UpdateAsync(int id, UpdateDepartmentDTO dto)
        {
            var department = await _context.Departments.FindAsync(id);
            if (department == null) return null;

            department.Name = dto.Name;
            department.Description = dto.Description;
            department.PhoneNumber = dto.PhoneNumber;

            await _context.SaveChangesAsync();

            // Load lại staff count để trả về chuẩn
            var staffCount = await _context.Users.CountAsync(u => u.DepartmentId == id);

            return new DepartmentResponseDTO
            {
                DepartmentId = department.DepartmentId,
                Name = department.Name,
                Description = department.Description,
                PhoneNumber = department.PhoneNumber,
                StaffCount = staffCount
            };
        }

        public async Task<bool> DeleteAsync(int id)
        {
            var department = await _context.Departments.FindAsync(id);
            if (department == null) return false;

            _context.Departments.Remove(department);
            await _context.SaveChangesAsync();
            return true;
        }
    }
}