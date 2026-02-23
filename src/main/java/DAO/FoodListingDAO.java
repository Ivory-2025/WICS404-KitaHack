package DAO;

import Database.DatabaseConnection;
import Models.FoodListing;
import Models.Vendor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FoodListingDAO {

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
            pstmt.setInt(1, listing.getVendor().getId());
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

    public boolean updateStatus(int listingId, String status) {
        String sql = "UPDATE food_listings SET status = ? WHERE listingId = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, listingId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private FoodListing mapResultSet(ResultSet rs) throws SQLException {
        FoodListing listing = new FoodListing();
        listing.setListingId(rs.getInt("listingId"));
        listing.setFoodName(rs.getString("foodName"));
        listing.setStatus(rs.getString("status"));
        
        Vendor vendor = new Vendor();
        vendor.setId(rs.getInt("vendorId"));
        vendor.setLatitude(rs.getDouble("latitude")); 
        vendor.setLongitude(rs.getDouble("longitude"));
        listing.setVendor(vendor);
        return listing;
    }
}