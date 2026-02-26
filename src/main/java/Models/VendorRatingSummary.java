package Models;

public class VendorRatingSummary {
    private String vendorName;
    private double averageRating;

    public VendorRatingSummary(String vendorName, double averageRating) {
        this.vendorName = vendorName;
        this.averageRating = averageRating;
    }

    // This method generates the star string for the RATING column
    public String getStars() {
    int solidStars = (int) Math.round(averageRating);
    if (solidStars <= 0) return "No Ratings Yet";
    
    // REMOVED the + " (" + averageRating + ")" part
    return "★".repeat(solidStars); 
}
    public String getVendorName() { return vendorName; }
    public double getAverageRating() { return averageRating; }
}

