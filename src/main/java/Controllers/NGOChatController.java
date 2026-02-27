package Controllers;

import DAO.FoodListingDAO;
import DAO.MessageDAO;
import DAO.VendorDAO;
import Models.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import Services.RoutingService;

import java.util.List;

public class NGOChatController {
    @FXML private ListView<Vendor> vendorListView;
    @FXML private Label chatHeaderLabel;
    @FXML private VBox messageContainer;
    @FXML private TextField messageInputField;
    @FXML private ScrollPane chatScrollPane;

    private final VendorDAO vendorDAO = new VendorDAO();
    private final MessageDAO messageDAO = new MessageDAO();
    private final FoodListingDAO foodListingDAO = new FoodListingDAO();
    private Vendor selectedVendor;

    @FXML
    public void initialize() {
        // 1. ListCell Factory for Vendors
        vendorListView.setCellFactory(lv -> new ListCell<Vendor>() {
            @Override
            protected void updateItem(Vendor vendor, boolean empty) {
                super.updateItem(vendor, empty);
                setText((empty || vendor == null) ? null : vendor.getStoreName());
            }
        });

        loadActiveVendors();

        // 2. Selection Listener
        vendorListView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedVendor = newVal;
                chatHeaderLabel.setText(newVal.getStoreName());
                loadMessages();
            }
        });
    }

    private void loadMessages() {
        messageContainer.getChildren().clear();
        if (selectedVendor == null) return;

        int ngoId = UserSession.getInstance().getNGO().getUserId();
        List<Message> history = messageDAO.getChatHistory(selectedVendor.getUserId(), ngoId);

        for (Message msg : history) {
            // Logic Flip: If sender is NOT the NGO, it's from the Vendor
            boolean isFromMe = (msg.getSenderId() == ngoId);
            addMessageToUI(msg.getContent(), isFromMe);
        }
        chatScrollPane.setVvalue(1.0);
    }

    private void addMessageToUI(String content, boolean isFromMe) {
        VBox bubbleContainer = new VBox(8);
        bubbleContainer.setAlignment(isFromMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: " + (isFromMe ? "#d1e7ff" : "#FFFFFF") + "; " +
                      "-fx-padding: 15; -fx-background-radius: 15; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        if (content.contains("New Surplus Alert") && !isFromMe) {
            setupSurplusActionCard(card, content, bubbleContainer);
        } else {
            Label text = new Label(content);
            text.setWrapText(true);
            card.getChildren().add(text);
        }

        bubbleContainer.getChildren().add(card);
        messageContainer.getChildren().add(bubbleContainer);
    }

    private void setupSurplusActionCard(VBox card, String content, VBox container) {
        Label header = new Label("INCOMING DONATION");
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label details = new Label(content);
        details.setWrapText(true);

        HBox actions = new HBox(10);
        Button acceptBtn = new Button("Claim Donation");
        Button rejectBtn = new Button("Decline");

        acceptBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 10;");
        
        acceptBtn.setOnAction(e -> handleClaim(container));

        actions.getChildren().addAll(acceptBtn, rejectBtn);
        card.getChildren().addAll(header, details, actions);
    }

    private void handleClaim(VBox container) {
        // Here you would trigger the Google Maps link logic 
        // using your existing RoutingService.
        RoutingService rs = new RoutingService();
        // Mocking a listing fetch for the specific vendor
        FoodListing listing = foodListingDAO.getLatestListingByVendor(selectedVendor.getUserId());
        
        if (listing != null) {
            String mapsLink = rs.generateGoogleMapsLink(listing, UserSession.getInstance().getNGO());
            // Open Browser
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(mapsLink));
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    @FXML
    private void handleSendMessage() {
        String text = messageInputField.getText();
        if (text == null || text.trim().isEmpty() || selectedVendor == null) return;

        int ngoId = UserSession.getInstance().getNGO().getUserId();
        messageDAO.sendMessage(ngoId, selectedVendor.getUserId(), text.trim());
        addMessageToUI(text.trim(), true);
        messageInputField.clear();
    }

    private void loadActiveVendors() {
        List<Vendor> vendors = vendorDAO.getAllVendors();
        vendorListView.setItems(FXCollections.observableArrayList(vendors));
    }

    @FXML
private void handleBackToDashboard(javafx.event.ActionEvent event) {
    try {
        // 1. Load the Dashboard FXML
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/Views/NGODashboard.fxml"));
        javafx.scene.Parent root = loader.load();

        // 2. Get the current Stage
        javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

        // 3. Set the scene
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
    } catch (java.io.IOException e) {
        System.err.println("Error returning to Dashboard: " + e.getMessage());
        e.printStackTrace();
    }
}
}