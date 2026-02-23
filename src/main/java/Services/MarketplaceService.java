package Services;

import Models.User;
public class MarketplaceService {
    // 1. Move undonated food to flash sale
    public void convertToFlashSale(String foodItem) {
        System.out.println("No matching NGO found. Moving " + foodItem + " to Flash Sale.");
    }

    // 2. Handle first-come-first-serve purchase
    public synchronized boolean processFCFSPurchase(User customer, String foodId) {
        // 'synchronized' prevents multiple people from buying the last item at once
        System.out.println("Processing deal for user: " + customer.getName());
        return true; 
    }

    // 3. Expire listing automatically
    public void autoExpireListing(String listingId, boolean isConsumed) {
        if (!isConsumed) {
            System.out.println("Listing " + listingId + " expired. Sending disclaimer for safety.");
        }
    }
}
