package Controllers;

import Services.FoodAnalysisService;
import Models.FoodAnalysisReport;
import Models.FoodListing;
import Models.NGO;
import Models.UserSession;
import Models.Vendor;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import Services.MatchingService;
import Services.RoutingService;

import java.io.File;
import java.io.IOException;
import java.util.List;

import DAO.FoodListingDAO;
import DAO.MessageDAO;

public class UploadFoodController {

    // These must be declared at the class level to fix the red errors
private Vendor currentVendor; // This should be set when the dashboard loads
private FoodListingDAO foodListingDAO = new FoodListingDAO();
    private MatchingService matchingService = new MatchingService();
    private RoutingService routingService = new RoutingService();
    private MessageDAO messageDAO = new MessageDAO();
    // Matches your FXML fx:id exactly
    @FXML private TextField foodNameField;
    @FXML private TextField quantityField;
    @FXML private DatePicker expiryDatePicker;
    @FXML private ImageView foodImageView;
    @FXML private TextArea aiRecommendationArea; 
    
    private FoodAnalysisService foodService;
    private File selectedFile;

    @FXML
public void initialize() {
    this.foodService = new FoodAnalysisService();
    // Retrieve the vendor from the global session
    this.currentVendor = Models.UserSession.getInstance().getVendor(); 
    
    if (this.currentVendor == null) {
        System.err.println("Error: No vendor session found.");
    }
}

    @FXML
    private void handleSelectPhoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        selectedFile = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());

        if (selectedFile != null) {
            foodImageView.setImage(new Image(selectedFile.toURI().toString()));
            aiRecommendationArea.setText(""); // Reset for new scan
        }
    }

    @FXML
    private void handleAIScan(ActionEvent event) {
        if (selectedFile == null) {
            aiRecommendationArea.setText("Please select a photo first!");
            return;
        }

        aiRecommendationArea.setText("Gemini AI is analyzing " + selectedFile.getName() + "...");

        // Run AI in background to prevent UI freezing
        new Thread(() -> {
            try {
                FoodListing tempListing = new FoodListing();
                FoodAnalysisReport report = foodService.analyzeFoodImage(selectedFile, tempListing);

                Platform.runLater(() -> {
                    if (report == null) {
                        aiRecommendationArea.setText("⚠️ AI ANALYSIS FAILED\nPlease try a clearer photo.");
                    } else {
                        updateUIWithReport(report);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> aiRecommendationArea.setText("⚠️ ERROR: " + e.getMessage()));
            }
        }).start();
    }

    private void updateUIWithReport(FoodAnalysisReport report) {
        // Build premium report string
        StringBuilder sb = new StringBuilder();
        sb.append("✨ GEMINI ANALYSIS:\n");
        sb.append("• Condition: ").append(report.getListing().getStatus()).append("\n");
        sb.append("• Freshness: ").append((int)(report.getFreshnessScore() * 100)).append("%\n");
        sb.append("\n💡 Recommendation: ").append(report.getRecommendation());

        aiRecommendationArea.setText(sb.toString());

        // Auto-fill logic based on AI data
        if (report.getListing().getFoodName() != null) {
            foodNameField.setText(report.getListing().getFoodName());
        }
        
        if (report.getListing().getExpiryTime() != null) {
            expiryDatePicker.setValue(report.getListing().getExpiryTime().toLocalDate());
        }
    }

    @FXML
private void handlePublish() {

    if (currentVendor == null) {
        System.out.println("Error: No vendor session found.");
        return;
    }

    FoodListing listing = new FoodListing();
    listing.setFoodName(foodNameField.getText());
    listing.setQuantity(quantityField.getText());
    listing.setVendor(currentVendor);
    listing.setStatus("available");

    foodListingDAO.save(listing);

    // Find NGOs based on listing
    List<NGO> matches = matchingService.findMatchingNGOs(listing);

    for (NGO ngo : matches) {
        String msg = "New Surplus: " + listing.getFoodName() +
                " is ready!";

        messageDAO.sendAutoPM(
                currentVendor.getUserId(),
                ngo.getUserId(),
                msg
        );
    }

    showConfirmation("Listing Published! " + matches.size() + " NGOs notified.");
}

    private void showConfirmation(String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, content);
        alert.show();
    }
    
    public void setVendor(Vendor vendor) {
        this.currentVendor = vendor;
    }

    @FXML
private void handleBackToDashboard(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/VendorDashboard.fxml"));
        Parent root = loader.load();

        // 1. Pass the vendor profile back to the dashboard
        VendorDashboardController controller = loader.getController();
        controller.setCurrentVendor(Models.UserSession.getInstance().getVendor());

        // 2. Switch the Scene
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
        
    } catch (IOException e) {
        System.err.println("Navigation Error: " + e.getMessage());
    }
}

    @FXML
private void handleBack(ActionEvent event) {
    try {
        // Load the Vendor Dashboard FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/VendorDashboard.fxml"));
        Parent root = loader.load();

        // Pass the vendor profile back to the dashboard controller
        VendorDashboardController controller = loader.getController();
        controller.setCurrentVendor(Models.UserSession.getInstance().getVendor());

        // Get the current stage and switch the scene
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
        
    } catch (IOException e) {
        System.err.println("Navigation Error: " + e.getMessage());
        e.printStackTrace();
    }
}
}