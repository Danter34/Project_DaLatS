using Google.Cloud.Vision.V1;
using SafeDalat_API.Repositories.Interface;

namespace SafeDalat_API.Repositories.Services
{
    public class ImageAnalysisRepository : IImageAnalysisRepository
    {
        private readonly string _credentialPath = "safedalat-key.json";

        public ImageAnalysisRepository()
        {
            string path = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, _credentialPath);
            Environment.SetEnvironmentVariable("GOOGLE_APPLICATION_CREDENTIALS", path);
        }

        public async Task<bool> ValidateImageContentAsync(IFormFile imageFile)
        {
            try
            {
                var client = await ImageAnnotatorClient.CreateAsync();

                using var stream = new MemoryStream();
                await imageFile.CopyToAsync(stream);
                stream.Position = 0;

                var image = await Image.FromStreamAsync(stream);

                var safeSearch = await client.DetectSafeSearchAsync(image);

                if (safeSearch.Adult > Likelihood.Possible ||
                    safeSearch.Violence > Likelihood.Possible ||
                    safeSearch.Racy > Likelihood.Possible ||
                    safeSearch.Medical > Likelihood.Possible ||
                    safeSearch.Spoof > Likelihood.Possible) // Spoof: Chặn ảnh chế/photoshop lộ liễu
                {
                    Console.WriteLine($"[BLOCKED BY SAFESEARCH] Spoof: {safeSearch.Spoof}, Adult: {safeSearch.Adult}");
                    return false;
                }
                var labels = await client.DetectLabelsAsync(image);

                // DANH SÁCH TỪ KHÓA CẤM (Đã bổ sung mạnh tay hơn)
                var forbiddenLabels = new[]
                { 
                    // 1. Nhóm Hoạt hình/Vẽ (Cũ)
                    "cartoon", "illustration", "drawing", "sketch", "anime",
                    "clip art", "animated cartoon", "cg artwork", "fictional character", "comics", "poster",
                    
                    // 2. Nhóm Meme/Hài hước (Mới - Để chặn ảnh con chó)
                    "meme", "internet meme", "joke", "comedy", "fun", "humor", "photo caption",
                    "snout" /* Từ này hơi nguy hiểm vì chó thật cũng có, nhưng meme chó hay dính từ này */,
                    
                    // 3. Nhóm Game/Screenshot (Mới - Để chặn ảnh pixel art)
                    "screenshot", "video game", "software", "multimedia", "pixel art",
                    "game", "font", "text", "display device"
                };

                Console.WriteLine("--- AI ANALYZING LABELS ---");
                foreach (var label in labels)
                {
                    // In ra để Debug xem Google nhìn thấy cái gì
                    Console.WriteLine($"- Detect: {label.Description} (Score: {label.Score})");

                    // GIẢM SCORE XUỐNG 0.65 (65%) ĐỂ BẮT NHẠY HƠN
                    // Chuyển label về chữ thường để so sánh
                    if (label.Score > 0.65 && forbiddenLabels.Contains(label.Description.ToLower()))
                    {
                        Console.WriteLine($"[BLOCKED BY LABEL] Forbidden word found: {label.Description}");
                        return false;
                    }
                }
                Console.WriteLine("---------------------------");

                return true; // Ảnh SẠCH
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Google Vision Error: {ex.Message}");

                return true;
            }
        }
    }
}