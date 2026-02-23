package Models;

import java.time.LocalDateTime;

public class Transaction {
    private int transactionId; 
    private User buyer;        // The NGO making the claim
    private Vendor vendor;     // The Restaurant providing the food
    private FoodListing listing; // Reference to the specific surplus item
    private String foodItem;   
    private double amount;     
    private LocalDateTime transactionTime;
    private String status;     // "PENDING", "COMPLETED", "EXPIRED"

    // Default Constructor for DAO mapping
    public Transaction() {
        this.transactionTime = LocalDateTime.now();
        this.status = "PENDING";
    }

    // Constructor for NEW transactions
    public Transaction(User buyer, Vendor vendor, FoodListing listing, double amount) {
        this.buyer = buyer;
        this.vendor = vendor;
        this.listing = listing;
        this.foodItem = listing.getFoodName();
        this.amount = amount;
        this.transactionTime = LocalDateTime.now();
        this.status = "COMPLETED";
    }

    // --- Getters and Setters ---

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public User getBuyer() { return buyer; }
    public void setBuyer(User buyer) { this.buyer = buyer; }

    public Vendor getVendor() { return vendor; }
    public void setVendor(Vendor vendor) { this.vendor = vendor; }

    public FoodListing getListing() { return listing; }
    public void setListing(FoodListing listing) { this.listing = listing; }

    public String getFoodItem() { return foodItem; }
    public void setFoodItem(String foodItem) { this.foodItem = foodItem; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDateTime getTransactionTime() { return transactionTime; }
    public void setTransactionTime(LocalDateTime transactionTime) { this.transactionTime = transactionTime; }
    
    // Added for DAO compatibility
    public LocalDateTime getTransactionDate() { return transactionTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Helper for NGO Dashboard compatibility
    public NGO getNgo() { return (buyer instanceof NGO) ? (NGO) buyer : null; }
}