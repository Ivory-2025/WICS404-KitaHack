package DAO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import Models.Message;
import Database.DatabaseConnection;
import java.sql.Connection;

public class MessageDAO {
    
    public void sendAutoPM(int senderId, int receiverId, String content) {
        // SQL: INSERT INTO messages (sender_id, receiver_id, message_body) VALUES (?, ?, ?)
    }

    public List<Message> getChatHistory(int user1, int user2) {
    List<Message> history = new ArrayList<>();
    // Query to get messages between two specific users in order
    String sql = "SELECT * FROM messages WHERE (sender_id = ? AND receiver_id = ?) " +
                 "OR (sender_id = ? AND receiver_id = ?) ORDER BY timestamp ASC";

    try (Connection conn = DatabaseConnection.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setInt(1, user1);
        pstmt.setInt(2, user2);
        pstmt.setInt(3, user2);
        pstmt.setInt(4, user1);
        
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            Message msg = new Message();
            msg.setSenderId(rs.getInt("sender_id"));
            msg.setReceiverId(rs.getInt("receiver_id"));
            msg.setContent(rs.getString("message_text"));
            // Assuming your DB stores timestamps as strings or longs
            history.add(msg);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return history;
}

public void sendMessage(int senderId, int receiverId, String text) {
    String sql = "INSERT INTO messages (sender_id, receiver_id, message_text, timestamp) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";

    try (Connection conn = DatabaseConnection.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setInt(1, senderId);
        pstmt.setInt(2, receiverId);
        pstmt.setString(3, text);
        pstmt.executeUpdate();
        
    } catch (SQLException e) {
        System.err.println("Database Error: Could not send message - " + e.getMessage());
    }
}
}