package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.stage.Stage;
import Models.Rating;
import Models.User;
import Models.FoodListing;
import Services.MarketplaceService;

import java.io.IOException;

import DAO.RatingDAO;

public class MarketplaceController {
    @FXML private Label statusLabel;
    @FXML private TextField ratingScoreField;
    @FXML private TextArea commentArea;
    @FXML private Button buyButton;

    private MarketplaceService marketplaceService;
    private User currentUser; // To be set via setter after login

    public MarketplaceController() {
        this.marketplaceService = new MarketplaceService();
    }

    /**
     * Sets the current logged-in user. 
     * Essential for tracking who is making the purchase in the transaction table.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        System.out.println("Marketplace UI Initialized");
    }

    /**
     * Handles FCFS purchase using real database IDs.
     */
    @FXML
    public void onBuyButtonClicked() {
        if (currentUser == null) {
            statusLabel.setText("Error: No user session found.");
            return;
        }

        // Use a real integer ID matching your SQLite food_listings table
        int foodId = 1; 

        // Update: processFCFSPurchase now expects an int ID
        boolean success = marketplaceService.processFCFSPurchase(currentUser, foodId);
        
        if (success) {
            statusLabel.setText("Purchase successful! Pick up within 2 hours.");
            marketplaceService.autoExpireListing(foodId, true);
            buyButton.setDisable(true); 
        } else {
            statusLabel.setText("Item no longer available.");
        }
    }

    /**
     * Submits ratings to the SQLite database.
     */
    @FXML
    public void onRatingSubmitClicked() {
        try {
            int score = Integer.parseInt(ratingScoreField.getText());
            String comment = commentArea.getText();
            
            if (currentUser != null) {
                // In a real flow, you would identify the 'to' user (Vendor) from the transaction
                RatingDAO ratingDAO = new RatingDAO();
                // Logic to save rating would go here using ratingDAO.save(...)
                
                statusLabel.setText("Rating submitted! Thank you for building trust.");
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Please enter a valid number (1-5) for the score.");
        }
    }
    public void openNGOMarketplace(ActionEvent event) throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NGODashboard.fxml")); // adjust path
    Parent root = loader.load();
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.setScene(new Scene(root));
    stage.show();
    }
}