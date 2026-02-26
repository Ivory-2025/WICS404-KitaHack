package Controllers;

//** TODO: setup Java FX, FXML, for the codes to work
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
import Models.FoodListing;
import Models.NGO;
import Models.User;
import Models.UserSession;
import DAO.*;
import java.io.IOException;
import java.awt.Desktop;
import java.net.URI;
import java.util.List;
import javafx.stage.Popup;
import Services.*;
import javafx.util.Duration;

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
private void goToRatings(javafx.scene.input.MouseEvent event) {
    handleViewRating();
}

private void handleViewRating() {
    DAO.RatingDAO ratingDAO = new DAO.RatingDAO();
    List<Models.VendorRatingSummary> ratingsList = ratingDAO.getVendorLeaderboard();
    ObservableList<Models.VendorRatingSummary> data = FXCollections.observableArrayList(ratingsList);

    Stage stage = new Stage();
    javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(15);
    root.setPadding(new javafx.geometry.Insets(25));
    root.setStyle("-fx-background-color: white; -fx-background-radius: 20;");

    Label title = new Label("🏆 Food Hero Leaderboard");
    title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #2f3542;");
    
    TableView<Models.VendorRatingSummary> table = new TableView<>();
    table.setItems(data);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    table.setStyle("-fx-background-color: transparent; -fx-table-cell-border-color: transparent;");

    // COLUMN 1: VENDOR (Blue Style)
    TableColumn<Models.VendorRatingSummary, String> nameCol = new TableColumn<>("VENDOR");
    nameCol.setCellValueFactory(new PropertyValueFactory<>("vendorName"));
    nameCol.setCellFactory(column -> new TableCell<>() {
        @Override protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) { setText(null); setStyle(""); }
            else { 
                setText(item); 
                setStyle("-fx-background-color: #eaf2ff; -fx-text-fill: #0984e3; -fx-font-weight: bold; -fx-padding: 15; -fx-background-radius: 10 0 0 10;");
            }
        }
    });

    // COLUMN 2: RATING (Gold Style)
    TableColumn<Models.VendorRatingSummary, String> rateCol = new TableColumn<>("RATING");
    rateCol.setCellValueFactory(new PropertyValueFactory<>("stars"));
    rateCol.setCellFactory(column -> new TableCell<>() {
        @Override protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) { setText(null); setStyle(""); }
            else { 
                setText(item); 
                setStyle("-fx-background-color: #fff9e6; -fx-text-fill: #ffa502; -fx-alignment: CENTER; -fx-font-size: 18px; -fx-background-radius: 0 10 10 0;");
            }
        }
    });

    table.getColumns().addAll(nameCol, rateCol);
    root.getChildren().addAll(title, new Separator(), table);
    
    stage.setScene(new Scene(root, 500, 550));
    stage.setTitle("Rating Leaderboard");
    stage.show();
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
    loadDashboard("/Views/NGODashboard.fxml", event);
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
}
