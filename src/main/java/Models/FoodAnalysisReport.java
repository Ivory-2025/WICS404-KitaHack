package Models;

import java.io.File;
import java.util.List;

public class FoodAnalysisReport {
    private int reportId;
    private FoodListing listing;
    private List<String> allergens;
    private boolean vegetarian;
    private boolean halalFriendly;
    private double freshnessScore;
    private String recommendation;

    // Default Constructor
    public FoodAnalysisReport() {}

    // Added: Full Constructor for easy instantiation in Service layers
    public FoodAnalysisReport(int reportId, FoodListing listing, List<String> allergens, 
                              boolean vegetarian, boolean halalFriendly, 
                              double freshnessScore, String recommendation) {
        this.reportId = reportId;
        this.listing = listing;
        this.allergens = allergens;
        this.vegetarian = vegetarian;
        this.halalFriendly = halalFriendly;
        this.freshnessScore = freshnessScore;
        this.recommendation = recommendation;
    }

    public FoodAnalysisReport(
        FoodListing listing,
        List<String> allergens,
        boolean vegetarian,
        boolean halalFriendly,
        double freshnessScore,
        String recommendation
) {
    this.listing = listing;
    this.allergens = allergens;
    this.vegetarian = vegetarian;
    this.halalFriendly = halalFriendly;
    this.freshnessScore = freshnessScore;
    this.recommendation = recommendation;
}
    // --- Getters ---
    public int getReportId() { return reportId; }
    public FoodListing getListing() { return listing; }
    public List<String> getAllergens() { return allergens; }
    public boolean isVegetarian() { return vegetarian; }
    public boolean isHalalFriendly() { return halalFriendly; }
    public double getFreshnessScore() { return freshnessScore; }
    public String getRecommendation() { return recommendation; }

    // --- Setters ---
    public void setReportId(int reportId) { this.reportId = reportId; }
    public void setListing(FoodListing listing) { this.listing = listing; }
    public void setAllergens(List<String> allergens) { this.allergens = allergens; }
    public void setVegetarian(boolean vegetarian) { this.vegetarian = vegetarian; }
    public void setHalalFriendly(boolean halalFriendly) { this.halalFriendly = halalFriendly; }
    public void setFreshnessScore(double freshnessScore) { this.freshnessScore = freshnessScore; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    // Helper: Get Listing ID directly for DAO operations
    public int getListingId() {
        return (listing != null) ? listing.getListingId() : -1;
    }
    public String generateGeminiReport(File imageFile) {
    if (imageFile == null) return "No image provided for analysis.";

    // Logic: In a real production environment, you would use the 
    // Google AI Client SDK here to send the image to Gemini 1.5 Flash.
    
    String fileName = imageFile.getName().toLowerCase();
    
    // This simulates Gemini recognizing different items
    if (fileName.contains("banana")) {
        return "This banana is overripe with significant spotting. It is best used for baking (e.g., banana bread) rather than fresh consumption.";
    } else if (fileName.contains("bread")) {
        return "The bread appears to have surface dryness but no visible mold. It is safe for consumption if toasted.";
    } else {
        return "Gemini has analyzed the " + fileName + ". The item appears to be in surplus condition and is safe for donation. Please verify the expiry manually.";
    }
}
}  

