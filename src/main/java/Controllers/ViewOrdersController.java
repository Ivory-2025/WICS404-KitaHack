package Controllers;

import Models.FoodListing;
import Models.NGO;
import Models.OrderResponse;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.io.IOException;
import java.time.LocalDate;

public class ViewOrdersController {

    @FXML private VBox ordersContainer; // Ensure this fx:id is in your ScrollPane's content
    private ObservableList<OrderResponse> ordersList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Load data first, then trigger the visual render
        loadDummyOrders();
        renderOrderFeed();
    }

    /**
     * Replaces the TableView logic with a card-based feed
     */
    private void renderOrderFeed() {
        ordersContainer.getChildren().clear();
        ordersContainer.setSpacing(20);
        ordersContainer.setPadding(new Insets(10));

        if (ordersList.isEmpty()) {
            Label emptyLabel = new Label("No active orders found.");
            emptyLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 16px;");
            ordersContainer.getChildren().add(emptyLabel);
            return;
        }

        for (OrderResponse order : ordersList) {
            ordersContainer.getChildren().add(createHighClassCard(order));
        }
    }

    private HBox createHighClassCard(OrderResponse order) {
        // Main Card Container
        HBox card = new HBox(25);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 25;");
        
        // Soft Modern Shadow
        DropShadow shadow = new DropShadow();
        shadow.setRadius(20);
        shadow.setOffsetY(10);
        shadow.setColor(Color.web("#0000000D"));
        card.setEffect(shadow);

        // 1. Food Image Insert Space
        StackPane imageContainer = new StackPane();
        ImageView foodImg = new ImageView(); // You can set images here later
        foodImg.setFitHeight(110);
        foodImg.setFitWidth(110);
        
        // Creating a rounded clipping mask for the image
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(110, 110);
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        foodImg.setClip(clip);
        
        imageContainer.setStyle("-fx-background-color: #F5F5F7; -fx-background-radius: 15;");
        imageContainer.getChildren().add(foodImg);

        // 2. Info Section (Harmonious Mixed Typography)
        VBox info = new VBox(6);
        
        // Bold Modern Sans for Item Name
        Label name = new Label(order.getListing().getFoodName());
        name.setStyle("-fx-font-family: 'Inter', 'Segoe UI', sans-serif; -fx-font-weight: 900; -fx-font-size: 20px; -fx-text-fill: #1D1D1F;");
        
        // Elegant Serif for NGO Name
        Label ngo = new Label("Requested by " + order.getNgo().getOrganizationName());
        ngo.setStyle("-fx-font-family: 'Georgia', serif; -fx-font-style: italic; -fx-font-size: 15px; -fx-text-fill: #6E6E73;");
        
        // Tech Monospace for Metadata
        Label meta = new Label("EXPIRES: " + order.getListing().getExpiryTime().toLocalDate() + " • LOC: " + order.getNgo().getAddress());
        meta.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-text-fill: #AEAEB2; -fx-letter-spacing: 1px;");
        
        info.getChildren().addAll(name, ngo, meta);

        // 3. Status Badge (Gen Z Aesthetic)
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label status = new Label(order.getStatus().toUpperCase());
        status.setPadding(new Insets(8, 16, 8, 16));
        
        boolean isAccepted = order.getStatus().equalsIgnoreCase("Accepted");
        String bgColor = isAccepted ? "#E8F5E9" : "#FFEBEE";
        String txtColor = isAccepted ? "#2E7D32" : "#C62828";
        
        status.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: %s; -fx-background-radius: 30; -fx-font-weight: 800; -fx-font-size: 10px; -fx-letter-spacing: 0.5px;",
            bgColor, txtColor
        ));

        card.getChildren().addAll(imageContainer, info, spacer, status);
        
        // Interaction Effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #FAFAFA; -fx-background-radius: 25; -fx-scale-x: 1.02; -fx-scale-y: 1.02;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 25; -fx-scale-x: 1.0; -fx-scale-y: 1.0;"));

        return card;
    }

    private void loadDummyOrders() {
        ordersList.clear();
        
        // Dummy data generation
        NGO ngo1 = new NGO(); 
        ngo1.setOrganizationName("Helping Hands");
        ngo1.setAddress("Kuching, Sarawak");

        NGO ngo2 = new NGO();
        ngo2.setOrganizationName("Food Rescue NGO");
        ngo2.setAddress("Kuching, Sarawak");

        FoodListing listing1 = new FoodListing();
        listing1.setFoodName("Fried Rice");
        listing1.setExpiryTime(LocalDate.now().plusDays(2).atStartOfDay());

        FoodListing listing2 = new FoodListing();
        listing2.setFoodName("Banana Bread");
        listing2.setExpiryTime(LocalDate.now().plusDays(1).atStartOfDay());

        ordersList.add(new OrderResponse(listing1, ngo1, "Accepted"));
        ordersList.add(new OrderResponse(listing2, ngo2, "Rejected"));
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Views/VendorDashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("SavePlate - Vendor Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadDummyOrders();
        renderOrderFeed();
    }
}