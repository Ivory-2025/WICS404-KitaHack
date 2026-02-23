package DAO;

import Database.DatabaseConnection;
import Models.Transaction;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    /**
     * Saves a transaction to the database.
     * Replaces the static nextId with SQLite AUTOINCREMENT.
     */
    public void save(Transaction transaction) {
        String sql = "INSERT INTO transactions (listing_id, ngo_id, transaction_date, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, transaction.getListing().getListingId());
            pstmt.setInt(2, transaction.getNgo().getId());
            pstmt.setString(3, transaction.getTransactionDate().toString());
            pstmt.setString(4, transaction.getStatus());

            pstmt.executeUpdate();

            // Retrieve the auto-generated ID from SQLite
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                transaction.setTransactionId(rs.getInt(1));
            }
            
            System.out.println("DAO: Transaction #" + transaction.getTransactionId() + " saved to SQLite.");

        } catch (SQLException e) {
            System.err.println("Error saving transaction: " + e.getMessage());
        }
    }

    /**
     * Retrieves all transactions from the database.
     */
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactionList = new ArrayList<>();
        String sql = "SELECT * FROM transactions";

        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Transaction t = new Transaction();
                t.setTransactionId(rs.getInt("transaction_id"));
                t.setStatus(rs.getString("status"));
                // You would use FoodListingDAO and NGODAO to populate the full objects here
                transactionList.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactionList;
    }

    /**
     * Finds a specific transaction by ID.
     */
    public Transaction findById(int id) {
        String sql = "SELECT * FROM transactions WHERE transaction_id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Transaction t = new Transaction();
                t.setTransactionId(rs.getInt("transaction_id"));
                return t;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
