package DAO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import Models.SurplusRecord;
public class SurplusRecordDAO {
    // Simulated database table 
    private List<SurplusRecord> records = new ArrayList<>();

    // Create: Save a new daily surplus record
    public void save(SurplusRecord record) {
        records.add(record);
        System.out.println("DAO: Surplus record saved for " + record.getVendor().getName());
    }

    // Read: Get all records for a specific vendor to analyze trends
    public List<SurplusRecord> getRecordsByVendor(String vendorName) {
        return records.stream()
                .filter(r -> r.getVendor().getName().equalsIgnoreCase(vendorName))
                .collect(Collectors.toList());
    }

    // Read: Get all records for the whole system
    public List<SurplusRecord> getAllRecords() {
        return new ArrayList<>(records);
    }
}
