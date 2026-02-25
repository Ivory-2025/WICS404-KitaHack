package Controllers;

//** TODO: setup Java FX, FXML, for the codes to work
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import Models.FoodListing;
import Models.NGO;
import Services.MatchingService;
import Services.RoutingService;

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
        setupTableColumns();

        // When user selects a row, show the route info automatically
        foodTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, selectedListing) -> {
                if (selectedListing != null) {
                    showRouteSummary(selectedListing);
                }
            }
        );
    }

    /**
     * Call this right after loading the FXML, passing in the logged-in NGO.
     * Example from your login controller:
     *     ngoController.setCurrentNGO(loggedInNGO);
     */
    public void setCurrentNGO(NGO ngo) {
        this.currentNGO = ngo;
        loadMatchedListings();
    }

    // -----------------------------------------------------------------------
    // Table setup
    // -----------------------------------------------------------------------

    private void setupTableColumns() {
        colFoodName.setCellValueFactory(new PropertyValueFactory<>("foodName"));

        // Vendor name comes from nested object so we use a custom factory
        colVendorName.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getVendor().getRestaurantName()
            )
        );

        colExpiryTime.setCellValueFactory(new PropertyValueFactory<>("expiryTime"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    // -----------------------------------------------------------------------
    // Load food listings that match this NGO
    // -----------------------------------------------------------------------

    private void loadMatchedListings() {
        if (currentNGO == null) return;

        List<FoodListing> matched = matchingService.findMatchingListingsForNGO(currentNGO);
        ObservableList<FoodListing> data = FXCollections.observableArrayList(matched);
        foodTable.setItems(data);

        lblRouteSummary.setText("Select a listing to see route info.");
        System.out.println("Loaded " + matched.size() + " matched listings for NGO: " + currentNGO.getOrganizationName());
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

        boolean success = matchingService.acceptListing(selected.getListingId(), currentNGO);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Accepted!",
                "You have successfully claimed: " + selected.getFoodName()
                + "\nPlease proceed to pick it up.");
            loadMatchedListings(); // Refresh table so claimed listing disappears
        } else {
            showAlert(Alert.AlertType.ERROR, "Too Slow!",
                "Sorry! Another NGO already accepted this listing.");
            loadMatchedListings(); // Refresh anyway to reflect latest statuses
        }
    }

    // -----------------------------------------------------------------------
    // View Route button — opens Google Maps in the browser
    // -----------------------------------------------------------------------

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
}
