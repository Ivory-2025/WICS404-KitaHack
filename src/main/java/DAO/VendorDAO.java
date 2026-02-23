package DAO;

import Database.DatabaseConnection;
import Models.Vendor;
import java.sql.*;

public class VendorDAO {

    /**
     * Inserts a new vendor by creating a User record first and then the Vendor details.
     * Uses a transaction to ensure both tables stay in sync.
     */
    public void insertVendor(Vendor vendor) {
        String insertUserSQL = "INSERT INTO users(name, email, password, role, latitude, longitude) VALUES(?,?,?,?,?,?)";
        String insertVendorSQL = "INSERT INTO vendors(user_id, restaurant_name, address, trust_score) VALUES(?,?,?,?)";

        try (Connection conn = DatabaseConnection.connect()) {
            conn.setAutoCommit(false); // Transaction start

            try (PreparedStatement userStmt = conn.prepareStatement(insertUserSQL, Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, vendor.getName());
                userStmt.setString(2, vendor.getEmail());
                userStmt.setString(3, vendor.getPassword());
                userStmt.setString(4, "VENDOR");
                userStmt.setDouble(5, vendor.getLatitude());
                userStmt.setDouble(6, vendor.getLongitude());
                userStmt.executeUpdate();

                ResultSet generatedKeys = userStmt.getGeneratedKeys();
                int userId = 0;
                if (generatedKeys.next()) {
                    userId = generatedKeys.getInt(1);
                }

                try (PreparedStatement vendorStmt = conn.prepareStatement(insertVendorSQL)) {
                    vendorStmt.setInt(1, userId);
                    vendorStmt.setString(2, vendor.getRestaurantName());
                    vendorStmt.setString(3, vendor.getAddress());
                    vendorStmt.setDouble(4, vendor.getTrustScore());
                    vendorStmt.executeUpdate();
                }
                conn.commit(); // Success
                System.out.println("Vendor inserted successfully.");
            } catch (SQLException e) {
                conn.rollback(); // Undo if something fails
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches a Vendor by their User ID. 
     * Useful for your Dashboard and Analytics logic.
     */
    public Vendor getVendorById(int id) {
        String sql = """
                SELECT u.*, v.restaurant_name, v.address, v.trust_score
                FROM users u
                JOIN vendors v ON u.id = v.user_id
                WHERE u.id = ?
                """;
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Vendor getVendorByEmail(String email) {
        String sql = """
                SELECT u.*, v.restaurant_name, v.address, v.trust_score
                FROM users u
                JOIN vendors v ON u.id = v.user_id
                WHERE u.email = ?
                """;
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Vendor mapResultSet(ResultSet rs) throws SQLException {
        Vendor vendor = new Vendor();
        // Inherited fields from User
        vendor.setId(rs.getInt("id"));
        vendor.setName(rs.getString("name"));
        vendor.setEmail(rs.getString("email"));
        vendor.setPassword(rs.getString("password"));
        vendor.setRole(rs.getString("role"));
        vendor.setLatitude(rs.getDouble("latitude"));
        vendor.setLongitude(rs.getDouble("longitude"));

        // Vendor specific fields
        vendor.setRestaurantName(rs.getString("restaurant_name"));
        vendor.setAddress(rs.getString("address"));
        vendor.setTrustScore(rs.getDouble("trust_score"));
        return vendor;
    }
}