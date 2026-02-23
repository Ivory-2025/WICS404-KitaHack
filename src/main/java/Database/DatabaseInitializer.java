package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseInitializer {

    public static void initialize() {
        // 1. Users table (Core profile)
        String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT,
                    email TEXT UNIQUE,
                    password TEXT,
                    role TEXT,
                    latitude REAL,
                    longitude REAL
                );
                """;

        // 2. Vendors/NGOs details table
        String createVendorsTable = """
                CREATE TABLE IF NOT EXISTS vendors (
                    vendor_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    restaurant_name TEXT,
                    address TEXT,
                    trust_score REAL,
                    radiusCoverage REAL,
                    capacity INTEGER,
                    FOREIGN KEY(user_id) REFERENCES users(id)
                );
                """;

        // 3. Food Listings table (The surplus items)
        String createFoodListingsTable = """
                CREATE TABLE IF NOT EXISTS food_listings (
                    listingId INTEGER PRIMARY KEY AUTOINCREMENT,
                    vendorId INTEGER,
                    foodName TEXT,
                    imagePath TEXT,
                    productionTime TEXT,
                    ingredients TEXT,
                    status TEXT,
                    expiryTime TEXT,
                    FOREIGN KEY(vendorId) REFERENCES users(id)
                );
                """;

        // 4. Transactions table (Claims/Purchases)
        String createTransactionsTable = """
                CREATE TABLE IF NOT EXISTS transactions (
                    transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    listing_id INTEGER,
                    ngo_id INTEGER,
                    transaction_date TEXT,
                    status TEXT,
                    FOREIGN KEY(listing_id) REFERENCES food_listings(listingId),
                    FOREIGN KEY(ngo_id) REFERENCES users(id)
                );
                """;

        // 5. Surplus Records table (For Analytics)
        String createSurplusRecordsTable = """
                CREATE TABLE IF NOT EXISTS surplus_records (
                    record_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    vendor_id INTEGER,
                    amount REAL,
                    date TEXT,
                    food_type TEXT,
                    FOREIGN KEY(vendor_id) REFERENCES users(id)
                );
                """;

        // 6. Ratings table
        String createRatingsTable = """
                CREATE TABLE IF NOT EXISTS ratings (
                    rating_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    from_user_id INTEGER,
                    to_user_id INTEGER,
                    score INTEGER CHECK(score BETWEEN 1 AND 5),
                    comment TEXT,
                    FOREIGN KEY(from_user_id) REFERENCES users(id),
                    FOREIGN KEY(to_user_id) REFERENCES users(id)
                );
                """;

        // Add this after the table creation logic
        try (Connection conn = DatabaseConnection.connect(); 
         Statement stmt = conn.createStatement()) {
        
        if (conn == null) {
            System.err.println("Database connection is null. Check DatabaseConnection.java");
            return;
        }

        // IMPORTANT: You must execute the strings to actually create the tables
        stmt.execute(createUsersTable);
        stmt.execute(createVendorsTable);
        stmt.execute(createFoodListingsTable);
        stmt.execute(createTransactionsTable);
        stmt.execute(createSurplusRecordsTable);
        stmt.execute(createRatingsTable);

        System.out.println("All tables initialized successfully in kitaHack.db.");

        // Auto-create test user so you can log in immediately
        /// 1. Define the SQL string first
String insertTestUser = "INSERT OR IGNORE INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";

// 2. Now use it in the PreparedStatement
try (PreparedStatement pstmt = conn.prepareStatement(insertTestUser)) {
    pstmt.setString(1, "Test User");
    pstmt.setString(2, "aiburiliong@gmail.com");
    pstmt.setString(3, "password123");
    pstmt.setString(4, "VENDOR");
    pstmt.executeUpdate();
    System.out.println("DEBUG: Test user ready: aiburiliong@gmail.com");
}

    } catch (SQLException e) {
        System.err.println("Database Initialization Error: " + e.getMessage());
        e.printStackTrace();
    }
}

    public static void main(String[] args) {
        initialize();
    }
}