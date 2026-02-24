package Controllers;

import Services.FoodAnalysisService;
import Models.FoodAnalysisReport;
import Models.FoodListing;
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

import java.io.File;
import java.io.IOException;

public class UploadFoodController {

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
        foodService = new FoodAnalysisService();
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
    private void handlePublish(ActionEvent event) {
        // Add your DAO save logic here
        System.out.println("Publishing: " + foodNameField.getText());
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Views/VendorDashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            // 5-Star Premium Transition
            root.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(500), root);
            ft.setFromValue(0); ft.setToValue(1); ft.play();
            
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}