package Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import Models.FoodAnalysisReport;
import Models.FoodListing;
import Models.Vendor;
import Services.FoodAnalysisService;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;

/**
 * Pure JavaFX Vendor Dashboard (No FXML).
 * Handles building the UI, uploading food, displaying reports, and showing listing status.
 */
public class VendorDashboard extends VBox {

    // --- UI Elements ---
    private ImageView foodImageView;
    private Button uploadButton;
    private Label statusLabel;
    private TextArea reportTextArea;
    private TableView<FoodListing> listingsTable;

    // --- Data & Services ---
    private FoodAnalysisService aiService;
    private Vendor currentVendor;
    private ObservableList<FoodListing> vendorListings;

    public VendorDashboard() {
        // Initialize services and mock user
        aiService = new FoodAnalysisService();
        vendorListings = FXCollections.observableArrayList();
        currentVendor = new Vendor(1, "Ali", "ali@cafe.com", "pass123", "VENDOR", 
                                   "Campus Cafe", "Student Union Bldg", 4.8, 3.12, 101.65);

        // Build the User Interface
        buildUI();
    }

    /**
     * Constructs the visual layout using pure Java.
     */
    private void buildUI() {
        this.setPadding(new Insets(20));
        this.setSpacing(15);

        // 1. Header & Status
        Label titleLabel = new Label("Vendor Dashboard - EcoBites");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        statusLabel = new Label("Ready to scan surplus food.");
        statusLabel.setStyle("-fx-text-fill: green;");

        // 2. Upload Section (Requirement 1)
        foodImageView = new ImageView();
        foodImageView.setFitWidth(300);
        foodImageView.setFitHeight(200);
        foodImageView.setPreserveRatio(true);
        foodImageView.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

        uploadButton = new Button("📸 Snap / Upload Surplus Food");
        uploadButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        uploadButton.setOnAction(e -> handleUploadFood());

        // 3. AI Report Section (Requirement 2)
        Label reportLabel = new Label("AI Analysis Report:");
        reportTextArea = new TextArea();
        reportTextArea.setEditable(false);
        reportTextArea.setPrefRowCount(6);
        reportTextArea.setPromptText("Gemini AI results will appear here...");

        // 4. Listing Status Table (Requirement 3)
        Label tableLabel = new Label("Your Active Listings:");
        listingsTable = new TableView<>();
        listingsTable.setPrefHeight(200);

        TableColumn<FoodListing, String> nameCol = new TableColumn<>("Food Item");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("foodName"));
        nameCol.setPrefWidth(150);

        TableColumn<FoodListing, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);

        TableColumn<FoodListing, LocalDateTime> expiryCol = new TableColumn<>("Expires At");
        expiryCol.setCellValueFactory(new PropertyValueFactory<>("expiryTime"));
        expiryCol.setPrefWidth(150);

        listingsTable.getColumns().addAll(nameCol, statusCol, expiryCol);
        listingsTable.setItems(vendorListings);

        // Combine all elements into this VBox layout
        this.getChildren().addAll(
            titleLabel, 
            statusLabel, 
            new HBox(15, foodImageView, uploadButton), 
            reportLabel, 
            reportTextArea, 
            tableLabel, 
            listingsTable
        );
    }

    /**
     * Requirement 1: Upload food & call AI.
     */
    private void handleUploadFood() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Surplus Food Photo");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        Stage stage = (Stage) this.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                statusLabel.setText("Gemini AI is analyzing your food...");
                statusLabel.setStyle("-fx-text-fill: orange;");

                // Show image
                Image image = new Image(selectedFile.toURI().toString());
                foodImageView.setImage(image);

                // Convert to bytes for AI
                byte[] imageBytes = Files.readAllBytes(selectedFile.toPath());

                // Create listing framework
                FoodListing newListing = new FoodListing();
                newListing.setVendor(currentVendor);
                newListing.setProductionTime(LocalDateTime.now());
                newListing.setFoodName("Surplus Bundle #" + (vendorListings.size() + 1));

                // Call AI Service
                String aiJsonResponse = aiService.callGeminiApi(imageBytes);
                FoodAnalysisReport report = aiService.generateFoodAnalysisReport(aiJsonResponse, newListing);

                // Update Table (Requirement 3)
                vendorListings.add(report.getListing());

                // Display Report (Requirement 2)
                updateUIWithReport(report);

            } catch (Exception e) {
                statusLabel.setText("Error analyzing photo.");
                statusLabel.setStyle("-fx-text-fill: red;");
                e.printStackTrace();
            }
        }
    }

    /**
     * Requirement 2: Display the detailed AI analysis.
     */
    private void updateUIWithReport(FoodAnalysisReport report) {
        statusLabel.setText("Listing automatically pushed to Student App!");
        statusLabel.setStyle("-fx-text-fill: green;");
        
        String displayText = "🔥 AI generated the following details:\n" +
                "--------------------------------------------------\n" +
                "Recommendation: " + report.getRecommendation() + "\n" +
                "Ingredients Detected: " + report.getListing().getIngredients() + "\n" +
                "Freshness Score: " + (int)(report.getFreshnessScore() * 100) + "%\n" +
                "Expires At: " + report.getListing().getExpiryTime().toString() + "\n" +
                "Allergens: " + (report.getAllergens().isEmpty() ? "None Detected" : String.join(", ", report.getAllergens()));

        reportTextArea.setText(displayText);
    }
}