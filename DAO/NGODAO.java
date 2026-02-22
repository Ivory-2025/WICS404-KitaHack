package DAO;
//TODO: database need to be setup for the code to work

import Database.DatabaseConnection;
import Models.NGO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NGODAO {

    /**
     * Gets all NGOs from the database.
     * Used by MatchingService to find eligible NGOs.
     */
    public List<NGO> getAllNGOs() {
        List<NGO> ngos = new ArrayList<>();
        String sql = "SELECT * FROM ngos";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ngos.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error getting all NGOs: " + e.getMessage());
        }

        return ngos;
    }

    /**
     * Gets a single NGO by their user ID.
     */
    public NGO getNGOById(int id) {
        String sql = "SELECT * FROM ngos WHERE id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSet(rs);
            }

        } catch (SQLException e) {
            System.out.println("Error getting NGO by ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Maps a ResultSet row to an NGO object.
     */
    private NGO mapResultSet(ResultSet rs) throws SQLException {
        NGO ngo = new NGO();

        ngo.setId(rs.getInt("id"));
        ngo.setName(rs.getString("name"));
        ngo.setEmail(rs.getString("email"));
        ngo.setPassword(rs.getString("password"));
        ngo.setRole(rs.getString("role"));
        ngo.setOrganizationName(rs.getString("organizationName"));
        ngo.setAddress(rs.getString("address"));
        ngo.setRadiusCoverage(rs.getDouble("radiusCoverage"));
        ngo.setCapacity(rs.getInt("capacity"));

        return ngo;
    }
}
