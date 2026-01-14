using Microsoft.EntityFrameworkCore;
using SafeDalat_API.Data;
using SafeDalat_API.Model.Domain;
using SafeDalat_API.Model.DTO.IncidentCategory;
using SafeDalat_API.Repositories.Interface;

namespace SafeDalat_API.Repositories.Services
{
    public class IncidentCategoryRepository:IIncidentCategoryRepository
    {
        private readonly AppDbContext _context;

        public IncidentCategoryRepository(AppDbContext context)
        {
            _context = context;
        }

        public async Task<List<IncidentCategory>> GetAllAsync()
        {
            return await _context.IncidentCategories
                .AsNoTracking()
                .Select(x => new IncidentCategory
                {
                    CategoryId = x.CategoryId,
                    Name = x.Name
                })
                .ToListAsync();
        }

        public async Task<IncidentCategory?> GetByIdAsync(int id)
        {
            var category = await _context.IncidentCategories.FindAsync(id);
            if (category == null) return null;

            return new IncidentCategory
            {
                CategoryId = category.CategoryId,
                Name = category.Name
            };
        }

        public async Task<IncidentCategory> CreateAsync(CreateIncidentCategoryDTO dto)
        {
            var category = new IncidentCategory
            {
                Name = dto.Name
            };

            _context.IncidentCategories.Add(category);
            await _context.SaveChangesAsync();

            return new IncidentCategory
            {
                CategoryId = category.CategoryId,
                Name = category.Name
            };
        }

        public async Task<IncidentCategory?> UpdateAsync(int id, CreateIncidentCategoryDTO dto)
        {
            var category = await _context.IncidentCategories.FindAsync(id);
            if (category == null) return null;

            category.Name = dto.Name;
            await _context.SaveChangesAsync();

            return new IncidentCategory
            {
                CategoryId = category.CategoryId,
                Name = category.Name
            };
        }

        public async Task<bool> DeleteAsync(int id)
        {
            var category = await _context.IncidentCategories.FindAsync(id);
            if (category == null) return false;

            _context.IncidentCategories.Remove(category);
            await _context.SaveChangesAsync();
            return true;
        }
    }
}
