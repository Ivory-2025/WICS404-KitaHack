package DAO;

//TODO: need to have Database setup for this to work

/*This class has 3 methods 
- getListingById(), getAvailableListings(), and updateStatus()
- These are the exact methods that MatchingService calls, so it all links up cleanly.
*/

import Database.DatabaseConnection;
import Models.FoodListing;
import Models.Vendor;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FoodListingDAO {

    /**
     * Gets a single food listing by its ID.
     */
    public FoodListing getListingById(int listingId) {
        String sql = "SELECT * FROM food_listings WHERE listingId = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, listingId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSet(rs);
            }

        } catch (SQLException e) {
            System.out.println("Error getting listing by ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Gets all food listings with status "available".
     * Used by MatchingService to find listings NGOs can claim.
     */
    public List<FoodListing> getAvailableListings() {
        List<FoodListing> listings = new ArrayList<>();
        String sql = "SELECT * FROM food_listings WHERE status = 'available'";

        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                listings.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error getting available listings: " + e.getMessage());
        }

        return listings;
    }

    /**
     * Updates the status of a food listing.
     * e.g. "available" -> "matched", "matched" -> "completed"
     * Returns true if update was successful.
     */
    public boolean updateStatus(int listingId, String status) {
        String sql = "UPDATE food_listings SET status = ? WHERE listingId = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, listingId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Error updating listing status: " + e.getMessage());
        }

        return false;
    }

    /**
     * Maps a ResultSet row to a FoodListing object.
     * NOTE: Vendor is partially filled — only vendorId and address.
     
     */
    private FoodListing mapResultSet(ResultSet rs) throws SQLException {
        FoodListing listing = new FoodListing();

        listing.setListingId(rs.getInt("listingId"));
        listing.setFoodName(rs.getString("foodName"));
        listing.setImagePath(rs.getString("imagePath"));
        listing.setIngredients(rs.getString("ingredients"));
        listing.setStatus(rs.getString("status"));

        // Parse timestamps
        String productionTimeStr = rs.getString("productionTime");
        String expiryTimeStr = rs.getString("expiryTime");
        if (productionTimeStr != null) {
            listing.setProductionTime(LocalDateTime.parse(productionTimeStr));
        }
        if (expiryTimeStr != null) {
            listing.setExpiryTime(LocalDateTime.parse(expiryTimeStr));
        }

        // Partially fill vendor with just what we have in food_listings table
        //* TODO: need to Setup VendorDAO with methods
        //now its only empty vendor object
        Vendor vendor = new Vendor();
        vendor.setId(rs.getInt("vendorId"));
        listing.setVendor(vendor);

        return listing;
    }
}