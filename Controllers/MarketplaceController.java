package Controllers;

import Services.MarketplaceService;
import Models.User;
import Models.Rating;
public class MarketplaceController {
    private MarketplaceService marketplaceService;

    public MarketplaceController() {
        this.marketplaceService = new MarketplaceService();
    }

    // 1. Flash sale listing
    public void displayFlashSales() {
        System.out.println("--- Current Flash Sales ---");
        marketplaceService.convertToFlashSale("Premium Sushi Set");
    }

    // 2. Buy button - FCFS
    public void onBuyButtonClicked(User user, String foodId) {
        boolean success = marketplaceService.processFCFSPurchase(user, foodId);
        if (success) {
            System.out.println("Purchase successful! Please pick up within 2 hours.");
            marketplaceService.autoExpireListing(foodId, true);
        }
    }

    // 3. Rating submission
    public void submitRating(User from, User to, int score, String comment) {
        Rating newRating = new Rating(from, to, score, comment);
        
        System.out.println("Rating submitted from " + from.getName() + " to " + to.getName());
        System.out.println("Trust system updated to ensure food quality.");
    }
}
