package DAO;

import Database.DatabaseConnection;
import Models.FoodListing;
import Models.Vendor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FoodListingDAO {

    public List<FoodListing> getAvailableListings() {
        List<FoodListing> listings = new ArrayList<>();
        // JOIN users to get vendor latitude/longitude for matching
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
        vendor.setLatitude(rs.getDouble("latitude")); // Crucial for MatchingService
        vendor.setLongitude(rs.getDouble("longitude"));
        listing.setVendor(vendor);
        return listing;
    }
}