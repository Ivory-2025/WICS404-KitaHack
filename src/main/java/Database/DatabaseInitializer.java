package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseInitializer {

    public static void initialize() {
        insertTestData();
        insertMarketplaceMission();
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

        // 2.5 NGOs details table
        String createNGOsTable = """
                CREATE TABLE IF NOT EXISTS NGOs (
                    ngo_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    organizationName TEXT,
                    latitude REAL,
                    longitude REAL,
                    radiusCoverage REAL,
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
        stmt.execute(createNGOsTable);
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
    pstmt.setString(1, "Ali's Cafe");
    pstmt.setString(2, "aiburiliong@gmail.com");
    pstmt.setString(3, "password123");
    pstmt.setString(4, "VENDOR");
    pstmt.executeUpdate();
    System.out.println("DEBUG: Test user ready: aiburiliong@gmail.com");
}
// --- UPDATED TEST NGO CREATION ---
        
        // 1. Create the core User account
        String insertTestUserNgo = "INSERT OR IGNORE INTO users (name, email, password, role, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmtNgo = conn.prepareStatement(insertTestUserNgo)) {
            pstmtNgo.setString(1, "PJ Food Rescue");
            pstmtNgo.setString(2, "ngo@saveplate.com");
            pstmtNgo.setString(3, "password123");
            pstmtNgo.setString(4, "NGO");
            pstmtNgo.setDouble(5, 3.1073); // Petaling Jaya 
            pstmtNgo.setDouble(6, 101.6067); 
            pstmtNgo.executeUpdate();
        }

        // 2. Create the linked NGO Profile
        String insertTestNgoProfile = """
            INSERT OR IGNORE INTO NGOs (user_id, organizationName, latitude, longitude, radiusCoverage) 
            SELECT id, 'PJ Food Rescue', 3.1073, 101.6067, 15.0 
            FROM users WHERE email = 'ngo@saveplate.com'
        """;
        try (Statement stmtNgoProfile = conn.createStatement()) {
            stmtNgoProfile.execute(insertTestNgoProfile);
            System.out.println("DEBUG: Test NGO & Profile ready: ngo@saveplate.com");
        }

        // --- ADDED RATING TEST DATA ---
// 1. Create a Vendor profile for Ali's Cafe (so the leaderboard has a name)
String insertVendorProfile = """
    INSERT OR IGNORE INTO vendors (user_id, restaurant_name, trust_score) 
    SELECT id, 'Ali''s Cafe', 5.0 
    FROM users WHERE email = 'aiburiliong@gmail.com'
""";
stmt.execute(insertVendorProfile);

// 2. Wipe old ratings to keep the demo clean
stmt.execute("DELETE FROM ratings");

// 3. Add the Stars (Test Data)
// NGO (ID 2) rates Ali's Cafe (ID 1)
stmt.execute("INSERT INTO ratings (from_user_id, to_user_id, score, comment) VALUES (2, 1, 5, 'Amazing Nasi Lemak!')");
// Another NGO rates Ali's Cafe
stmt.execute("INSERT INTO ratings (from_user_id, to_user_id, score, comment) VALUES (2, 1, 4, 'Very helpful staff')");

System.out.println("DEBUG: Leaderboard test data (Ali's Cafe) is ready!");

stmt.execute("INSERT OR IGNORE INTO users (id, name, email, password, role, latitude, longitude) " +
             "VALUES (10, 'Ali''s Cafe', 'ali@cafe.com', 'pass', 'VENDOR', 3.0486, 101.5855)");

stmt.execute("UPDATE users SET latitude = 3.1073, longitude = 101.6067 WHERE id = 1");

// 2. Add the actual food listing
stmt.execute("DELETE FROM food_listings");
String tomorrow = java.time.LocalDateTime.now().plusDays(1).toString(); // Makes it valid for 24h

String insertFood = String.format("""
    INSERT INTO food_listings (vendorId, foodName, status, expiryTime, ingredients) 
    VALUES (10, 'Nasi Lemak Bungkus', 'available', '%s', 'Coconut rice, egg, sambal')
    """, tomorrow);
stmt.execute(insertFood);
System.out.println("DEBUG: Nasi Lemak test data is live!");
    } catch (SQLException e) {
        System.err.println("Database Initialization Error: " + e.getMessage());
        e.printStackTrace();
    }
    }


    public static void insertTestData() {
        String sql = "INSERT INTO food_listings (vendor_id, food_name, ingredients, status) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = Database.DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            // Sample 1: Artisan Sourdough for Vendor 1 (SavePlate Cafe)
            pstmt.setInt(1, 1); 
            pstmt.setString(2, "Artisan Sourdough");
            pstmt.setString(3, "Flour, Water, Sea Salt");
            pstmt.setString(4, "PENDING"); // Must be PENDING to show up
            pstmt.executeUpdate();

            // Sample 2: Truffle Mushroom Pasta
            pstmt.setInt(1, 1); 
            pstmt.setString(2, "Truffle Mushroom Pasta");
            pstmt.setString(3, "Penne, Cream, Truffle Oil");
            pstmt.setString(4, "PENDING");
            pstmt.executeUpdate();
            
            System.out.println("✅ Database populated with test food listings.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insertMarketplaceMission() {
    // Note: status 'AVAILABLE' makes it visible to NGOs in the marketplace
    String sql = "INSERT INTO food_listings (vendorId, foodName, ingredients, status, expiryTime) " +
                 "VALUES (?, ?, ?, 'AVAILABLE', ?)";
    
    try (Connection conn = Database.DatabaseConnection.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
         
        pstmt.setInt(1, 1); // Associated with Vendor ID 1 (SavePlate Cafe)
        pstmt.setString(2, "Premium Pastry Box");
        pstmt.setString(3, "Assorted Croissants and Danishes");
        pstmt.setString(4, "2026-03-01 22:00:00"); // Future expiry for routing logic
        
        pstmt.executeUpdate();
        System.out.println("✅ NGO Marketplace mission injected.");
    } catch (Exception e) {
        e.printStackTrace();
    }
}
    public static void main(String[] args) {
        initialize();
    }
}