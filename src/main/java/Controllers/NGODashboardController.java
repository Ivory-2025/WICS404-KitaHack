package Controllers;

//** TODO: setup Java FX, FXML, for the codes to work
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
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
import Services.MatchingService;
import Services.RoutingService;
import java.io.IOException;
import java.awt.Desktop;
import java.net.URI;
import java.util.List;

public class NGODashboardController {

    // -----------------------------------------------------------------------
    // FXML — wire these to your .fxml file
    // -----------------------------------------------------------------------

    @FXML private TableView<FoodListing> foodTable;
    @FXML private TableColumn<FoodListing, String> colFoodName;
    @FXML private TableColumn<FoodListing, String> colVendorName;
    @FXML private TableColumn<FoodListing, String> colExpiryTime;
    @FXML private TableColumn<FoodListing, String> colStatus;

    @FXML private Label lblRouteSummary;
    @FXML private Button btnAccept;
    @FXML private Button btnViewRoute;

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
    if (foodTable != null) {
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
    // 1. Get Selection and Check for Null
    FoodListing selected = foodTable.getSelectionModel().getSelectedItem();

    if (selected == null) {
        showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a food listing first.");
        return;
    }

    // 2. Prepare the "Gen Z" Safety Dialog
    String timeLeft = selected.getTimeUntilExpiry();
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Food Rescue Mission! 🥳");

    VBox content = new VBox(15);
    content.setAlignment(Pos.CENTER);
    content.setPadding(new Insets(25));
    content.setStyle("-fx-background-color: white; -fx-background-radius: 20;");

    Label msg = new Label("Awesome choice! \n\nJust a heads-up: this " + selected.getFoodName() + 
                         "\nexpires in about " + timeLeft + ".\n\nEat it before then to stay safe!");
    msg.setStyle("-fx-font-size: 14px; -fx-text-fill: #2d3436; -fx-text-alignment: CENTER; -fx-font-weight: bold;");

    content.getChildren().addAll(msg);
    dialog.getDialogPane().setContent(content);

    ButtonType claimBtn = new ButtonType("GOT IT, CLAIM!", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(claimBtn, ButtonType.CANCEL);
    
    // 3. Handle the Claim Execution
    dialog.showAndWait().ifPresent(response -> {
        if (response == claimBtn) {
            // Call the matching service to update the DB
            boolean success = matchingService.acceptListing(selected.getListingId(), currentNGO);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, 
                          "Accepted!", 
                          "You have successfully claimed: " + selected.getFoodName() + 
                          "\nPlease proceed to pick it up.");
            } else {
                showAlert(Alert.AlertType.ERROR, 
                          "Too Slow!", 
                          "Sorry! Another NGO already accepted this listing.");
            }
            
            // 4. Always Refresh the Table
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
    // try {
    //     FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/NGODashboard.fxml"));
    //     javafx.scene.Parent root = loader.load();
        
    //     // Pass the current NGO to the marketplace view
    //     NGODashboardController controller = loader.getController();
    //     controller.setCurrentNGO(this.currentNGO);
        
    //     //Stage stage = (Stage) lblRouteSummary.getScene().getWindow();
    //     Stage stage = (Stage) javafx.stage.Window.getWindows().filtered(w -> w.isShowing()).get(0);
    //     stage.setScene(new Scene(root));
    // } catch (java.io.IOException e) {
    //     e.printStackTrace();
    //     showAlert(Alert.AlertType.ERROR, "Error", "Could not load Marketplace.");
    // }
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/NGODashboard.fxml"));
        Parent root = loader.load();
        
        NGODashboardController controller = loader.getController();
        controller.setCurrentNGO(this.currentNGO);
        
        // Use the event to get the current window safely
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("NGO Marketplace");
    } catch (java.io.IOException e) {
        e.printStackTrace();
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
}
