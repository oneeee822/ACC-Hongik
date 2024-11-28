import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import java.io.IOException;

public class ServerCheck {

    public static void main(String[] args) throws IOException {
        // 환경 변수에서 URL 가져오기
        String serverUrl = System.getenv("SERVER_URL"); // 체크할 서버 URL
        String webhookUrl = System.getenv("SLACK_WEBHOOK_URL"); // Slack Webhook URL

        if (serverUrl == null || webhookUrl == null) {
            throw new IllegalArgumentException("SERVER_URL or SLACK_WEBHOOK_URL is not set.");
        }

        // 서버 상태 확인
        String statusMessage = checkServerStatus(serverUrl);

        // Slack 알림 보내기
        sendSlackNotification(webhookUrl, statusMessage);
    }

    private static String checkServerStatus(String url) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    return "Server is up and running!";
                } else {
                    return "Server returned status: " + statusCode;
                }
            }
        } catch (IOException e) {
            return "Error checking server: " + e.getMessage();
        }
    }

    private static void sendSlackNotification(String webhookUrl, String message) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(webhookUrl);
            post.setHeader("Content-type", "application/json");
            String payload = String.format("{\"text\":\"%s\"}", message);
            post.setEntity(new StringEntity(payload));

            try (CloseableHttpResponse response = httpClient.execute(post)) {
                System.out.println("Slack notification sent, response code: " +
                        response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            System.err.println("Error sending Slack notification: " + e.getMessage());
        }
    }
}
