package Services;

import Models.SurplusRecord;
import Models.Vendor;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import Utils.Config; 

public class AnalyticsService {
    private List<SurplusRecord> history = new ArrayList<>();

    // 1. Store daily surplus
    public void recordDailySurplus(SurplusRecord record) {
        history.add(record);
    }

    // 2. Calculate 7-day average as the data foundation for AI analysis
    public double getSevenDayAverage(Vendor vendor) {
        return history.stream()
            .filter(r -> r.getVendor().equals(vendor))
            .limit(7)
            .mapToInt(SurplusRecord::getSurplusCount)
            .average()
            .orElse(0.0);
    }

    // 3. Generate weekly insights combining manual logic and AI suggestions
    public String getWeeklyInsight(Vendor vendor) {
        double avg = getSevenDayAverage(vendor);
        String manualInsight;
        
        if (avg > 5.0) {
            manualInsight = "Action Required: High surplus detected. Average: " + Math.round(avg) + " units.";
        } else {
            manualInsight = "Sustainability Goal Met: Low food waste this week!";
        }

        try {
            // Call Gemini AI for professional inventory strategy
            String aiSuggestion = getAIStockSuggestion(vendor, avg);
            return manualInsight + "\n" + aiSuggestion;
        } catch (Exception e) {
            return manualInsight + "\nAI Suggestion: Service temporarily unavailable.";
        }
    }
    // 4. Call Google AI Studio (Gemini) for smart stock reduction suggestions
    public String getAIStockSuggestion(Vendor vendor, double avgSurplus) throws Exception {
        // Fetch API Key from Config file for security
        String apiKey = Config.GEMINI_API_KEY; 
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + apiKey;

        String prompt = "As a food waste expert, a vendor named " + vendor.getName() + 
                        " has a 7-day average surplus of " + avgSurplus + " items. " +
                        "Suggest a stock reduction strategy for next week in 1 sentence.";

        // Build the request body following Google AI Studio API format
        String jsonBody = "{\"contents\": [{\"parts\": [{\"text\": \"" + prompt + "\"}]}]}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            // Returns raw JSON. In the final phase, consider parsing the JSON for plain text output.
            return "AI Insight: " + response.body(); 
        } else {
            return "AI Suggestion: Consider reducing stock by 15% to align with SDG 12 goals.";
        }
    }
}
