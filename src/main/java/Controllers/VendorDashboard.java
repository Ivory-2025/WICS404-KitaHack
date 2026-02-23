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
import DAO.FoodListingDAO;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;

public class VendorDashboard extends VBox {

    private ImageView foodImageView;
    private Button uploadButton;
    private Label statusLabel;
    private TextArea reportTextArea;
    private TableView<FoodListing> listingsTable;

    private FoodAnalysisService aiService;
    private Vendor currentVendor;
    private ObservableList<FoodListing> vendorListings;
    private FoodListingDAO foodListingDAO; // Added DAO

    // Updated constructor to accept the logged-in vendor
    public VendorDashboard(Vendor vendor) {
        this.aiService = new FoodAnalysisService();
        this.foodListingDAO = new FoodListingDAO(); 
        this.vendorListings = FXCollections.observableArrayList();
        this.currentVendor = vendor; // Use the actual logged-in user

        buildUI();
        loadExistingListings();
    }

    private void buildUI() {
        this.setPadding(new Insets(20));
        this.setSpacing(15);

        Label titleLabel = new Label("Vendor Dashboard - " + currentVendor.getRestaurantName());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        statusLabel = new Label("Ready to scan surplus food.");
        statusLabel.setStyle("-fx-text-fill: green;");

        foodImageView = new ImageView();
        foodImageView.setFitWidth(300);
        foodImageView.setFitHeight(200);
        foodImageView.setPreserveRatio(true);
        foodImageView.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

        uploadButton = new Button("📸 Snap / Upload Surplus Food");
        uploadButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        uploadButton.setOnAction(e -> handleUploadFood());

        reportTextArea = new TextArea();
        reportTextArea.setEditable(false);
        reportTextArea.setPrefRowCount(6);

        listingsTable = new TableView<>();
        TableColumn<FoodListing, String> nameCol = new TableColumn<>("Food Item");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("foodName"));
        TableColumn<FoodListing, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        listingsTable.getColumns().addAll(nameCol, statusCol);
        listingsTable.setItems(vendorListings);

        this.getChildren().addAll(titleLabel, statusLabel, new HBox(15, foodImageView, uploadButton), new Label("AI Analysis Report:"), reportTextArea, new Label("Your Active Listings:"), listingsTable);
    }

    private void handleUploadFood() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"));
        File selectedFile = fileChooser.showOpenDialog((Stage) this.getScene().getWindow());

        if (selectedFile != null) {
            try {
                statusLabel.setText("Gemini AI is analyzing your food...");
                statusLabel.setStyle("-fx-text-fill: orange;");

                Image image = new Image(selectedFile.toURI().toString());
                foodImageView.setImage(image);

                byte[] imageBytes = Files.readAllBytes(selectedFile.toPath());

                // 1. Create temporary listing
                FoodListing newListing = new FoodListing();
                newListing.setVendor(currentVendor);
                newListing.setProductionTime(LocalDateTime.now());
                newListing.setFoodName("Surplus Bundle #" + (vendorListings.size() + 1));
                newListing.setStatus("available");

                // 2. Call AI Service
                String aiJsonResponse = aiService.callGeminiApi(imageBytes);
                FoodAnalysisReport report = aiService.generateFoodAnalysisReport(aiJsonResponse, newListing);

                // 3. Save to Database via DAO
                foodListingDAO.save(report.getListing());

                // 4. Update UI
                vendorListings.add(report.getListing());
                updateUIWithReport(report);

            } catch (Exception e) {
                statusLabel.setText("Error analyzing photo.");
                statusLabel.setStyle("-fx-text-fill: red;");
                e.printStackTrace();
            }
        }
    }

    private void loadExistingListings() {
        // You can use your DAO to load previously saved listings for this vendor
        // List<FoodListing> existing = foodListingDAO.findByVendor(currentVendor.getId());
        // vendorListings.addAll(existing);
    }

    private void updateUIWithReport(FoodAnalysisReport report) {
        statusLabel.setText("Listing saved and pushed to NGO network!");
        statusLabel.setStyle("-fx-text-fill: green;");
        reportTextArea.setText("AI Recommendation: " + report.getRecommendation());
    }
}