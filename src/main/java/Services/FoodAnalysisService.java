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
            
            // 1. Enhanced defensive parsing
            JSONObject fullJson = new JSONObject(rawResponse);
            
            // Check if API returned an error field
            if (fullJson.has("error")) {
                throw new Exception("API Error: " + fullJson.getJSONObject("error").getString("message"));
            }

            String aiText = fullJson.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

            // 2. Clean AI text: Remove markdown and extra whitespace
            String cleanJson = aiText.replaceAll("(?s)```json\\s*|\\s*```", "").trim();
            JSONObject data = new JSONObject(cleanJson);

            // 3. Extract Data Safely with defaults
            String ingredients = data.optString("ingredients", "Not detected");
            String condition = data.optString("condition", "Unknown");
            double freshness = data.optDouble("freshnessScore", 0.5);
            boolean readyToEat = data.optBoolean("isReadyToEat", true);
            String recommendation = data.optString("recommendation", "No specific recommendation.");

            // 4. Update Model
            listing.setIngredients(ingredients);
            listing.setExpiryTime(calculateExpiry(freshness, readyToEat));
            listing.setStatus(condition);

            List<String> allergens = detectAllergens(ingredients);
            
            // Refined Gen Z dietary logic
            boolean isVeg = !ingredients.toLowerCase().matches(".*(chicken|beef|pork|fish|meat|mutton|shrimp).*");
            boolean isHalal = !ingredients.toLowerCase().matches(".*(pork|lard|alcohol|wine|beer).*");

            return new FoodAnalysisReport(
                    listing,
                    allergens,
                    isVeg,
                    isHalal,
                    freshness,
                    recommendation
            );

        } catch (Exception e) {
            // Log exactly what went wrong for debugging
            System.err.println("Gemini Analysis Failure: " + e.getMessage());
            return null;
        }
    }

    private String callGeminiAPI(File imageFile) throws Exception {
        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // Explicit prompt to ensure high-class JSON data
        String prompt = "Act as a food safety expert. Analyze this food image. " +
                        "Provide a detailed JSON response only. Required keys: " +
                        "\"condition\", \"ingredients\", \"freshnessScore\" (0.0-1.0), " +
                        "\"isReadyToEat\" (boolean), \"recommendation\".";

        String requestBody = String.format(
            "{ \"contents\": [{ \"parts\": [" +
            "{ \"text\": \"%s\" }," +
            "{ \"inline_data\": { \"mime_type\": \"image/jpeg\", \"data\": \"%s\" } }" +
            "] }] }", prompt, base64Image);

        String endpoint = "[https://generativelanguage.googleapis.com/v1beta/models/](https://generativelanguage.googleapis.com/v1beta/models/)" 
                + GEMINI_MODEL + ":generateContent?key=" + Config.GEMINI_API_KEY;

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
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