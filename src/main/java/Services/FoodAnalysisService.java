package Services;

import Models.FoodAnalysisReport;
import Models.FoodListing;
import org.json.JSONArray;
import org.json.JSONObject;
import Utils.Api;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FoodAnalysisService {

    String apiKey = Api.getApiKey();
    private static final String MODEL = "gemini-2.5-flash";     // supports image input

    public FoodAnalysisReport analyzeFoodImage(File imageFile, FoodListing listing) {
        if (imageFile == null || !imageFile.exists()) return null;

        try {
            // Step 1: Encode image as Base64
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Step 2: Build request JSON for Gemini
            JSONObject inlineData = new JSONObject()
                    .put("mime_type", "image/jpeg")
                    .put("data", base64Image);

            JSONObject textPart = new JSONObject()
                    .put("text", "You are a food safety expert. Analyze this food image and return a detailed JSON ONLY. " +
                            "Required keys: ingredients, condition, freshnessScore (0.0-1.0), " +
                            "isReadyToEat (boolean), recommendation.");

            JSONObject imagePart = new JSONObject().put("inline_data", inlineData);

            JSONArray parts = new JSONArray().put(textPart).put(imagePart);
            JSONObject content = new JSONObject().put("parts", parts);
            JSONArray contents = new JSONArray().put(content);
            JSONObject root = new JSONObject().put("contents", contents);

            // Step 3: Correct v1beta endpoint for Gemini
            String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
        + MODEL + ":generateContent?key=" + apiKey;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(root.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("HTTP Status: " + response.statusCode());
            System.out.println("RAW AI RESPONSE: " + response.body());

            if (response.statusCode() != 200) {
                throw new Exception("AI API returned HTTP " + response.statusCode());
            }

            // Step 4: Parse AI response
            JSONObject fullJson = new JSONObject(response.body());

            if (fullJson.has("error")) {
                throw new Exception("AI Error: " + fullJson.getJSONObject("error").getString("message"));
            }

            JSONArray candidates = fullJson.optJSONArray("candidates");
            if (candidates == null || candidates.length() == 0) {
                throw new Exception("No candidates returned by AI");
            }

            String aiText = candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

            String cleanJson = aiText.replaceAll("(?s)```json\\s*|\\s*```", "").trim();
            JSONObject data = new JSONObject(cleanJson);

            // Step 5: Extract values
            String ingredients = data.optString("ingredients", "Not detected");
            String condition = data.optString("condition", "Unknown");
            double freshness = data.optDouble("freshnessScore", 0.5);
            boolean readyToEat = data.optBoolean("isReadyToEat", true);
            String recommendation = data.optString("recommendation", "No specific recommendation.");

            // Step 6: Update listing
            listing.setIngredients(ingredients);
            listing.setExpiryTime(calculateExpiry(freshness, readyToEat));
            listing.setStatus(condition);

            List<String> allergens = detectAllergens(ingredients);
            boolean isVeg = !ingredients.toLowerCase().matches(".*(chicken|beef|pork|fish|meat|mutton|shrimp).*");
            boolean isHalal = !ingredients.toLowerCase().matches(".*(pork|lard|alcohol|wine|beer).*");

            return new FoodAnalysisReport(listing, allergens, isVeg, isHalal, freshness, recommendation);

        } catch (Exception e) {
            System.err.println("Food Analysis Failure: " + e.getMessage());
            return null;
        }
    }

    // ------------------- Helper Methods -------------------

    private LocalDateTime calculateExpiry(double freshness, boolean ready) {
        LocalDateTime now = LocalDateTime.now();
        return ready ? now.plusHours((long) (48 * freshness)) : now.plusDays((long) (10 * freshness));
    }

    private List<String> detectAllergens(String ingredients) {
        List<String> found = new ArrayList<>();
        String[] common = {"peanuts", "nuts", "milk", "dairy", "eggs", "wheat", "gluten", "soy", "shellfish"};
        String input = ingredients.toLowerCase();
        for (String a : common) {
            if (input.contains(a)) found.add(a);
        }
        return found;
    }
}