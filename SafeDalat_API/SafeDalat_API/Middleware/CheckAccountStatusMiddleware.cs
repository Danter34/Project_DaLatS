using Microsoft.AspNetCore.Authorization;
using SafeDalat_API.Data;
using System.Security.Claims;

namespace SafeDalat_API.Middleware
{
    public class CheckAccountStatusMiddleware
    {
        private readonly RequestDelegate _next;

        public CheckAccountStatusMiddleware(RequestDelegate next)
        {
            _next = next;
        }

        public async Task Invoke(HttpContext context, IServiceScopeFactory scopeFactory)
        {
            // Chỉ kiểm tra nếu User đang đăng nhập (có Token hợp lệ)
            if (context.User.Identity != null && context.User.Identity.IsAuthenticated)
            {
                // Lấy UserId từ Token
                var userIdClaim = context.User.FindFirst("UserId")?.Value
                               ?? context.User.FindFirst(ClaimTypes.NameIdentifier)?.Value;

                if (int.TryParse(userIdClaim, out int userId))
                {
                    // Tạo scope mới để gọi DbContext
                    using (var scope = scopeFactory.CreateScope())
                    {
                        var dbContext = scope.ServiceProvider.GetRequiredService<AppDbContext>();

                        // Tìm User trong DB
                        var user = await dbContext.Users.FindAsync(userId);

                        // Nếu User bị khóa -> Chặn luôn, trả về 401
                        if (user != null && user.IsLocked)
                        {
                            context.Response.StatusCode = StatusCodes.Status401Unauthorized;
                            await context.Response.WriteAsync("Account is locked"); // Thông báo lỗi
                            return; // Dừng request tại đây
                        }
                    }
                }
            }

            // Nếu không bị khóa thì cho đi tiếp
            await _next(context);
        }
    }
}