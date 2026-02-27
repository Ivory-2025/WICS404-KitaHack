package Controllers;

//** TODO: setup Java FX, FXML, for the codes to work
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.Node;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import Models.*;
import DAO.*;
import java.io.IOException;
import java.awt.Desktop;
import java.net.URI;
import java.util.List;
import javafx.stage.Popup;
import Services.*;
import javafx.util.Duration;
import javafx.scene.layout.Region;

public class NGODashboardController {

    // -----------------------------------------------------------------------
    // FXML — wire these to your .fxml file
    // -----------------------------------------------------------------------

    @FXML private TableView<FoodListing> foodTable;
    @FXML private TextField emailField;
    @FXML private TableColumn<FoodListing, String> colFoodName;
    @FXML private TableColumn<FoodListing, String> colVendorName;
    @FXML private TableColumn<FoodListing, String> colExpiryTime;
    @FXML private TableColumn<FoodListing, String> colStatus;
    @FXML private Label lblRouteSummary;
    @FXML private Button btnAccept;
    @FXML private Button btnViewRoute;
    @FXML private PasswordField passwordField;
    private final DAO.NGODAO ngoDAO = new DAO.NGODAO();

    private final UserService userService = new UserService();
    // -----------------------------------------------------------------------
    // Services
    // -----------------------------------------------------------------------

    private final MatchingService matchingService = new MatchingService();
    private final RoutingService  routingService  = new RoutingService();

    // The currently logged-in NGO — set this via setCurrentNGO() after login
    private NGO currentNGO;

    // -----------------------------------------------------------------------
    // Init
    // -----------------------------------------------------------------------

    @FXML
    public void initialize() {
        // setupTableColumns();

        // // When user selects a row, show the route info automatically
        // foodTable.getSelectionModel().selectedItemProperty().addListener(
        //     (obs, oldVal, selectedListing) -> {
        //         if (selectedListing != null) {
        //             showRouteSummary(selectedListing);
        //         }
        //     }
        // );
        // ONLY run this if the current FXML actually has a table (Marketplace view)
    if (foodTable != null){
        setupTableColumns();
        foodTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, selectedListing) -> {
                if (selectedListing != null) {
                    showRouteSummary(selectedListing);
                }
            }
        );
    }
    }

    /**
     * Call this right after loading the FXML, passing in the logged-in NGO.
     * Example from your login controller:
     *     ngoController.setCurrentNGO(loggedInNGO);
     */

    @FXML private Label welcomeLabel; 

// 2. Keep your setCurrentNGO exactly like this
public void setCurrentNGO(NGO ngo) {
    this.currentNGO = ngo;
    
    // Simple message, no more null name errors!
    if (welcomeLabel != null) {
        welcomeLabel.setText("Welcome back! ✨");
    }
    
    if (foodTable != null) {
        loadMatchedListings();
    }
}

    // -----------------------------------------------------------------------
    // Table setup
    // -----------------------------------------------------------------------

    private void setupTableColumns() {
    // 1. Food Item Column (Bold & Professional)
    colFoodName.setCellValueFactory(new PropertyValueFactory<>("foodName"));
    colFoodName.setCellFactory(column -> new TableCell<>() {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) { setText(null); setStyle(""); }
            else { 
                setText(item); 
                setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-weight: bold; -fx-text-fill: #2d3436; -fx-padding: 10;");
            }
        }
    });

    // 2. Vendor Column (Subtle Blue Text)
    colVendorName.setCellValueFactory(cellData -> 
        new javafx.beans.property.SimpleStringProperty(cellData.getValue().getVendor().getRestaurantName()));
    colVendorName.setCellFactory(column -> new TableCell<>() {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) { setText(null); setStyle(""); }
            else { 
                setText("🏠 " + item); 
                setStyle("-fx-text-fill: #2d3436; -fx-font-style: italic; -fx-padding: 10;");
            }
        }
    });

    // 3. Expiry Column (Clear Red/Orange Warning)
    colExpiryTime.setCellValueFactory(new PropertyValueFactory<>("expiryTimeString"));
    colExpiryTime.setCellFactory(column -> new TableCell<>() {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) { setText(null); setStyle(""); }
            else { 
                setText("⏰ " + item); 
                setStyle("-fx-text-fill: #d63031; -fx-font-weight: bold; -fx-padding: 10;");
            }
        }
    });

    // 4. Status Column (The "Pill" Design)
    colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    colStatus.setCellFactory(column -> new TableCell<>() {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) { setText(null); setStyle(""); }
            else {
                setText(item.toUpperCase());
                if (item.equalsIgnoreCase("available")) {
                    setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-font-weight: bold; " +
                             "-fx-background-radius: 15; -fx-alignment: center; -fx-margin: 5;");
                } else {
                    setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-font-weight: bold; " +
                             "-fx-background-radius: 15; -fx-alignment: center; -fx-margin: 5;");
                }
            }
        }
    });
}

    // -----------------------------------------------------------------------
    // Load food listings that match this NGO
    // -----------------------------------------------------------------------

    private void loadMatchedListings() {
        // if (currentNGO == null) return;

        // List<FoodListing> matched = matchingService.findMatchingListingsForNGO(currentNGO);
        // ObservableList<FoodListing> data = FXCollections.observableArrayList(matched);
        // foodTable.setItems(data);

        // lblRouteSummary.setText("Select a listing to see route info.");
        // System.out.println("Loaded " + matched.size() + " matched listings for NGO: " + currentNGO.getOrganizationName());
        if (currentNGO == null || foodTable == null) return; // Add 'foodTable == null' check

    List<FoodListing> matched = matchingService.findMatchingListingsForNGO(currentNGO);
    ObservableList<FoodListing> data = FXCollections.observableArrayList(matched);
    foodTable.setItems(data);

    if (lblRouteSummary != null) {
        lblRouteSummary.setText("Select a listing to see route info.");
    }
    }

    // -----------------------------------------------------------------------
    // Accept button — first-come-first-serve
    // -----------------------------------------------------------------------

    @FXML
private void handleAccept() {
    FoodListing selected = foodTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
        showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a food listing first.");
        return;
    }

    String timeLeft = selected.getTimeUntilExpiry();
    Dialog<ButtonType> dialog = new Dialog<>();
    
    // Remove the "ugly" Windows top bar for a high-class feel
    dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);

    // Root Container
    VBox root = new VBox(20);
    root.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
    root.getStyleClass().add("custom-dialog-container");
    root.setPadding(new Insets(40));
    root.setAlignment(Pos.CENTER);

    // Icon and High-Class Header
    Label icon = new Label("✨");
    icon.setStyle("-fx-font-size: 45px;");
    
    Label title = new Label("Mission Briefing");
    title.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #0F172A;");

    // Your specific message logic
    VBox messageBox = new VBox(10);
    messageBox.setAlignment(Pos.CENTER);
    
    Label mainMsg = new Label("Awesome choice! You're rescuing " + selected.getFoodName());
    mainMsg.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: #10B981; -fx-text-alignment: CENTER;");
    mainMsg.setWrapText(true);

    Label expiryMsg = new Label("Just a heads-up: it expires in about " + timeLeft + ".");
    expiryMsg.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B; -fx-font-weight: 600;");

    root.getChildren().addAll(icon, title, messageBox, mainMsg, expiryMsg);
    dialog.getDialogPane().setContent(root);

    // Modern Button Types
    ButtonType claimBtnType = new ButtonType("GOT IT, CLAIM! ⚡", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelBtnType = new ButtonType("CANCEL", ButtonBar.ButtonData.CANCEL_CLOSE);
    dialog.getDialogPane().getButtonTypes().addAll(claimBtnType, cancelBtnType);

    // Apply high-class styling to the buttons after they are created
    Button claimBtn = (Button) dialog.getDialogPane().lookupButton(claimBtnType);
    claimBtn.getStyleClass().add("gen-z-button");
    claimBtn.getStyleClass().add("claim-btn-premium");

    dialog.showAndWait().ifPresent(response -> {
        if (response == claimBtnType) {
            boolean success = matchingService.acceptListing(selected.getListingId(), currentNGO);
            if (success) {
                // High-class success toast instead of standard alert
                showToast((Stage)foodTable.getScene().getWindow(), "Mission Secured! ⚡", true);
            } else {
                showAlert(Alert.AlertType.ERROR, "Too Slow!", "Sorry! Another NGO already accepted this listing.");
            }
            loadMatchedListings(); 
        }
    });
}

    @FXML
    private void handleViewRoute() {
        FoodListing selected = foodTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a food listing first.");
            return;
        }

        String mapsLink = routingService.generateGoogleMapsLink(selected, currentNGO);
        System.out.println("Opening Google Maps: " + mapsLink);

        try {
            Desktop.getDesktop().browse(new URI(mapsLink));
        } catch (Exception e) {
            // If browser won't open, just show the link in the label
            lblRouteSummary.setText("Open this link manually:\n" + mapsLink);
            System.out.println("Could not open browser: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Auto-show route summary when a listing is selected in the table
    // -----------------------------------------------------------------------

    private void showRouteSummary(FoodListing listing) {
        if (currentNGO == null) return;
        String summary = routingService.getRouteSummary(listing, currentNGO);
        lblRouteSummary.setText(summary);
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
public void handleLogout() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Login.fxml"));
        javafx.scene.Parent root = loader.load();
        javafx.stage.Stage stage = (Stage) foodTable.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("SavePlate: Login");
    } catch (java.io.IOException e) {
        e.printStackTrace();
    }
}
@FXML
private void goToMarketplace(javafx.scene.input.MouseEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/NGODashboard.fxml"));
        Parent root = loader.load();
        
        NGODashboardController controller = loader.getController();
        
        // Use UserSession as the single source of truth to avoid null NGO errors
        NGO sessionNGO = Models.UserSession.getInstance().getNGO();
        controller.setCurrentNGO(sessionNGO != null ? sessionNGO : this.currentNGO);
        
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        
        // 🔥 ADDED: High-class Fade-in
        root.setOpacity(0);
        javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(600), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        stage.setScene(new Scene(root));
        stage.setTitle("SavePlate - NGO Marketplace");
    } catch (java.io.IOException e) {
        e.printStackTrace();
        // Use your 4-String toast signature for errors
        showToast("Error: Could not load Marketplace. 🚫", "#FEE2E2", "#991B1B", "⚠️");
    }
}

@FXML
private void handleViewRating() {
    DAO.RatingDAO ratingDAO = new DAO.RatingDAO();
    List<Models.VendorRatingSummary> ratingsList = ratingDAO.getVendorLeaderboard();

    Stage stage = new Stage();
    VBox root = new VBox(0); 
    root.setStyle("-fx-background-color: #FFFFFF;"); 

    // --- NEW: TOP NAVIGATION BAR ---
    HBox navBar = new HBox();
    navBar.setPadding(new Insets(20, 50, 0, 50));
    navBar.setAlignment(Pos.CENTER_LEFT);
    
    Button backBtn = new Button("←  Dashboard");
    backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8; " + 
                     "-fx-font-weight: 700; -fx-font-size: 14px; -fx-cursor: hand;");
    
    // Smooth hover for back button
    backBtn.setOnMouseEntered(e -> backBtn.setStyle(backBtn.getStyle() + "-fx-text-fill: #1E293B;"));
    backBtn.setOnMouseExited(e -> backBtn.setStyle(backBtn.getStyle().replace("-fx-text-fill: #1E293B;", "")));
    
    // Close the current leaderboard stage to "return" to the dashboard
    backBtn.setOnAction(e -> {
    Stage currentStage = (Stage) backBtn.getScene().getWindow();
    
    // Create a smooth 300ms fade-out transition
    javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(
        javafx.util.Duration.millis(300), 
        backBtn.getScene().getRoot()
    );
    fadeOut.setFromValue(1.0);
    fadeOut.setToValue(0.0);
    
    // Close the stage only AFTER the animation finishes
    fadeOut.setOnFinished(event -> currentStage.close());
    fadeOut.play();
}); 
    
    navBar.getChildren().add(backBtn);

    // --- EXISTING HEADER ---
    VBox header = new VBox(8);
    header.setPadding(new Insets(30, 50, 40, 50)); // Adjusted padding for navBar
    header.setAlignment(Pos.CENTER_LEFT);
    
    Label title = new Label("Food Hero Leaderboard");
    title.setStyle("-fx-font-size: 36px; -fx-font-weight: 900; -fx-text-fill: #1E293B; -fx-letter-spacing: -0.5px;");
    
    Label subtitle = new Label("The most impactful contributors in your area.");
    subtitle.setStyle("-fx-font-size: 15px; -fx-text-fill: #94A3B8; -fx-font-weight: 500;");
    
    Region line = new Region();
    line.setPrefHeight(1);
    line.setStyle("-fx-background-color: #F1F5F9; -fx-max-width: 100;");
    
    header.getChildren().addAll(title, subtitle, line);

    // --- SCROLLABLE CONTENT ---
    VBox listContainer = new VBox(12);
    listContainer.setPadding(new Insets(10, 50, 40, 50));

    ScrollPane scrollPane = new ScrollPane(listContainer);
    scrollPane.setFitToWidth(true);
    scrollPane.setPrefHeight(600);
    scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");

    int rank = 1;
    for (Models.VendorRatingSummary summary : ratingsList) {
        listContainer.getChildren().add(createMinimalistCard(summary, rank++));
    }

    // Adding the navBar at the very top
    root.getChildren().addAll(navBar, header, scrollPane);

    Scene scene = new Scene(root, 750, 850); // Slightly taller to accommodate nav
    stage.setScene(scene);
    stage.setTitle("SavePlate Elite");
    stage.show();
}

private HBox createMinimalistCard(Models.VendorRatingSummary summary, int rank) {
    HBox card = new HBox(20);
    card.setAlignment(Pos.CENTER_LEFT);
    card.setPadding(new Insets(20, 30, 20, 30));
    
    // MINIMALIST DESIGN: White on White with a "Cloud" shadow
    String baseStyle = "-fx-background-color: #FFFFFF; " +
                       "-fx-background-radius: 20; " +
                       "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.03), 20, 0, 0, 10); " +
                       "-fx-border-color: #F8FAFC; -fx-border-width: 1; -fx-border-radius: 20;";
    card.setStyle(baseStyle);

    // 1. ELEGANT RANK
    Label rankLabel = new Label(String.format("%02d", rank));
    rankLabel.setStyle("-fx-font-weight: 900; -fx-font-size: 18px; -fx-text-fill: #000000; -fx-min-width: 40;");

    // 2. SOPHISTICATED AVATAR (Oppa Style)
    Label avatar = new Label(summary.getVendorName().substring(0, 1).toUpperCase());
    avatar.setStyle("-fx-background-color: #F8FAFC; -fx-text-fill: #64748B; " +
                    "-fx-min-width: 50; -fx-min-height: 50; -fx-background-radius: 15; " +
                    "-fx-alignment: center; -fx-font-weight: 800; -fx-font-size: 18px; " +
                    "-fx-border-color: #F1F5F9; -fx-border-width: 1; -fx-border-radius: 15;");

    // 3. VENDOR INFO
    VBox details = new VBox(2);
    Label name = new Label(summary.getVendorName());
    name.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #1E293B;");
    
    Label badge = new Label(rank == 1 ? "PREMIUM PARTNER" : "VERIFIED");
    badge.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 10px; -fx-font-weight: 800; -fx-letter-spacing: 1px;");
    details.getChildren().addAll(name, badge);

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    // 4. CHAMPAGNE GOLD RATING
    HBox starChip = new HBox(5);
    starChip.setAlignment(Pos.CENTER);
    starChip.setPadding(new Insets(8, 15, 8, 15));
    starChip.setStyle("-fx-background-color: #FFFDF5; -fx-background-radius: 12;");

    Label starIcon = new Label("★");
    starIcon.setStyle("-fx-text-fill: #EAB308; -fx-font-size: 16px;");
    
    Label score = new Label(summary.getStars());
    score.setStyle("-fx-font-weight: 900; -fx-text-fill: #854D0E; -fx-font-size: 16px;");
    starChip.getChildren().addAll(starIcon, score);

    card.getChildren().addAll(rankLabel, avatar, details, spacer, starChip);

    // SMOOTH INTERACTION
    card.setOnMouseEntered(e -> {
        card.setStyle(baseStyle + "-fx-background-color: #FBFCFE; -fx-translate-y: -3;");
        card.setEffect(new javafx.scene.effect.DropShadow(30, 0, 15, Color.web("rgba(0,0,0,0.06)")));
    });
    card.setOnMouseExited(e -> {
        card.setStyle(baseStyle);
        card.setEffect(new javafx.scene.effect.DropShadow(20, 0, 10, Color.web("rgba(0,0,0,0.03)")));
        card.setTranslateY(0);
    });

    return card;
}

@FXML
public void handleLogout(ActionEvent event) { // Added ActionEvent parameter for the Button
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Login.fxml"));
        javafx.scene.Parent root = loader.load();
        
        // Get the stage from the ActionEvent source (the Logout button)
        javafx.stage.Stage stage = (javafx.stage.Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("SavePlate: Login");
    } catch (java.io.IOException e) {
        e.printStackTrace();
    }
}
public void openNGOMarketplace(ActionEvent event) throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NGODashboard.fxml")); // adjust path if your FXML is elsewhere
    Parent root = loader.load();
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.setScene(new Scene(root));
    stage.show();
}

@FXML
private void handleBackToDashboard(ActionEvent event) {
    // Navigate to the main NGO entry point
    // Make sure the path matches your actual file name (e.g., NGOMainDashboard.fxml)
    loadDashboard("/Views/NGOMainDashboard.fxml", event); 
}

// A helper to handle the scene switching logic
private void loadDashboard(String fxmlPath, ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        
        Object controller = loader.getController();
        
        // Pass data based on the controller type
        if (controller instanceof NGODashboardController) {
             ((NGODashboardController) controller).setCurrentNGO(Models.UserSession.getInstance().getNGO());
        } else if (controller instanceof VendorDashboardController) {
             ((VendorDashboardController) controller).setCurrentVendor(Models.UserSession.getInstance().getVendor());
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    } catch (IOException e) {
        System.err.println("Error loading dashboard: " + e.getMessage());
    }
}

@FXML
public void handleLogin(ActionEvent event) {
    // 1. Clear old session data
    Models.UserSession.getInstance().logout(); 

    String email = emailField.getText().trim();
    String password = passwordField.getText();

    Models.User loggedInUser = userService.login(email, password);

    if (loggedInUser == null) {
        Stage stage = (Stage) emailField.getScene().getWindow();
        showToast(stage, "Login Failed: Invalid credentials. ❌", false);
        return;
    }
    
    // 2. Save User to Session
    Models.UserSession.getInstance().setUser(loggedInUser);

    // 3. Switch Screen based on Role
    if ("VENDOR".equalsIgnoreCase(loggedInUser.getRole())) {
        loadDashboard("/Views/VendorDashboard.fxml", event);
    } // ✅ REPAIRED: NGO ROLE BLOCK
else if ("NGO".equalsIgnoreCase(loggedInUser.getRole())) {
NGO ngo = ngoDAO.getNGOByUserId(loggedInUser.getUserId());
    
    if (ngo == null) {
        // Use the 3-parameter toast signature you defined for errors
        Stage stage = (Stage) emailField.getScene().getWindow();
        showToast(stage, "Error: NGO profile not found in DB. ❌", false);
        return;
    }

    UserSession.getInstance().setNGO(ngo);
    
    // Correctly call the helper with the 'event' parameter
    loadDashboard("/Views/NGOMainDashboard.fxml", event);
}
}


public void showToast(Stage stage, String message, boolean isSuccess) {
    Label label = new Label(message);
    
    // High-class "Glassmorphism" styling
    label.setStyle("-fx-background-color: " + (isSuccess ? "#10B981" : "#EF4444") + ";"
            + "-fx-text-fill: white; -fx-padding: 15 30; -fx-background-radius: 25;"
            + "-fx-font-weight: 800; -fx-font-size: 14px;"
            + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);");

    Popup popup = new Popup();
    popup.getContent().add(label);
    
    // Position it at the top-center of the app window
    popup.show(stage);
    popup.setX(stage.getX() + (stage.getWidth() / 2) - (label.getLayoutBounds().getWidth() / 2));
    popup.setY(stage.getY() + 80); 

    // Smooth fade-out animation
    javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(2500), label);
    fadeOut.setFromValue(1.0);
    fadeOut.setToValue(0.0);
    fadeOut.setOnFinished(e -> popup.hide());
    fadeOut.play();
}

private void showToast(String message, String bgColor, String textColor, String icon) {
    // Safety check to ensure the UI is loaded
    if (foodTable == null || foodTable.getScene() == null) return;

    Stage stage = (Stage) foodTable.getScene().getWindow();
    Popup popup = new Popup();

    HBox toastRoot = new HBox(12);
    toastRoot.setAlignment(Pos.CENTER_LEFT);
    toastRoot.setStyle(String.format(
            "-fx-background-color: %s; -fx-background-radius: 20; -fx-padding: 12 25;", bgColor));

    Label iconLabel = new Label(icon);
    Label messageLabel = new Label(message);
    messageLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-weight: 800;", textColor));

    toastRoot.getChildren().addAll(iconLabel, messageLabel);
    popup.getContent().add(toastRoot);
    popup.show(stage);

    // Fade-out logic
    FadeTransition out = new FadeTransition(Duration.millis(400), toastRoot);
    out.setDelay(Duration.seconds(2.5));
    out.setFromValue(1);
    out.setToValue(0);
    out.setOnFinished(e -> popup.hide());
    out.play();
}
@FXML
private void goToChat(javafx.scene.input.MouseEvent event) {
    try {
        // Make sure the path matches your actual FXML location
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/Views/NGOChatLanding.fxml"));
        javafx.scene.Parent root = loader.load();
        
        // Get the current stage from the source of the click
        javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
    } catch (java.io.IOException e) {
        System.err.println("Error loading NGO Chat: " + e.getMessage());
        e.printStackTrace();
    }
}

@FXML
private void goToRatings(javafx.scene.input.MouseEvent event) {
    // This calls the aesthetic leaderboard method we just built
    handleViewRating();
}

@FXML
private void handleHoverEnter(javafx.scene.input.MouseEvent event) {
    ((VBox)event.getSource()).setScaleX(1.05);
    ((VBox)event.getSource()).setScaleY(1.05);
}

@FXML
private void handleHoverExit(javafx.scene.input.MouseEvent event) {
    ((VBox)event.getSource()).setScaleX(1.0);
    ((VBox)event.getSource()).setScaleY(1.0);
}
}
