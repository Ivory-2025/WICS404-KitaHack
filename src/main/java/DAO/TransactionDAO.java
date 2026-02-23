package DAO;

import java.util.ArrayList;
import java.util.List;

import Models.Transaction;
public class TransactionDAO {
    private static List<Transaction> transactions = new ArrayList<>();
    private static int nextId = 1;

    // Create: Save a transaction and assign an ID
    public void save(Transaction transaction) {
        transaction.setTransactionId(nextId++); // Auto-assign ID
        transactions.add(transaction);
        System.out.println("DAO: Transaction #" + transaction.getTransactionId() + " saved.");
    }

    // Read: Get all transactions to calculate total food saved [cite: 595]
    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions);
    }

    // Read: Find a specific transaction by ID
    public Transaction findById(int id) {
        return transactions.stream()
                .filter(t -> t.getTransactionId() == id)
                .findFirst()
                .orElse(null);
    }
}
