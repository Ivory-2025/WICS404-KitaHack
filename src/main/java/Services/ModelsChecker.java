package Services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import Utils.Config;

public class ModelsChecker {

    /**
     * Calls the Google Generative Language API to list all available models
     * for your API key and prints the JSON response.
     */
    public static void listAvailableModels() {
        String apiKey = Config.GEMINI_API_KEY;

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey))
                    .GET()
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Print the raw JSON response
            System.out.println("ListModels response:");
            System.out.println(response.body());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error calling ListModels API.");
        }
    }

    public static void main(String[] args) {
        listAvailableModels();
    }
}