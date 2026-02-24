package DAO;

import Database.DatabaseConnection;
import Models.SurplusRecord;
import Models.Vendor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SurplusRecordDAO {

    /**
     * Saves a new daily surplus record to the SQLite database.
     * This allows AnalyticsService to track trends over time.
     */
    public void save(SurplusRecord record) {
        String sql = "INSERT INTO surplus_records (vendor_id, amount, date, food_type) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, record.getVendor().getUserId());
            pstmt.setDouble(2, record.getAmount());
            pstmt.setString(3, record.getDate().toString());
            pstmt.setString(4, record.getFoodType());

            pstmt.executeUpdate();
            System.out.println("DAO: Surplus record saved for " + record.getVendor().getName());

        } catch (SQLException e) {
            System.err.println("Error saving surplus record: " + e.getMessage());
        }
    }

    /**
     * Retrieves all records for a specific vendor using a JOIN with the users table.
     */
    public List<SurplusRecord> getRecordsByVendor(int vendorId) {
        List<SurplusRecord> vendorRecords = new ArrayList<>();
        String sql = "SELECT * FROM surplus_records WHERE vendor_id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, vendorId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                SurplusRecord record = new SurplusRecord();
                record.setAmount(rs.getDouble("amount"));
                record.setFoodType(rs.getString("food_type"));
                // Date parsing logic would go here
                vendorRecords.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vendorRecords;
    }

    public List<SurplusRecord> getAllRecords() {
        List<SurplusRecord> allRecords = new ArrayList<>();
        String sql = "SELECT * FROM surplus_records";

        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Map result set to SurplusRecord objects
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return allRecords;
    }
}
