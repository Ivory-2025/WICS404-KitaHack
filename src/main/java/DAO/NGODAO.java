package DAO;

import Database.DatabaseConnection;
import Models.NGO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NGODAO {

    public List<NGO> getAllNGOs() {
        List<NGO> ngos = new ArrayList<>();
        // JOIN users and vendors (NGO details) to get full profile
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

    private NGO mapResultSet(ResultSet rs) throws SQLException {
        NGO ngo = new NGO();
        ngo.setId(rs.getInt("id"));
        ngo.setName(rs.getString("name"));
        ngo.setLatitude(rs.getDouble("latitude"));
        ngo.setLongitude(rs.getDouble("longitude"));
        ngo.setOrganizationName(rs.getString("organizationName"));
        ngo.setRadiusCoverage(rs.getDouble("radiusCoverage"));
        ngo.setCapacity(rs.getInt("capacity"));
        return ngo;
    }
}