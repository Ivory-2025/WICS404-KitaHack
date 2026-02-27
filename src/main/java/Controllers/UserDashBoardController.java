package Controllers;
import javafx.scene.layout.Region;
import Models.FoodListing;
import Models.User;
import Models.UserSession;
import DAO.FoodListingDAO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserDashBoardController {

    @FXML private Label userNameLabel;
    @FXML private HBox flashSaleContainer;
    @FXML private VBox mainFoodContainer;
    @FXML private MenuButton profileMenu;

    private final FoodListingDAO dao = new FoodListingDAO();
    private List<FoodListing> allCachedItems;
    @FXML
    public void initialize() {
        // Ensure the session is valid before loading
        User currentUser = UserSession.getInstance().getUser();
        if (currentUser != null) {
            userNameLabel.setText("Hello, " + currentUser.getName() + "!");
            profileMenu.setText(currentUser.getName());
        }
        
        setupProfileMenu();
        loadDashboardData();
    }

    private void setupProfileMenu() {
        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setStyle("-fx-text-fill: #F43F5E; -fx-font-weight: bold;");
        logoutItem.setOnAction(e -> handleLogout());
        
        profileMenu.getItems().clear();
        profileMenu.getItems().add(logoutItem);
    }

    private void handleLogout() {
        UserSession.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Login.fxml"));
            Stage stage = (Stage) profileMenu.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadDashboardData() {
        // Essential: Clear containers before loading to prevent duplicate UI elements
        flashSaleContainer.getChildren().clear();
        mainFoodContainer.getChildren().clear();

        allCachedItems = dao.getAvailableListings();
        displayItems(allCachedItems);
    // TEMPORARY DEBUG: Force a card to show
    FoodListing testItem = new FoodListing();
    testItem.setFoodName("Test Burger");
    testItem.setPrice(5.99);
    flashSaleContainer.getChildren().add(createFlashSaleCard(testItem));
        List<FoodListing> allItems = dao.getAvailableListings();

        if (allItems == null || allItems.isEmpty()) {
            Label emptyMsg = new Label("No food items available at the moment.");
            emptyMsg.setStyle("-fx-text-fill: #94A3B8; -fx-italic: true;");
            mainFoodContainer.getChildren().add(emptyMsg);
            return;
        }

        for (FoodListing item : allItems) {
            if (item.isForSale()) {
                flashSaleContainer.getChildren().add(createFlashSaleCard(item));
            } else {
                mainFoodContainer.getChildren().add(createRegularRow(item));
            }
        }
    }

    private VBox createFlashSaleCard(FoodListing item) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMinWidth(220);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 20; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(244,63,94,0.1), 15, 0, 0, 5); -fx-cursor: hand;");

        Label badge = new Label("FLASH SALE");
        badge.setStyle("-fx-text-fill: #F43F5E; -fx-font-weight: 800; -fx-font-size: 10px;");

        Label name = new Label(item.getFoodName());
        name.setStyle("-fx-font-weight: 700; -fx-font-size: 16px;");

        Label price = new Label(String.format("RM %.2f", item.getPrice()));
        price.setStyle("-fx-text-fill: #10B981; -fx-font-weight: 900;");

        card.getChildren().addAll(badge, name, price);
        
        // FIX: Ensure this points to the window method, not the old handlePurchase
        card.setOnMouseClicked(e -> showTransactionWindow(item));
        return card;
    }

    private HBox createRegularRow(FoodListing item) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        VBox info = new VBox(5);
        Label name = new Label(item.getFoodName());
        name.setStyle("-fx-font-weight: 800; -fx-font-size: 18px; -fx-text-fill: #1E293B;");
        
        Label ingredients = new Label(item.getIngredients() != null ? item.getIngredients() : "Daily Surplus");
        ingredients.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px;");
        
        info.getChildren().addAll(name, ingredients);
        HBox.setHgrow(info, Priority.ALWAYS);

        Button purchaseBtn = new Button("PURCHASE");
        purchaseBtn.setStyle("-fx-background-color: #c8c8e7; -fx-text-fill: white; -fx-font-weight: 900; " +
                             "-fx-padding: 10 25; -fx-background-radius: 30; -fx-cursor: hand;");

        purchaseBtn.setOnAction(e -> showTransactionWindow(item));

        row.getChildren().addAll(info, purchaseBtn);
        return row;
    }

    private void showTransactionWindow(FoodListing item) {
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Premium Checkout Experience");
    
    // 1. Force a larger, high-class window size
    DialogPane dialogPane = dialog.getDialogPane();
    dialogPane.setPrefSize(700, 550); // Larger width and height for a "big screen" feel
    dialogPane.setStyle("-fx-background-color: #ffffff; -fx-font-family: 'Segoe UI', system-ui;");
    
    // 2. Cinematic Header Section
    VBox header = new VBox(10);
    header.setAlignment(Pos.CENTER);
    header.setPadding(new Insets(40, 0, 20, 0)); // Generous top padding
    header.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");
    
    Label title = new Label("SECURE CHECKOUT");
    title.setStyle("-fx-font-size: 28px; -fx-font-weight: 900; -fx-text-fill: #0F172A; -fx-letter-spacing: 2px;");
    
    Label subtitle = new Label("Ref No: #" + System.currentTimeMillis() % 100000);
    subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");
    
    header.getChildren().addAll(title, subtitle);
    dialogPane.setHeader(header);

    // 3. High-Class Form Layout (Single Column, Center Focused)
    VBox contentContainer = new VBox(25);
    contentContainer.setAlignment(Pos.TOP_CENTER);
    contentContainer.setPadding(new Insets(30, 100, 30, 100)); // Large side margins to focus the center

    // Order Summary Card
    HBox summaryCard = new HBox();
    summaryCard.setAlignment(Pos.CENTER_LEFT);
    summaryCard.setStyle("-fx-background-color: #F1F5F9; -fx-padding: 15; -fx-background-radius: 12;");
    
    VBox itemInfo = new VBox(5);
    Label itemName = new Label(item.getFoodName());
    itemName.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
    Label itemPrice = new Label("Unit Price: RM " + String.format("%.2f", item.getPrice()));
    itemPrice.setStyle("-fx-text-fill: #64748B;");
    itemInfo.getChildren().addAll(itemName, itemPrice);
    
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    
    Label totalLabel = new Label("RM " + String.format("%.2f", item.getPrice()));
    totalLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: #10B981;");
    
    summaryCard.getChildren().addAll(itemInfo, spacer, totalLabel);

    // Modern Form Fields
    String inputStyle = "-fx-background-color: #FFFFFF; -fx-border-color: #CBD5E1; -fx-border-radius: 8; -fx-padding: 12; -fx-font-size: 14px;";
    String labelStyle = "-fx-font-weight: bold; -fx-text-fill: #475569; -fx-font-size: 12px;";

    VBox nameBox = new VBox(8);
    Label nameLabel = new Label("FULL NAME");
    nameLabel.setStyle(labelStyle);
    TextField nameField = new TextField(UserSession.getInstance().getUser() != null ? UserSession.getInstance().getUser().getName() : "");
    nameField.setStyle(inputStyle);
    nameBox.getChildren().addAll(nameLabel, nameField);

    VBox phoneBox = new VBox(8);
    Label phoneLabel = new Label("PHONE NUMBER");
    phoneLabel.setStyle(labelStyle);
    TextField phoneField = new TextField();
    phoneField.setPromptText("e.g. 012-3456789");
    phoneField.setStyle(inputStyle);
    phoneBox.getChildren().addAll(phoneLabel, phoneField);

    VBox payBox = new VBox(8);
    Label payLabel = new Label("PAYMENT GATEWAY");
    payLabel.setStyle(labelStyle);
    ComboBox<String> paymentMethod = new ComboBox<>();
    paymentMethod.getItems().addAll("Online Banking (FPX)", "E-Wallet (TNG/Grab)", "Credit/Debit Card");
    paymentMethod.setValue("Online Banking (FPX)");
    paymentMethod.setMaxWidth(Double.MAX_VALUE);
    paymentMethod.setStyle(inputStyle);
    payBox.getChildren().addAll(payLabel, paymentMethod);

    contentContainer.getChildren().addAll(summaryCard, nameBox, phoneBox, payBox);
    dialogPane.setContent(contentContainer);

    // 4. Premium Button Styling
ButtonType payButtonType = new ButtonType("AUTHORIZE PAYMENT", ButtonBar.ButtonData.OK_DONE);
dialogPane.getButtonTypes().addAll(payButtonType, ButtonType.CANCEL);

// Style the AUTHORIZE (Primary) Button
Button payBtn = (Button) dialogPane.lookupButton(payButtonType);
payBtn.setStyle(
    "-fx-background-color: #0F172A; " + // Deep Navy
    "-fx-text-fill: white; " +
    "-fx-font-weight: 900; " +
    "-fx-padding: 12 30; " +
    "-fx-background-radius: 8; " +
    "-fx-cursor: hand;"
);

// Style the CANCEL (Secondary) Button
Button cancelBtn = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
cancelBtn.setStyle(
    "-fx-background-color: transparent; " + // No background
    "-fx-text-fill: #64748B; " +            // Sophisticated Slate Gray
    "-fx-font-weight: 700; " +
    "-fx-padding: 12 30; " +
    "-fx-cursor: hand; " +
    "-fx-border-color: #E2E8F0; " +         // Subtle border
    "-fx-border-radius: 8; " +
    "-fx-border-width: 1;"
);

// High-Class Hover Effects
payBtn.setOnMouseEntered(e -> payBtn.setStyle(payBtn.getStyle() + "-fx-background-color: #334155;"));
payBtn.setOnMouseExited(e -> payBtn.setStyle(payBtn.getStyle() + "-fx-background-color: #0F172A;"));

cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(cancelBtn.getStyle() + "-fx-background-color: #F8FAFC; -fx-text-fill: #0F172A;"));
cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(cancelBtn.getStyle() + "-fx-background-color: transparent; -fx-text-fill: #64748B;"));

    Optional<ButtonType> result = dialog.showAndWait();
    if (result.isPresent() && result.get() == payButtonType) {
        handlePaymentRedirect(item, paymentMethod.getValue());
    }
}

    private void handlePaymentRedirect(FoodListing item, String method) {
    // 1. Create a custom Stage for the 'Redirecting' experience
    Stage loadingStage = new Stage();
    loadingStage.initStyle(javafx.stage.StageStyle.TRANSPARENT); // High-class frameless look
    
    VBox root = new VBox(20);
    root.setAlignment(Pos.CENTER);
    root.setPadding(new Insets(40));
    root.setStyle("-fx-background-color: white; -fx-background-radius: 20; " +
                  "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 20, 0, 0, 10);");

    // Add a professional loading spinner (ProgressIndicator)
    ProgressIndicator spinner = new ProgressIndicator();
    spinner.setStyle("-fx-progress-color: #6366F1;"); // Match your Indigo theme
    spinner.setPrefSize(50, 50);

    Label title = new Label("Secure Connection");
    title.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #1E293B;");

    Label msg = new Label("Redirecting to " + method + "...");
    msg.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B;");

    Label footer = new Label("Please complete payment in your browser");
    footer.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8; -fx-font-style: italic;");

    root.getChildren().addAll(spinner, title, msg, footer);

    Scene scene = new Scene(root);
    scene.setFill(null); // Allows for rounded corners on the root
    loadingStage.setScene(scene);
    loadingStage.show();

    // 2. Browser Redirection Logic
    try {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(new URI("https://www.paypal.com/checkoutnow"));
        }
    } catch (Exception e) {
        System.err.println("Browser error: " + e.getMessage());
    }

    // 3. Database Update & Auto-Refresh
    Platform.runLater(() -> {
        // We add a slight delay so the user sees the "classy" window before it disappears
        new Thread(() -> {
            try { Thread.sleep(2500); } catch (InterruptedException ignored) {}
            
            Platform.runLater(() -> {
                if (dao.updateStatus(item.getListingId(), "sold")) {
                    loadingStage.close();
                    loadDashboardData(); // Refreshes the unified scroll page
                    
                    // Optional: Show a high-class success notification
                    showSuccessToast("Order Placed Successfully!");
                }
            });
        }).start();
    });
}

// Helper for a high-class success notification
private void showSuccessToast(String message) {
    Alert success = new Alert(Alert.AlertType.NONE);
    success.getDialogPane().getButtonTypes().add(ButtonType.OK);
    success.setTitle("Success");
    success.setContentText(message);
    
    // Style the success alert to be high class
    DialogPane dp = success.getDialogPane();
    dp.setStyle("-fx-background-color: #ECFDF5; -fx-font-family: 'Segoe UI';");
    dp.lookup(".content.label").setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
    
    success.show();
}

private void displayItems(List<FoodListing> items) {
        flashSaleContainer.getChildren().clear();
        mainFoodContainer.getChildren().clear();

        if (items == null || items.isEmpty()) {
            mainFoodContainer.getChildren().add(new Label("No items found in this category."));
            return;
        }

        for (FoodListing item : items) {
            if (item.isForSale()) {
                flashSaleContainer.getChildren().add(createFlashSaleCard(item));
            } else {
                mainFoodContainer.getChildren().add(createRegularRow(item));
            }
        }
    }

}