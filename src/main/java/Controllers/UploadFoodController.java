package Controllers;

import Services.FoodAnalysisService;
import Services.MatchingService;
import Services.RoutingService;

import Models.*;
import DAO.*;

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
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import Services.RoutingService;
public class UploadFoodController {

    private Vendor currentVendor;
    private final FoodListingDAO foodListingDAO = new FoodListingDAO();
    private final MatchingService matchingService = new MatchingService();
    private final RoutingService routingService = new RoutingService();
    private final MessageDAO messageDAO = new MessageDAO();

    @FXML private Button chatNavButton; // Matches the fx:id in FXML
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
        this.currentVendor = UserSession.getInstance().getVendor();

        if (this.currentVendor == null) {
            System.err.println("⚠️ No vendor session found.");
        } else {
            System.out.println("✅ Vendor session loaded: " + currentVendor.getEmail());
        }
    }

    @FXML
    private void handleSelectPhoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        selectedFile = fileChooser.showOpenDialog(
                ((Node) event.getSource()).getScene().getWindow());

        if (selectedFile != null) {
            foodImageView.setImage(new Image(selectedFile.toURI().toString()));
            aiRecommendationArea.clear();
        }
    }

    @FXML
    private void handleAIScan(ActionEvent event) {

        if (selectedFile == null) {
            aiRecommendationArea.setText("Please select a photo first!");
            return;
        }

        aiRecommendationArea.setText("Gemini AI is analyzing " + selectedFile.getName() + "...");

        new Thread(() -> {
            try {
                FoodListing tempListing = new FoodListing();
                FoodAnalysisReport report =
                        foodService.analyzeFoodImage(selectedFile, tempListing);

                Platform.runLater(() -> {
                    if (report == null) {
                        aiRecommendationArea.setText("⚠️ ANALYSIS FAILED");
                        showToast(getStage(event),
                                "API Key or Connection Error!", false);
                    } else {
                        updateUIWithReport(report);
                        showToast(getStage(event),
                                "✨ Analysis Complete!", true);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() ->
                        aiRecommendationArea.setText("⚠️ ERROR: " + e.getMessage()));
            }
        }).start();
    }

    private void updateUIWithReport(FoodAnalysisReport report) {
    // 1. Refined Report Construction
    String status = report.getListing().getStatus().toUpperCase();
    int freshnessPercent = (int) (report.getFreshnessScore() * 100);
    
    StringBuilder sb = new StringBuilder();
    sb.append("✨ PREMIUM AI INSIGHTS\n");
    sb.append("────────────────────\n");
    sb.append("• Visual Condition : ").append(status).append("\n");
    sb.append("• Freshness Index  : ").append(freshnessPercent).append("%\n");
    sb.append("\n💡 STRATEGY: ").append(report.getRecommendation());

    // 2. Update the designated report area
    aiRecommendationArea.setText(sb.toString());

    // 3. Null-safe auto-fill logic
    if (report.getListing().getFoodName() != null) {
        foodNameField.setText(report.getListing().getFoodName());
    }

    if (report.getListing().getExpiryTime() != null) {
        expiryDatePicker.setValue(report.getListing().getExpiryTime().toLocalDate());
    }
}
@FXML
private void handlePublish(ActionEvent event) {
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

    if (currentVendor == null) {
        showToast(stage, "Error: No session found", false);
        return;
    }

    // Demo Coordinate Fix
    if (currentVendor.getLatitude() == 0.0) {
        currentVendor.setLatitude(3.1390);
        currentVendor.setLongitude(101.6869);
    }

    try {
        // Data Preparation
        FoodListing listing = new FoodListing();
        listing.setFoodName(foodNameField.getText());
        listing.setQuantity(quantityField.getText());
        listing.setVendor(currentVendor);
        listing.setStatus("available");
        listing.setProductionTime(LocalDateTime.now());

        if (expiryDatePicker.getValue() != null) {
            listing.setExpiryTime(expiryDatePicker.getValue().atStartOfDay());
        } else {
            listing.setExpiryTime(LocalDateTime.now().plusDays(1));
        }

        // SAVE
        foodListingDAO.save(listing);

        // MATCH
        List<NGO> matches = matchingService.findMatchingNGOs(listing);
        String aiReport = aiRecommendationArea.getText(); // FIX: Pulls actual report
        RoutingService routingService = new RoutingService();

        foodListingDAO.save(listing);
        String actualAIReport = aiRecommendationArea.getText();
            // Rich Gen Z Style Content
        // Consolidated Loop: Sends ONE rich message per NGO
        for (NGO ngo : matches) {
            String routeSummary = routingService.getRouteSummary(listing, ngo);
            String mapsLink = routingService.generateGoogleMapsLink(listing, ngo);
            System.out.println("Attempting PM: Vendor(" + currentVendor.getUserId() + ") -> NGO(" + ngo.getUserId() + ")");
        // FIX: Pull the actual report generated on the landing page
        
            String messageContent = "NEW SURPLUS ALERT: " + listing.getFoodName() + " 🍎\n" +
                                    "✨ AI Analysis: " + actualAIReport + "\n" +
                                    "--------------------------\n" +
                                    "📦 Quantity: " + listing.getQuantity() + "\n" +
                                    "⌛ Best Before: " + listing.getExpiryTime().format(DateTimeFormatter.ofPattern("HH:mm a")) + "\n" +
                                    "--------------------------\n" +
                                    routeSummary + "\n" +
                                    "🗺 Navigate: " + mapsLink + "\n\n" +
                                    "Tap Accept or Reject below. ⚡";

            messageDAO.sendAutoPM(currentVendor.getUserId(), ngo.getUserId(), messageContent);
        }

        // Confirmation Dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Surplus Published");
        alert.setHeaderText("Success!");
        alert.setContentText(matches.size() + " NGOs have been notified.");

        ButtonType goToChat = new ButtonType("Go to Chat");
        ButtonType stayHere = new ButtonType("Stay Here");
        alert.getButtonTypes().setAll(goToChat, stayHere);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == goToChat) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/ChatLanding.fxml"));
            Parent root = loader.load();
            
            root.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), root);
            fadeIn.setFromValue(0); fadeIn.setToValue(1);
            fadeIn.play();

            stage.setScene(new Scene(root));
            stage.show();
        }

    } catch (Exception e) {
        e.printStackTrace();
        showToast(stage, "Publishing Failed!", false);
    }
}
    // ✅ Strongly typed now
    public void setVendor(Vendor vendor) {
        this.currentVendor = vendor;

        if (vendor != null) {
            System.out.println("Vendor set: " + vendor.getEmail());
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Views/VendorDashboard.fxml"));
            Parent root = loader.load();

            VendorDashboardController controller =
                    loader.getController();
            controller.setCurrentVendor(
                    UserSession.getInstance().getVendor());

            Stage stage = getStage(event);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Stage getStage(ActionEvent event) {
    // This grabs the window from the button that triggered the event
    return (Stage) ((Node) event.getSource()).getScene().getWindow();
}
    public void showToast(Stage stage,
                          String message,
                          boolean isSuccess) {

        Label label = new Label(message);
        label.setStyle(
                "-fx-background-color: "
                        + (isSuccess ? "#1A1A1A" : "#FF4D4D") + ";"
                        + "-fx-text-fill: white;"
                        + "-fx-padding: 15 30;"
                        + "-fx-background-radius: 25;"
                        + "-fx-font-weight: 700;"
                        + "-fx-font-size: 14px;"
                        + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 20, 0, 0, 10);"
        );

        Popup popup = new Popup();
        popup.getContent().add(label);
        popup.show(stage);

        FadeTransition fadeOut =
                new FadeTransition(Duration.millis(2000), label);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> popup.hide());
        fadeOut.play();
    }

    @FXML
private void handleGoToChats(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/ChatLanding.fxml"));
        Parent root = loader.load();
        
        // Use your existing helper to switch scenes
        Stage stage = getStage(event);
        stage.setScene(new Scene(root));
        stage.show();
    } catch (IOException e) {
        System.err.println("Error navigating to Chat Landing: " + e.getMessage());
    }
}
}