package DAO;

import Database.DatabaseConnection;
import Models.User;
import Models.Vendor;
import Models.NGO;
import java.sql.*;

public class UserDAOImpl implements UserDAOInt {

    @Override
    public void insertUser(User user) {
        String sql = "INSERT INTO users(name, email, password, role, latitude, longitude) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getRole());
            pstmt.setDouble(5, user.getLatitude()); // Added missing parameter 5
            pstmt.setDouble(6, user.getLongitude()); // Added missing parameter 6

            pstmt.executeUpdate();
            System.out.println("User inserted successfully.");

        } catch (SQLException e) {
            System.err.println("Insert Error: " + e.getMessage());
        }
    }

    @Override
    public User getUserByEmail(String email) {
    String sql = "SELECT * FROM users WHERE email = ?";
    try (Connection conn = DatabaseConnection.connect(); // Using your class here!
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, email);
        ResultSet rs = pstmt.executeQuery();
        // Inside UserDAOImpl.java
if (rs.next()) {
    User user = new User();
    user.setId(rs.getInt("id"));
    user.setName(rs.getString("name"));      // If this is missing, you get "Welcome null"
    user.setEmail(rs.getString("email"));
    user.setPassword(rs.getString("password")); // If this is missing, you get the NullPointerException
    user.setRole(rs.getString("role"));
    return user;
}
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}

    @Override
    public void updateUser(User user) {
        // Important for updating Latitude/Longitude for your SDG project
        String sql = "UPDATE users SET name = ?, latitude = ?, longitude = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getName());
            pstmt.setDouble(2, user.getLatitude());
            pstmt.setDouble(3, user.getLongitude());
            pstmt.setInt(4, user.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public User login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("id"), rs.getString("name"), 
                                rs.getString("email"), rs.getString("password"), 
                                rs.getString("role"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    //DELETE
    @Override
    public void deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("User deleted.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}