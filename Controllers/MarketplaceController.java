package Controllers;

import Services.MarketplaceService;
import Models.User;
import Models.Rating;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
public class MarketplaceController {
    // UI Elements linked via FXML
    @FXML private Label statusLabel;
    @FXML private TextField ratingScoreField;
    @FXML private TextArea commentArea;
    @FXML private Button buyButton;

    private MarketplaceService marketplaceService;

    public MarketplaceController() {
        this.marketplaceService = new MarketplaceService();
    }

    // 1. Flash sale listing - Triggered when the marketplace view loads
    @FXML
    public void initialize() {
        // Example: Logic to populate a ListView or Table would go here
        System.out.println("Marketplace UI Initialized");
    }

    // 2. Buy button - FCFS logic triggered by JavaFX Button
    @FXML
    public void onBuyButtonClicked() {
        // For the demo, we use a placeholder User and Food ID
        User currentUser = new User(); // Replace with actual logged-in user logic
        String foodId = "PROD_001"; 

        boolean success = marketplaceService.processFCFSPurchase(currentUser, foodId);
        
        if (success) {
            statusLabel.setText("Purchase successful! Pick up within 2 hours.");
            marketplaceService.autoExpireListing(foodId, true);
            buyButton.setDisable(true); // FCFS: Disable button after successful purchase
        } else {
            statusLabel.setText("Item no longer available.");
        }
    }

    // 3. Rating submission triggered by a "Submit" Button
    @FXML
    public void onRatingSubmitClicked() {
        try {
            int score = Integer.parseInt(ratingScoreField.getText());
            String comment = commentArea.getText();
            
            // Placeholder users for Demo
            User from = new User(); 
            User to = new User();

            Rating newRating = new Rating(from, to, score, comment);
            // In a real flow, this would call a Service then a DAO to save to SQLite
            
            statusLabel.setText("Rating submitted! Thank you for building trust.");
        } catch (NumberFormatException e) {
            statusLabel.setText("Please enter a valid number for the score.");
        }
    }
}
