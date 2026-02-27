package Services;

import DAO.FoodListingDAO;
import DAO.TransactionDAO;
import Models.FoodListing;
import Models.Transaction;
import Models.User;
import Models.Vendor;
import java.time.LocalDateTime;

public class MarketplaceService {
    private final FoodListingDAO listingDAO = new FoodListingDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    /**
     * 1. Move undonated food to flash sale.
     * Updates the status in the database so it appears in the student marketplace.
     */
    public void convertToFlashSale(int listingId) {
        // Update status to 'flash_sale' in SQLite
        boolean success = listingDAO.updateStatus(listingId, "flash_sale");
        if (success) {
            System.out.println("Listing #" + listingId + " moved to Flash Sale.");
        }
    }

    /**
     * 2. Handle first-come-first-serve purchase.
     * Uses 'synchronized' to prevent race conditions during the claim process.
     */
    public synchronized boolean processFCFSPurchase(User customer, int listingId) {
        // 1. Verify item is still available
        FoodListing listing = listingDAO.getListingById(listingId);
        
        if (listing != null && (listing.getStatus().equals("flash_sale") || listing.getStatus().equals("available"))) {
            // 2. Mark as matched/sold to prevent double-buying
            listingDAO.updateStatus(listingId, "sold");

            // 3. Create and save the transaction record
            Transaction transaction = new Transaction();
            transaction.setBuyer(customer);
            transaction.setListing(listing);
            transaction.setVendor(listing.getVendor());
            transaction.setTransactionTime(LocalDateTime.now());
            transaction.setStatus("COMPLETED");
            
            transactionDAO.save(transaction);
            
            System.out.println("Processing deal for user: " + customer.getName());
            return true;
        }
        return false; 
    }

    /**
     * 3. Expire listing automatically.
     */
    public void autoExpireListing(int listingId, boolean isConsumed) {
        if (!isConsumed) {
            listingDAO.updateStatus(listingId, "expired");
            System.out.println("Listing " + listingId + " expired. Sending disclaimer for safety.");
        }
    }
}