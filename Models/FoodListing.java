package Models;

import java.time.LocalDateTime;

public class FoodListing {

    private int listingId;
    private Vendor vendor;
    private String foodName;
    private String imagePath;
    private LocalDateTime productionTime;
    private String ingredients;
    private String status;
    private LocalDateTime expiryTime;

    // Constructors

    // Default constructor
    public FoodListing() {}

    // Full constructor
    public FoodListing(int listingId, Vendor vendor, String foodName, String imagePath,
                       LocalDateTime productionTime, String ingredients,
                       String status, LocalDateTime expiryTime) {
        this.listingId = listingId;
        this.vendor = vendor;
        this.foodName = foodName;
        this.imagePath = imagePath;
        this.productionTime = productionTime;
        this.ingredients = ingredients;
        this.status = status;
        this.expiryTime = expiryTime;
    }

    // Getters & Setters

    public int getListingId() {
        return listingId;
    }

    public void setListingId(int listingId) {
        this.listingId = listingId;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public LocalDateTime getProductionTime() {
        return productionTime;
    }

    public void setProductionTime(LocalDateTime productionTime) {
        this.productionTime = productionTime;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    // Methods
    // Check if food is still valid (not expired)
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryTime);
    }

    // Get remaining hours before expiry
    public long hoursUntilExpiry() {
        return java.time.Duration.between(LocalDateTime.now(), this.expiryTime).toHours();
    }

    //to String method
    @Override
    public String toString() {
        return "FoodListing{" +
                "listingId=" + listingId +
                ", vendor=" + vendor +
                ", foodName='" + foodName + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", productionTime=" + productionTime +
                ", ingredients='" + ingredients + '\'' +
                ", status='" + status + '\'' +
                ", expiryTime=" + expiryTime +
                '}';
    }
}
