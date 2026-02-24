package Services;

import Models.FoodAnalysisReport;
import Models.FoodListing;
import Utils.Config;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class FoodAnalysisService {

    private static final String GEMINI_MODEL = "gemini-1.5-flash";

    public FoodAnalysisReport analyzeFoodImage(File imageFile, FoodListing listing) {
    if (imageFile == null || !imageFile.exists()) return null;

    try {
        String rawResponse = callGeminiAPI(imageFile);
        
        // 1. Parse the full API response wrapper first
        JSONObject fullJson = new JSONObject(rawResponse);
        
        if (fullJson.has("error")) {
            throw new Exception("API Error: " + fullJson.getJSONObject("error").getString("message"));
        }

        // 2. Extract the actual AI text from the Gemini hierarchy
        String aiText = fullJson.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text");

        // 3. Clean the markdown code blocks (```json ... ```)
        String cleanJson = aiText.replaceAll("(?s)```json\\s*|\\s*```", "").trim();
        
        // 4. Parse the inner JSON containing food details
        JSONObject data = new JSONObject(cleanJson);

        // 5. Extract Data Safely with defaults
        String ingredients = data.optString("ingredients", "Not detected");
        String condition = data.optString("condition", "Unknown");
        double freshness = data.optDouble("freshnessScore", 0.5);
        boolean readyToEat = data.optBoolean("isReadyToEat", true);
        String recommendation = data.optString("recommendation", "No specific recommendation.");

        // 6. Update Model
        listing.setIngredients(ingredients);
        listing.setExpiryTime(calculateExpiry(freshness, readyToEat));
        listing.setStatus(condition);

        List<String> allergens = detectAllergens(ingredients);
        
        boolean isVeg = !ingredients.toLowerCase().matches(".*(chicken|beef|pork|fish|meat|mutton|shrimp).*");
        boolean isHalal = !ingredients.toLowerCase().matches(".*(pork|lard|alcohol|wine|beer).*");

        return new FoodAnalysisReport(
                listing, allergens, isVeg, isHalal, freshness, recommendation
        );

    } catch (Exception e) {
        System.err.println("Gemini Analysis Failure: " + e.getMessage());
        return null;
    }
}

    private String callGeminiAPI(File imageFile) throws Exception {
    byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
    String base64Image = Base64.getEncoder().encodeToString(imageBytes);

    // Use a JSONObject to build the request instead of manual String formatting.
    // This automatically handles escaping quotes and special characters.
    JSONObject inlineData = new JSONObject()
            .put("mime_type", "image/jpeg")
            .put("data", base64Image);

    // Explicit prompt to ensure high-class JSON data
    String prompt = "Act as a food safety expert. Analyze this food image. " +
                    "Provide a detailed JSON response only. Required keys: " +
                    "\"condition\", \"ingredients\", \"freshnessScore\" (0.0-1.0), " +
                    "\"isReadyToEat\" (boolean), \"recommendation\".";

    JSONObject textPart = new JSONObject().put("text", prompt);
    JSONObject imagePart = new JSONObject().put("inline_data", inlineData);

    JSONArray parts = new JSONArray().put(textPart).put(imagePart);
    JSONObject content = new JSONObject().put("parts", parts);
    JSONArray contents = new JSONArray().put(content);
    JSONObject root = new JSONObject().put("contents", contents);

    String endpoint =
        "https://generativelanguage.googleapis.com/v1beta/models/"
        + GEMINI_MODEL
        + ":generateContent?key="
        + Config.GEMINI_API_KEY;

    HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(10))
                    .build();

    HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(root.toString())) // Use root.toString()
                    .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

System.out.println("HTTP Status: " + response.statusCode());
System.out.println("RAW GEMINI RESPONSE:");
System.out.println(response.body());

return response.body();
}

    private LocalDateTime calculateExpiry(double freshness, boolean ready) {
        LocalDateTime now = LocalDateTime.now();
        // Logistics-focused expiry calculation
        return ready ? now.plusHours((long)(48 * freshness)) : now.plusDays((long)(10 * freshness));
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