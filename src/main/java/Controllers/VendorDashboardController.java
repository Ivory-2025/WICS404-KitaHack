package Controllers;
import Models.Vendor;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

import DAO.FoodListingDAO;

public class VendorDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private VBox uploadCard;
    @FXML private VBox ordersCard;
    @FXML private VBox manageSalesCard;
    private Vendor currentVendor;
    @FXML private VBox chatCard;
    private void showToast(String message, boolean success) {
    // Reuse the toast logic we built for the NGO side
    System.out.println((success ? "SUCCESS: " : "ERROR: ") + message);
    // You can implement the actual Popup toast here as we did previously
}

private void loadVendorListings() {
    // This logic should refresh your UI container where listings are shown.
    // Since this is the Dashboard, you might need to refresh a specific VBox.
    System.out.println("Refreshing listings UI...");
}

    public void setCurrentVendor(Vendor vendor) {
    if (vendor == null) {
        System.err.println("Error: Vendor object is null!");
        return;
    }
    this.currentVendor = vendor;
    welcomeLabel.setText("Welcome back, " + currentVendor.getName() + "!");
    System.out.println("Vendor set: " + currentVendor.getName());
}

    @FXML
    public void initialize() {
    setupCardInteractions(manageSalesCard);
        setupCardInteractions(uploadCard);
        setupCardInteractions(ordersCard);
        Models.Vendor current = Models.UserSession.getInstance().getVendor();
    if (current != null) {
        welcomeLabel.setText("Welcome back, " + current.getRestaurantName() + "!");
    } else {
        welcomeLabel.setText("Welcome back, Guest!");
    }
    }

    private void setupCardInteractions(VBox card) {
        if (card == null) return;

        ScaleTransition st = new ScaleTransition(Duration.millis(200), card);

        card.setOnMouseEntered(e -> {
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });

        card.setOnMouseExited(e -> {
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    // ===================== Navigation =====================

    @FXML
    private void handleUploadFoodSurplus(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/UploadFood.fxml"));
            Parent root = loader.load();

            // ✅ Pass vendor session
            UploadFoodController controller = loader.getController();
            controller.setVendor(currentVendor);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();

        } catch (IOException e) {
            System.err.println("Error loading UploadFood.fxml");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewOrders(MouseEvent event) {
        switchScreen(event, "/Views/ViewOrders.fxml");
    }

    @FXML
private void handleLogout(javafx.event.ActionEvent event) {
    // 1. Clear the session so the next user starts fresh
    Models.UserSession.getInstance().logout(); 

    try {
        // 2. Load the Login view
        Parent root = FXMLLoader.load(getClass().getResource("/Views/Login.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // 3. Keep your signature high-class fade transition
        root.setOpacity(0);
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(Duration.millis(500), root);
        ft.setFromValue(0);
        ft.setToValue(1);

        stage.setScene(new Scene(root));
        stage.centerOnScreen();
        ft.play();
        
        System.out.println("✅ Session cleared and user logged out.");
    } catch (IOException e) {
        System.err.println("Error: Could not load Login.fxml");
        e.printStackTrace();
    }
}

    private void switchScreen(MouseEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            root.setOpacity(0);
            javafx.animation.FadeTransition ft =
                    new javafx.animation.FadeTransition(Duration.millis(500), root);
            ft.setFromValue(0);
            ft.setToValue(1);

            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            ft.play();

        } catch (IOException e) {
            System.err.println("Error: Could not load " + fxmlPath);
            e.printStackTrace();
        }
    }

    @FXML
private void handleGoToChats(MouseEvent event) {
    try {
        // Load the new Chat Landing FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/ChatLanding.fxml"));
        Parent root = loader.load();
        
        // Switch the scene
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    } catch (IOException e) {
        System.err.println("Error navigating to Chats: " + e.getMessage());
    }
}

private void handlePurchase(Models.FoodListing listing) {
    // 1. Instant check to prevent double-booking
    FoodListingDAO dao = new FoodListingDAO();
    Models.FoodListing freshData = dao.getListingById(listing.getListingId());

    if (freshData == null || !"FLASH_SALE".equals(freshData.getStatus())) {
        showToast("Sorry! This item was just purchased by someone else.", false);
        return;
    }

    // 2. Mock Online Payment (High Class Popup)
    TextInputDialog payment = new TextInputDialog();
    payment.setTitle("Secure Checkout");
    payment.setHeaderText("Purchasing " + freshData.getFoodName());
    payment.setContentText("Enter Card CVV to confirm RM" + freshData.getPrice() + ":");

    payment.showAndWait().ifPresent(cvv -> {
        // 3. Update status to SOLD in database
        dao.updateStatus(freshData.getListingId(), "SOLD");
        showToast("Purchase Complete! Pick up at " + freshData.getVendor().getRestaurantName(), true);
        
        // Refresh your UI list here
    });
}

@FXML
private void handleMarkAsSale(Models.FoodListing listing) {
    // 1. High-Class "Minimalist Luxury" Dialog
    TextInputDialog dialog = new TextInputDialog("5.00");
    dialog.setTitle("Flash Sale");
    dialog.setHeaderText("Convert '" + listing.getFoodName() + "' to Surplus Sale");
    dialog.setContentText("Enter Discounted Price (RM):");

    dialog.showAndWait().ifPresent(priceInput -> {
        try {
            double price = Double.parseDouble(priceInput);
            
            // 2. Database Update Logic
            DAO.FoodListingDAO dao = new DAO.FoodListingDAO();
            dao.updateToFlashSale(listing.getListingId(), price);
            
            // 3. UI Feedback
            showToast("Item is now live for purchase!", true);
            
            // 4. Refresh the dashboard to update status badges
            // Implement your specific refresh logic here (e.g., loadVendorListings())
        } catch (NumberFormatException e) {
            showToast("Please enter a valid numeric price.", false);
        }
    });
}

private VBox createFlashSaleCard(Models.FoodListing listing) {
    VBox card = new VBox(15);
    card.setPadding(new Insets(25));
    // High-Class Aesthetic: Pure white, deep soft shadow, and subtle border
    card.setStyle("-fx-background-color: white; " +
                  "-fx-background-radius: 30; " +
                  "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 20, 0, 0, 10); " +
                  "-fx-border-color: #F1F5F9; -fx-border-width: 1; -fx-border-radius: 30;");

    // 1. Header: Elegant Flash Sale Badge
    HBox badgeBox = new HBox();
    Label saleBadge = new Label("FLASH SALE");
    saleBadge.setStyle("-fx-background-color: #FEF2F2; -fx-text-fill: #EF4444; " +
                       "-fx-font-size: 10px; -fx-font-weight: 900; -fx-padding: 4 12; " +
                       "-fx-background-radius: 10; -fx-letter-spacing: 1px;");
    badgeBox.getChildren().add(saleBadge);

    // 2. Food Details (from your mapResultSet logic)
    VBox details = new VBox(5);
    Label foodName = new Label(listing.getFoodName().toUpperCase());
    foodName.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #1E293B;");
    
    Label vendorName = new Label("by " + listing.getVendor().getRestaurantName());
    vendorName.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8; -fx-font-weight: 600;");
    details.getChildren().addAll(foodName, vendorName);

    // 3. Pricing Section (Using the price column you added to DAO)
    HBox priceContainer = new HBox(12);
    priceContainer.setAlignment(Pos.BASELINE_LEFT);
    
    Label currency = new Label("RM");
    currency.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: #10B981;");
    
    // Formats the double price from your updateToFlashSale method
    Label priceLabel = new Label(String.format("%.2f", listing.getPrice()));
    priceLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: 900; -fx-text-fill: #10B981; -fx-letter-spacing: -1px;");
    
    priceContainer.getChildren().addAll(currency, priceLabel);

    // 4. Interactive "Buy Now" Button
    Button buyBtn = new Button("Secure Order");
    buyBtn.setMaxWidth(Double.MAX_VALUE); // Full width button
    buyBtn.setCursor(javafx.scene.Cursor.HAND);
    buyBtn.setStyle("-fx-background-color: #1E293B; -fx-text-fill: white; " +
                    "-fx-font-weight: 800; -fx-font-size: 14px; -fx-background-radius: 15; " +
                    "-fx-padding: 15;");

    // Connect to payment logic (First-Come, First-Served)
    buyBtn.setOnAction(e -> handlePurchase(listing));

    // Hover Animation for "High Class" feel
    card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + "-fx-translate-y: -5; -fx-border-color: #CBD5E1;"));
    card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("-fx-translate-y: -5; -fx-border-color: #CBD5E1;", "")));

    card.getChildren().addAll(badgeBox, details, priceContainer, buyBtn);
    return card;
}

@FXML
private void handleGoToManageSales(MouseEvent event) {
    try {
        // Ensure the path matches your project structure EXACTLY (Case Sensitive!)
        java.net.URL fxmlLocation = getClass().getResource("/Views/ManageSurplus.fxml");
        
        if (fxmlLocation == null) {
            throw new IOException("FXML file not found! Check if ManageSurplus.fxml is in src/main/resources/Views/");
        }

        Parent root = FXMLLoader.load(fxmlLocation);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    } catch (IOException e) {
        System.err.println("Navigation Error: " + e.getMessage());
        e.printStackTrace();
    }
}
}