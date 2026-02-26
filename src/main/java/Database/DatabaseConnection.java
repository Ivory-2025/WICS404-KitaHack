package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:kitaHack.db";

    public static Connection connect() {
        Connection conn = null;
        try {
            // Establish the connection to the local .db file
            conn = DriverManager.getConnection(URL);
            // System.out.println("✅ Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.err.println("❌ Database Connection Error: " + e.getMessage());
        }
        return conn;
    }
}
