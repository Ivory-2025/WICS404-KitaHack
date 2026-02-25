package Models;

import java.time.LocalDate;

public class SurplusRecord {
    private int recordId; // Changed to int for SQLite compatibility
    private Vendor vendor;
    private LocalDate date;
    private int surplusCount;
    private double estimatedCo2Saved;
    private String foodType; // Added to match DAO requirement

    // Full Constructor
    public SurplusRecord(int recordId, Vendor vendor, LocalDate date, int surplusCount, String foodType) {
        this.recordId = recordId;
        this.vendor = vendor;
        this.date = date;
        this.surplusCount = surplusCount;
        this.foodType = foodType;
        this.estimatedCo2Saved = surplusCount * 2.5; 
    }

    // Default Constructor for JavaFX/DAO flexibility
    public SurplusRecord() {
        this.date = LocalDate.now();
    }

    // Getters
    public int getRecordId() { return recordId; }
    public Vendor getVendor() { return vendor; }
    public LocalDate getDate() { return date; }
    public int getSurplusCount() { return surplusCount; }
    public double getEstimatedCo2Saved() { return estimatedCo2Saved; }
    public String getFoodType() { return foodType; }

    // Setters
    public void setRecordId(int recordId) { this.recordId = recordId; }
    public void setVendor(Vendor vendor) { this.vendor = vendor; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setFoodType(String foodType) { this.foodType = foodType; }
    
    public void setSurplusCount(int surplusCount) {
        this.surplusCount = surplusCount;
        this.estimatedCo2Saved = surplusCount * 2.5;
    }

    // Methods
    public double getAmount() {
        return (double) surplusCount;
    }
    
    public void setAmount(double amount) {
        // Since database 'amount' is a double but model uses 'surplusCount' as int
        this.surplusCount = (int) amount;
        this.estimatedCo2Saved = this.surplusCount * 2.5;
    }

    @Override
    public String toString() {
        return "SurplusRecord{" +
                "Date=" + date +
                ", Items=" + surplusCount +
                ", CO2 Saved=" + estimatedCo2Saved + "kg" +
                ", Type='" + foodType + '\'' +
                '}';
    }
}