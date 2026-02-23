package Models;

import java.time.LocalDate;
public class SurplusRecord {
    private String recordId;
    private Vendor vendor;
    private LocalDate date;
    private int surplusCount;
    private double estimatedCo2Saved;

    public SurplusRecord(String recordId, Vendor vendor, LocalDate date, int surplusCount) {
        this.recordId = recordId;
        this.vendor = vendor;
        this.date = date;
        this.surplusCount = surplusCount;
        this.estimatedCo2Saved = surplusCount * 2.5; 
    }

    // Getters
    public String getRecordId() { return recordId; }
    public Vendor getVendor() { return vendor; }
    public LocalDate getDate() { return date; }
    public int getSurplusCount() { return surplusCount; }
    public double getEstimatedCo2Saved() { return estimatedCo2Saved; }

    // Setters
    public void setSurplusCount(int surplusCount) {
        this.surplusCount = surplusCount;
        this.estimatedCo2Saved = surplusCount * 2.5;
    }

    @Override
    public String toString() {
        return "SurplusRecord{" +"Date=" + date +", Items=" + surplusCount +", CO2 Saved=" + estimatedCo2Saved + "kg" +'}';
    }

}
