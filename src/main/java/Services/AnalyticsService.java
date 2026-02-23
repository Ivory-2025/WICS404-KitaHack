package Services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import Utils.Config; // Ensure Utils folder has "package Utils;"
import Models.SurplusRecord;
import Models.Vendor;
import DAO.SurplusRecordDAO;

public class AnalyticsService {
    
    // Use the DAO for database persistence instead of a local list
    private final SurplusRecordDAO surplusDAO = new SurplusRecordDAO();

    public void recordDailySurplus(SurplusRecord record) {
        surplusDAO.save(record);
    }

    public double getSevenDayAverage(Vendor vendor) {
        // Fetch real history from SQLite
        List<SurplusRecord> history = surplusDAO.getRecordsByVendor(vendor.getId());
        
        return history.stream()
            .limit(7)
            .mapToInt(SurplusRecord::getSurplusCount)
            .average()
            .orElse(0.0);
    }

    public String getWeeklyInsight(Vendor vendor) {
        double avg = getSevenDayAverage(vendor);
        String manualInsight = (avg > 5.0) 
            ? "Action Required: High surplus detected. Average: " + Math.round(avg) + " units."
            : "Sustainability Goal Met: Low food waste this week!";

        try {
            String aiSuggestion = getAIStockSuggestion(vendor, avg);
            return manualInsight + "\n" + aiSuggestion;
        } catch (Exception e) {
            return manualInsight + "\nAI Suggestion: Service temporarily unavailable.";
        }
    }

    public String getAIStockSuggestion(Vendor vendor, double avgSurplus) throws Exception {
        // Fetch API Key from Config file
        String apiKey = Config.GEMINI_API_KEY; 
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + apiKey;

        String prompt = "As a food waste expert, a vendor named " + vendor.getRestaurantName() + 
                        " has a 7-day average surplus of " + avgSurplus + " items. " +
                        "Suggest a stock reduction strategy in 1 sentence.";

        String jsonBody = "{\"contents\": [{\"parts\": [{\"text\": \"" + prompt + "\"}]}]}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            // Basic cleanup of raw JSON if you don't have a JSON library yet
            return "AI Insight: " + response.body().replaceAll("\\s+", " "); 
        } else {
            return "AI Suggestion: Reduce inventory by 10% to meet SDG 12 (Responsible Consumption).";
        }
    }
}
