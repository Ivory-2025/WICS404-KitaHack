package Controllers;

import DAO.FoodListingDAO;
import Models.FoodListing;
import Models.UserSession;
import Models.Vendor;
import java.io.IOException;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ManageSurplusController {

    @FXML private VBox salesContainer;
    
    private Vendor currentVendor;
    private final FoodListingDAO dao = new FoodListingDAO();

    @FXML
    public void initialize() {
        // Retrieve vendor session
        this.currentVendor = UserSession.getInstance().getVendor();

        if (currentVendor != null) {
            if (salesContainer != null) {
                loadVendorItems();
            }
        } else {
            System.err.println("Error: No vendor session found. Please log in again.");
        }
    }

    private void loadVendorItems() {
    salesContainer.getChildren().clear();
    
    // Manual System Check remains for UI verification
    salesContainer.getChildren().add(createDonationRow("System Check", "Checking UI Connection", "ACCEPTED", 0.00));

    // 1. Fetch from Database
    List<Models.FoodListing> allItems = dao.getAvailableListings(); 
    
    if (currentVendor == null) return;
    int currentVendorId = currentVendor.getUserId();

    if (allItems == null || allItems.isEmpty()) {
        Label emptyLabel = new Label("No pending surplus found in database.");
        emptyLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-style: italic;");
        salesContainer.getChildren().add(emptyLabel);
        return;
    }

    for (Models.FoodListing item : allItems) {
    // Both are now integers, so this comparison finally works
    if (item.getVendorId() == currentVendorId && "available".equalsIgnoreCase(item.getStatus())) {
        salesContainer.getChildren().add(createDonationRow(
            item.getFoodName(), 
            item.getIngredients(), 
            item.getStatus(),
            item.getPrice()
        ));
    }
}
  }

/**
 * UI Generation: Horizontal Donation Row (Includes Price)
 */
private HBox createDonationRow(String foodName, String ingredients, String statusType, double priceValue) {
    HBox card = new HBox(25);
    card.setAlignment(Pos.CENTER_LEFT);
    card.setPadding(new Insets(25));
    card.setStyle("-fx-background-color: white; -fx-background-radius: 25; " +
                  "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 15, 0, 0, 5);");

    StackPane imgPlaceholder = new StackPane();
    imgPlaceholder.setPrefSize(120, 120);
    imgPlaceholder.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 20;");

    VBox details = new VBox(8);
    Label name = new Label(foodName.toUpperCase());
    name.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #1E293B;");
    
    Label desc = new Label("Ingredients: " + ingredients);
    desc.setStyle("-fx-text-fill: #94A3B8; -fx-font-style: italic; -fx-font-weight: 500;");

    // Added Price Label to match your DB details
    Label priceLabel = new Label(String.format("RM %.2f", priceValue));
    priceLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: 800; -fx-font-size: 16px;");
    
    details.getChildren().addAll(name, desc, priceLabel);
    HBox.setHgrow(details, Priority.ALWAYS);

    Label badge = new Label(statusType.toUpperCase());
    boolean isPositive = statusType.equalsIgnoreCase("ACCEPTED") || statusType.equalsIgnoreCase("PENDING");
    String color = isPositive ? "#10B981" : "#EF4444";
    String bg = isPositive ? "#ECFDF5" : "#FEF2F2";
    
    badge.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + color + "; " +
                   "-fx-font-weight: 800; -fx-padding: 8 15; -fx-background-radius: 12;");

    card.getChildren().addAll(imgPlaceholder, details, badge);
    
    card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + "-fx-translate-y: -3;"));
    card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("-fx-translate-y: -3;", "")));

    return card;
}

    /**
     * Event Handling: Mark Item for Sale
     */
    private void handleMarkAsSale(FoodListing listing) {
        TextInputDialog dialog = new TextInputDialog("5.00");
        dialog.setTitle("Flash Sale");
        dialog.setHeaderText("Set Price for " + listing.getFoodName());
        dialog.setContentText("RM:");

        dialog.showAndWait().ifPresent(priceInput -> {
            try {
                double price = Double.parseDouble(priceInput);
                dao.updateToFlashSale(listing.getListingId(), price);
                loadVendorItems(); 
            } catch (NumberFormatException e) {
                System.err.println("Invalid Price");
            }
        });
    }

    /**
     * Navigation: Back to Dashboard
     */
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/VendorDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            root.setOpacity(0);
            stage.getScene().setRoot(root);
            
            FadeTransition ft = new FadeTransition(Duration.millis(400), root);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
            
        } catch (IOException e) {
            System.err.println("Navigation Error: " + e.getMessage());
        }
    }
}