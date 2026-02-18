package Models;
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
    public AIAnalysisResult() {}

    // Getters
    public int getReportId() { return reportId; }
    public FoodListing getListing() { return listing; }
    public List<String> getAllergens() { return allergens; }
    public boolean isVegetarian() { return vegetarian; }
    public boolean isHalalFriendly() { return halalFriendly; }
    public double getFreshnessScore() { return freshnessScore; }
    public String getRecommendation() { return recommendation; }

    // Setters
    public void setReportId(int reportId) { this.reportId = reportId; }
    public void setListing(FoodListing listing) { this.listing = listing; }
    public void setAllergens(List<String> allergens) { this.allergens = allergens; }
    public void setVegetarian(boolean vegetarian) { this.vegetarian = vegetarian; }
    public void setHalalFriendly(boolean halalFriendly) { this.halalFriendly = halalFriendly; }
    public void setFreshnessScore(double freshnessScore) { this.freshnessScore = freshnessScore; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
}   

