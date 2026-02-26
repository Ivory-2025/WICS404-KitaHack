package DAO;

import Database.DatabaseConnection;
import Models.Rating;
import Models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RatingDAO {

    /**
     * Saves a new rating to the database.
     * Matches the table structure in your DatabaseInitializer.
     */
    public void save(Rating rating) {
        String sql = "INSERT INTO ratings (from_user_id, to_user_id, score, comment) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, rating.getFromUser().getUserId());
            pstmt.setInt(2, rating.getToUser().getUserId());
            pstmt.setInt(3, rating.getScore());
            pstmt.setString(4, rating.getComment());

            pstmt.executeUpdate();
            System.out.println("DAO: Rating saved successfully.");

        } catch (SQLException e) {
            System.err.println("Error saving rating: " + e.getMessage());
        }
    }

    /**
     * Calculates the average trust score for a specific vendor.
     * This result can be used to update the Vendor's trust_score in the users table.
     */
    public double getAverageScoreForUser(int userId) {
        String sql = "SELECT AVG(score) as average FROM ratings WHERE to_user_id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("average");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public List<Models.VendorRatingSummary> getVendorLeaderboard() {
    List<Models.VendorRatingSummary> list = new ArrayList<>();
    // Join with users to get names, group by vendor, and order by highest average
    String sql = "SELECT u.name, AVG(r.score) as avg_score " +
                 "FROM ratings r " +
                 "JOIN users u ON r.to_user_id = u.id " +
                 "GROUP BY u.name " +
                 "ORDER BY avg_score DESC";

    try (Connection conn = Database.DatabaseConnection.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
            list.add(new Models.VendorRatingSummary(
                rs.getString("name"),
                rs.getDouble("avg_score")
            ));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return list;
}
    /**
     * Retrieves all comments/ratings for a specific vendor.
     */
    public List<Rating> getRatingsForUser(int userId) {
        List<Rating> ratings = new ArrayList<>();
        String sql = "SELECT r.*, u.name FROM ratings r JOIN users u ON r.from_user_id = u.id WHERE r.to_user_id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Logic to map result set to Rating objects
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ratings;
    }
}
