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
    private static final double CO2_FACTOR = 2.5;//Avg 2.5kg CO2 per 1kg of food waste

    public void recordDailySurplus(SurplusRecord record) {
        surplusDAO.save(record);
    }

    public double getSevenDayAverage(Vendor vendor) {
        // Fetch real history from SQLite
        List<SurplusRecord> history = surplusDAO.getRecordsByVendor(vendor.getUserId());
        
        return history.stream()
            .limit(7)
            .mapToInt(SurplusRecord::getSurplusCount)
            .average()
            .orElse(0.0);
    }

    public double calculateCO2Impact(Vendor vendor) {
        List<SurplusRecord> history = surplusDAO.getRecordsByVendor(vendor.getUserId());
        return history.stream().mapToDouble(SurplusRecord::getSurplusCount).sum()* CO2_FACTOR;
    }

    public String getWeeklyInsight(Vendor vendor) {
        double avg = getSevenDayAverage(vendor);
        double co2 = calculateCO2Impact(vendor);
        String manualInsight;

    // Condition 1: Zero Waste
    if (avg == 0) {
        manualInsight = "Sustainability Goal Met: No food waste detected! Keep up the great work.";
        return manualInsight; // No need for AI suggestion if there is no waste
    } 
    // Condition 2: High Waste (e.g., more than 5 units)
    else if (avg > 5.0) {
        manualInsight = "Action Required: High surplus detected. Average: " + Math.round(avg) + " units.";
    } 
    // Condition 3: Low Waste (between 0 and 5)
    else {
        manualInsight = "Sustainability Goal Met: Low food waste this week!";
    }

        try {
            String aiSuggestion = getAIStockSuggestion(vendor, avg, co2);
            return manualInsight + "\n" + aiSuggestion;
        } catch (Exception e) {
            return manualInsight + "\nAI Suggestion: Service temporarily unavailable.";
        }
    }

    public String getAIStockSuggestion(Vendor vendor, double avgSurplus,double co2) throws Exception {
        // Fetch API Key from Config file
        String apiKey = Config.GEMINI_API_KEY; 
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + apiKey;

        String prompt = "As a food waste expert, a restaurant named " + vendor.getRestaurantName() + 
                        " has a weekly surplus of " + avgSurplus + " items, causing " + co2 + 
                        "kg of CO2. Predict if they should reduce stock and suggest a strategy in 1 sentence.";

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
