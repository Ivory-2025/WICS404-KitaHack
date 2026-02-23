package Services;

import Models.FoodAnalysisReport;
import Models.FoodListing;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * Service responsible for analyzing food images using the Gemini API,
 * generating reports, calculating expiry times, and detecting allergens.
 * Built using native Java; no external JSON libraries required.
 */
public class FoodAnalysisService {

    /**
     * 1. Call Gemini API
     * Sends the image of the surplus food to Gemini Vision API for analysis.
     * * @param imageBytes The raw bytes of the JPG/PNG image.
     * @return The raw JSON response string from Google's servers.
     */
    public String callGeminiApi(byte[] imageBytes) {
        String apiKey = System.getenv("GEMINI_API_KEY"); 
        
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("API Key is missing! Set the GEMINI_API_KEY environment variable.");
            return "{ \"error\": \"API key missing\" }";
        }

        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        String prompt = "Analyze this food image. Return ONLY a valid JSON object with the following exact keys: " +
                        "\\\"condition\\\" (String: e.g., Good, Bruised), \\\"ingredients\\\" (String: comma-separated list), " +
                        "\\\"freshnessScore\\\" (Number between 0.0 and 1.0), \\\"isReadyToEat\\\" (boolean), " +
                        "\\\"recommendation\\\" (String: a clever recipe idea or a flash sale pitch). Do not use markdown blocks.";

        String requestBody = "{\n" +
            "  \"contents\": [{\n" +
            "    \"parts\": [\n" +
            "      {\"text\": \"" + prompt + "\"},\n" +
            "      {\n" +
            "        \"inline_data\": {\n" +
            "          \"mime_type\": \"image/jpeg\",\n" + 
            "          \"data\": \"" + base64Image + "\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]\n" +
            "  }]\n" +
            "}";

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();

        } catch (Exception e) {
            e.printStackTrace();
            return "{ \"error\": \"Failed to connect to the AI\" }";
        }
    }

    /**
     * 2. Generate FoodAnalysisReport (NO EXTERNAL LIBRARIES)
     * Parses the Gemini API response using native Java String manipulation.
     * * @param geminiJsonResponse The raw JSON string from the AI.
     * @param listing The initial FoodListing to be updated.
     * @return A fully populated FoodAnalysisReport.
     */
    public FoodAnalysisReport generateFoodAnalysisReport(String geminiJsonResponse, FoodListing listing) {
        FoodAnalysisReport report = new FoodAnalysisReport();

        try {
            // Extract the raw text block from Gemini's complex response wrapper
            String extractedText = "";
            int textIndex = geminiJsonResponse.indexOf("\"text\": \"");
            
            if (textIndex != -1) {
                int startIndex = textIndex + 9; 
                int endIndex = geminiJsonResponse.indexOf("\"}", startIndex); 
                
                // Handle case where closing brace might be formatted differently
                if (endIndex == -1) {
                    endIndex = geminiJsonResponse.lastIndexOf("\"");
                }
                
                extractedText = geminiJsonResponse.substring(startIndex, endIndex);
                extractedText = extractedText.replace("\\n", "").replace("\\\"", "\"").replace("\\\\", "\\");
            } else {
                extractedText = geminiJsonResponse; 
            }

            // Extract specific fields using basic String searching
            String extractedIngredients = extractStringValue(extractedText, "ingredients");
            String recommendation = extractStringValue(extractedText, "recommendation");
            
            boolean isReadyToEat = extractTextValue(extractedText, "isReadyToEat").contains("true");
            
            double extractedFreshness = 0.5; // Default fallback value
            String freshnessString = extractTextValue(extractedText, "freshnessScore");
            if (!freshnessString.isEmpty()) {
                try {
                    extractedFreshness = Double.parseDouble(freshnessString);
                } catch (NumberFormatException ignored) {}
            }

            // Update the FoodListing with AI-detected data
            listing.setIngredients(extractedIngredients);
            listing.setExpiryTime(calculateExpiryTime(extractedFreshness, isReadyToEat));
            listing.setStatus("Active"); 
            
            // Populate the Analysis Report
            report.setReportId((int) (Math.random() * 10000));
            report.setListing(listing);
            report.setFreshnessScore(extractedFreshness);
            
            boolean containsMeat = extractedIngredients.toLowerCase().matches(".*(chicken|beef|pork|fish|meat).*");
            report.setVegetarian(!containsMeat); 
            report.setHalalFriendly(!extractedIngredients.toLowerCase().contains("pork")); 
            report.setRecommendation(recommendation);
            report.setAllergens(detectAllergens(extractedIngredients));

        } catch (Exception e) {
            System.err.println("Error parsing AI response manually: " + e.getMessage());
            e.printStackTrace();
        }
        
        return report;
    }

    /**
     * Helper Method: Extracts a String value (surrounded by quotes) from a JSON string.
     */
    private String extractStringValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return ""; 
        
        int valueStart = json.indexOf("\"", keyIndex + searchKey.length()) + 1;
        int valueEnd = json.indexOf("\"", valueStart);
        
        if (valueStart > 0 && valueEnd > valueStart) {
            return json.substring(valueStart, valueEnd);
        }
        return "";
    }

    /**
     * Helper Method: Extracts a raw text value (boolean or number) from a JSON string.
     */
    private String extractTextValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return ""; 
        
        int valueStart = keyIndex + searchKey.length();
        while (valueStart < json.length() && (json.charAt(valueStart) == ' ' || json.charAt(valueStart) == '\"')) {
            valueStart++;
        }
        
        int commaEnd = json.indexOf(",", valueStart);
        int braceEnd = json.indexOf("}", valueStart);
        
        int valueEnd = -1;
        if (commaEnd != -1 && braceEnd != -1) valueEnd = Math.min(commaEnd, braceEnd);
        else if (commaEnd != -1) valueEnd = commaEnd;
        else if (braceEnd != -1) valueEnd = braceEnd;
        else valueEnd = json.length();
        
        String result = json.substring(valueStart, valueEnd).trim();
        // Strip trailing quotes if they accidentally got included
        if (result.endsWith("\"")) result = result.substring(0, result.length() - 1);
        
        return result;
    }

    /**
     * 3. Calculate expiry time
     */
    public LocalDateTime calculateExpiryTime(double freshnessScore, boolean isReadyToEat) {
        LocalDateTime now = LocalDateTime.now();
        
        if (isReadyToEat) {
            long hoursUntilExpiry = (long) (24 * freshnessScore); 
            return now.plusHours(Math.max(1, hoursUntilExpiry)); 
        } else {
            long daysUntilExpiry = (long) (7 * freshnessScore);
            return now.plusDays(Math.max(1, daysUntilExpiry)); 
        }
    }

    /**
     * 4. Detect allergens from ingredient string
     */
    public List<String> detectAllergens(String ingredientsString) {
        List<String> foundAllergens = new ArrayList<>();
        
        if (ingredientsString == null || ingredientsString.isEmpty()) {
            return foundAllergens;
        }

        String lowerCaseIngredients = ingredientsString.toLowerCase();
        List<String> commonAllergens = Arrays.asList(
            "peanuts", "tree nuts", "milk", "eggs", "wheat", "soy", "fish", "shellfish", "sesame"
        );

        for (String allergen : commonAllergens) {
            if (lowerCaseIngredients.contains(allergen)) {
                foundAllergens.add(allergen);
            }
        }

        return foundAllergens;
    }
}
