using FirebaseAdmin.Messaging;
using SafeDalat_API.Repositories.Interface;

namespace SafeDalat_API.Repositories.Services
{
    public class FcmService : IFcmService
    {

        public async Task SendToTokenAsync(string token, string title, string body)
        {
            if (string.IsNullOrEmpty(token)) return;

            var message = new Message()
            {
                Token = token,
                Notification = new Notification()
                {
                    Title = title,
                    Body = body
                },
                Data = new Dictionary<string, string>()
                {
                    { "click_action", "FLUTTER_NOTIFICATION_CLICK" },
                    { "type", "system" }
                }
            };

            try
            {
                await FirebaseMessaging.DefaultInstance.SendAsync(message);
            }
            catch (Exception ex)
            {
  
                Console.WriteLine("Lỗi gửi FCM: " + ex.Message);
            }
        }

        public async Task SendToMultipleTokensAsync(
     List<string> tokens,
     string title,
     string body)
        {
            if (tokens == null || tokens.Count == 0) return;

            var message = new MulticastMessage()
            {
                Tokens = tokens,
                Notification = new Notification
                {
                    Title = title,
                    Body = body
                },
                Data = new Dictionary<string, string>
        {
            { "type", "broadcast" }
        }
            };

            try
            {
                var response = await FirebaseMessaging.DefaultInstance
    .SendEachForMulticastAsync(message);

                Console.WriteLine($"Broadcast OK: {response.SuccessCount}");
                Console.WriteLine($"Broadcast Fail: {response.FailureCount}");

                for (int i = 0; i < response.Responses.Count; i++)
                {
                    if (!response.Responses[i].IsSuccess)
                    {
                        Console.WriteLine(
                            $"Token lỗi [{tokens[i]}]: {response.Responses[i].Exception.Message}"
                        );
                    }
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine("FCM Broadcast Exception: " + ex.Message);
            }
        }
    }
}