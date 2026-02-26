package DAO;

import Database.DatabaseConnection;
import Models.FoodListing;
import Models.Vendor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import Services.FoodListingService;

public class FoodListingDAO {

    public Models.FoodListing getLatestListingByVendor(int vendorUserId) {
    // Use listingId for ordering as per your schema
    String sql = "SELECT * FROM food_listings WHERE vendor_id = ? ORDER BY listingId DESC LIMIT 1";
    
    try (Connection conn = Database.DatabaseConnection.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, vendorUserId);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            Models.FoodListing listing = new Models.FoodListing();
            listing.setListingId(rs.getInt("listingId")); // Fixed column name
            listing.setFoodName(rs.getString("food_name")); // Fixed column name
            listing.setStatus(rs.getString("status"));
            // Since 'quantity' is missing in schema, use a placeholder to stop the error
            listing.setQuantity("Standard Pack"); 
            return listing;
        }
    } catch (SQLException e) {
        System.err.println("Database Error: " + e.getMessage());
    }
    return null;
}
    private List<FoodListing> listings = new ArrayList<>();
    /**
     * Saves a new food listing to the database.
     * This fixes the "undefined" error in VendorDashboard.java.
     */
    public void save(FoodListing listing) {
        // SQL matches the columns in your DatabaseInitializer
        String sql = "INSERT INTO food_listings (vendor_id, food_name, ingredients, production_time, expiry_time, status) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Getting data from the FoodListing and nested Vendor model
            pstmt.setInt(1, listing.getVendor().getUserId());
            pstmt.setString(2, listing.getFoodName());
            pstmt.setString(3, listing.getIngredients());
            pstmt.setString(4, listing.getProductionTime().toString());
            pstmt.setString(5, listing.getExpiryTime().toString());
            pstmt.setString(6, listing.getStatus());
            
            pstmt.executeUpdate();
            System.out.println("Food listing saved to database.");
            
        } catch (SQLException e) {
            System.out.println("Error saving listing: " + e.getMessage());
        }
    }

    public List<FoodListing> getAvailableListings() {
        List<FoodListing> listings = new ArrayList<>();
        String sql = "SELECT fl.*, u.latitude, u.longitude FROM food_listings fl " +
                     "JOIN users u ON fl.vendorId = u.id WHERE fl.status = 'available'";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                listings.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listings;
    }

    public FoodListing getListingById(int listingId) {
        String sql = "SELECT fl.*, u.latitude, u.longitude FROM food_listings fl " +
                     "JOIN users u ON fl.vendorId = u.id WHERE fl.listingId = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, listingId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateStatus(int listingId, String status, int ngoId) {
    // SQL to update the status and record which NGO claimed the listing
    String sql = "UPDATE food_listings SET status = ?, ngo_id = ? WHERE id = ?";

    try (Connection conn = DatabaseConnection.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, status);
        pstmt.setInt(2, ngoId);
        pstmt.setInt(3, listingId);

        int rowsAffected = pstmt.executeUpdate();
        return rowsAffected > 0; // Return true if the database was updated
        
    } catch (SQLException e) {
        System.err.println("Database Error: " + e.getMessage());
        return false;
    }
}

// Version for Marketplace / Flash Sale (No NGO involved)
public boolean updateStatus(int listingId, String status) {
    String sql = "UPDATE food_listings SET status = ? WHERE id = ?";

    try (Connection conn = DatabaseConnection.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, status);
        pstmt.setInt(2, listingId);

        return pstmt.executeUpdate() > 0;
    } catch (SQLException e) {
        System.err.println("Database Error: " + e.getMessage());
        return false;
    }
}

    private FoodListing mapResultSet(ResultSet rs) throws SQLException {
        FoodListing listing = new FoodListing();
        listing.setListingId(rs.getInt("listingId"));
        listing.setFoodName(rs.getString("foodName"));
        listing.setStatus(rs.getString("status"));
        
        Vendor vendor = new Vendor();
        vendor.setUserId(rs.getInt("vendorId"));
        vendor.setLatitude(rs.getDouble("latitude")); 
        vendor.setLongitude(rs.getDouble("longitude"));
        listing.setVendor(vendor);
        return listing;
    }

    public FoodListing findById(int id) {
    String sql = "SELECT * FROM food_listing WHERE listing_id = ?";

    try (Connection conn = DatabaseConnection.connect();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return new FoodListing(
                rs.getInt("listing_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("location"),
                rs.getString("status")
            );
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return null;
}
public void update(FoodListing listing) {
    String sql = "UPDATE food_listing SET status = ? WHERE listing_id = ?";

    try (Connection conn = DatabaseConnection.connect();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, listing.getStatus());
        stmt.setInt(2, listing.getListingId());
        stmt.executeUpdate();

    } catch (Exception e) {
        e.printStackTrace();
    }
}
}