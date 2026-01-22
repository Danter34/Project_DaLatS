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
                    safeSearch.Spoof > Likelihood.Possible) 
                {
                    Console.WriteLine($"[BLOCKED BY SAFESEARCH] Spoof: {safeSearch.Spoof}, Adult: {safeSearch.Adult}");
                    return false;
                }
                var labels = await client.DetectLabelsAsync(image);

                
                var forbiddenLabels = new[]
                { 
                    
                    "cartoon", "illustration", "drawing", "sketch", "anime",
                    "clip art", "animated cartoon", "cg artwork", "fictional character", "comics", "poster",
                    
                   
                    "meme", "internet meme", "joke", "comedy", "fun", "humor", "photo caption",
                    "snout" ,
                    
                  
                    "screenshot", "video game", "software", "multimedia", "pixel art",
                    "game", "font", "text", "display device"
                };

                Console.WriteLine("--- AI ANALYZING LABELS ---");
                foreach (var label in labels)
                {
                    
                    Console.WriteLine($"- Detect: {label.Description} (Score: {label.Score})");

                   
                    if (label.Score > 0.65 && forbiddenLabels.Contains(label.Description.ToLower()))
                    {
                        Console.WriteLine($"[BLOCKED BY LABEL] Forbidden word found: {label.Description}");
                        return false;
                    }
                }
                Console.WriteLine("---------------------------");

                return true; 
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Google Vision Error: {ex.Message}");

                return true;
            }
        }
    }
}