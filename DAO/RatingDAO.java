package DAO;

import Database.DatabaseConnection;
import Models.Rating;
import Models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RatingDAO {

    // Insert new rating
    public void insertRating(Rating rating) {

        String sql = """
                INSERT INTO ratings(from_user_id, to_user_id, score, comment)
                VALUES(?,?,?,?)
                """;

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, rating.getFromUser().getId());
            pstmt.setInt(2, rating.getToUser().getId());
            pstmt.setInt(3, rating.getScore());
            pstmt.setString(4, rating.getComment());

            pstmt.executeUpdate();
            System.out.println("Rating inserted successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get all ratings for a specific user (vendor or NGO)
    public List<Rating> getRatingsForUser(int userId) {

        String sql = """
                SELECT r.*, 
                       fu.id as from_id, fu.name as from_name, fu.email as from_email,
                       tu.id as to_id, tu.name as to_name, tu.email as to_email
                FROM ratings r
                JOIN users fu ON r.from_user_id = fu.id
                JOIN users tu ON r.to_user_id = tu.id
                WHERE r.to_user_id = ?
                """;

        List<Rating> ratings = new ArrayList<>();

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

                User fromUser = new User();
                fromUser.setId(rs.getInt("from_id"));
                fromUser.setName(rs.getString("from_name"));
                fromUser.setEmail(rs.getString("from_email"));

                User toUser = new User();
                toUser.setId(rs.getInt("to_id"));
                toUser.setName(rs.getString("to_name"));
                toUser.setEmail(rs.getString("to_email"));

                Rating rating = new Rating(
                        rs.getInt("rating_id"),
                        fromUser,
                        toUser,
                        rs.getInt("score"),
                        rs.getString("comment")
                );

                ratings.add(rating);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ratings;
    }

    // Calculate average rating
    public double getAverageRating(int userId) {

        String sql = "SELECT AVG(score) as avg_score FROM ratings WHERE to_user_id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("avg_score");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }
}