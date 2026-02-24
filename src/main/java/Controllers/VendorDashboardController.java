package Controllers;

import DAO.FoodListingDAO;
import Models.FoodAnalysisReport;
import Models.FoodListing;
import Models.User;
import Models.Vendor;
import Services.FoodAnalysisService;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

public class VendorDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private VBox uploadCard;
    @FXML private VBox ordersCard;

    private Vendor currentVendor;

    public void setCurrentVendor(User user) {
        if (user instanceof Vendor) {
            this.currentVendor = (Vendor) user;
            welcomeLabel.setText("Welcome back, " + currentVendor.getName() + "!");
        }
    }

    @FXML
    public void initialize() {
        // Setup card hover animations for high-class feel
        setupCardInteractions(uploadCard);
        setupCardInteractions(ordersCard);
    }

    private void setupCardInteractions(VBox card) {
        if (card == null) return;
        ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
        card.setOnMouseEntered(e -> { st.setToX(1.05); st.setToY(1.05); st.play(); });
        card.setOnMouseExited(e -> { st.setToX(1.0); st.setToY(1.0); st.play(); });
    }

    // ===================== Navigation Fix =====================
    
    @FXML
    private void handleUploadFoodSurplus(MouseEvent event) {
        // Redirect back to your dedicated landing page
        switchScreen(event, "/Views/UploadFood.fxml");
    }

    @FXML
    private void handleViewOrders(MouseEvent event) {
        switchScreen(event, "/Views/ViewOrders.fxml");
    }

    @FXML
    private void handleLogout(MouseEvent event) {
        switchScreen(event, "/Views/Login.fxml");
    }

    /**
     * Reusable logic for smooth screen transitions
     */
    private void switchScreen(MouseEvent event, String fxmlPath) {
    try {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        
        // Use a FadeTransition here for that 5-star premium feel
        root.setOpacity(0);
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(500), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        
        stage.setScene(new Scene(root));
        stage.centerOnScreen();
        ft.play();
    } catch (IOException e) {
        System.err.println("Error: Could not load " + fxmlPath);
        e.printStackTrace();
    }
}}