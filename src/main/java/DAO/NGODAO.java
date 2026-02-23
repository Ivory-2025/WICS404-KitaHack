package DAO;

import Database.DatabaseConnection;
import Models.NGO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NGODAO {

    /**
     * Gets all NGOs from the database.
     * Joins users table to retrieve coordinates and vendors table for NGO-specific details.
     */
    public List<NGO> getAllNGOs() {
        List<NGO> ngos = new ArrayList<>();
        String sql = "SELECT u.*, v.organizationName, v.radiusCoverage, v.capacity " +
                     "FROM users u JOIN vendors v ON u.id = v.user_id WHERE u.role = 'NGO'";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ngos.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ngos;
    }

    /**
     * Fetches a single NGO by their User ID.
     * Useful for setting the current NGO in the dashboard after login.
     */
    public NGO getNGOById(int userId) {
        String sql = "SELECT u.*, v.organizationName, v.radiusCoverage, v.capacity " +
                     "FROM users u JOIN vendors v ON u.id = v.user_id WHERE u.id = ? AND u.role = 'NGO'";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private NGO mapResultSet(ResultSet rs) throws SQLException {
        NGO ngo = new NGO();
        // Fields inherited from User model
        ngo.setId(rs.getInt("id"));
        ngo.setName(rs.getString("name"));
        ngo.setEmail(rs.getString("email"));
        ngo.setRole(rs.getString("role"));
        ngo.setLatitude(rs.getDouble("latitude"));
        ngo.setLongitude(rs.getDouble("longitude"));
        
        // Fields specific to NGO
        ngo.setOrganizationName(rs.getString("organizationName"));
        ngo.setRadiusCoverage(rs.getDouble("radiusCoverage"));
        ngo.setCapacity(rs.getInt("capacity"));
        
        return ngo;
    }
}