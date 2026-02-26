package Controllers;

import DAO.FoodListingDAO;
import DAO.MessageDAO;
import DAO.NGODAO;
import Models.FoodListing;
import Models.Message;
import Models.NGO;
import Models.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import Services.*;
public class ChatLandingController {
    @FXML private ListView<NGO> ngoListView;
    @FXML private Label chatHeaderLabel;
    @FXML private VBox messageContainer;
    @FXML private TextField messageInputField;
    @FXML private ScrollPane chatScrollPane;
    @FXML private Label statusLabel;
    @FXML private javafx.scene.shape.Circle statusIndicator;
@FXML private Label distanceLabel;
@FXML private Label etaLabel;

@FXML private HBox decisionBox;
@FXML private Button acceptButton;
@FXML private Button rejectButton;

@FXML private Label listingTitleLabel;

    @FXML private Label routeInfoLabel;
    @FXML private FoodListing currentListing;

    private final FoodListingDAO foodListingDAO = new FoodListingDAO();
    private Timeline autoRefreshTimeline;
    private final NGODAO ngoDAO = new NGODAO();
    private final MessageDAO messageDAO = new MessageDAO();
    private NGO selectedNGO;

    @FXML
    public void initialize() {
        loadActiveChats();
        
        ngoListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedNGO = newVal;
                chatHeaderLabel.setText(newVal.getOrganizationName());
loadMessages();

// Default status when opening chat
statusLabel.setText("Pending Response");
statusIndicator.setFill(javafx.scene.paint.Color.GOLD);

distanceLabel.setText("");
etaLabel.setText("");
decisionBox.setVisible(false);
decisionBox.setManaged(false);
            }
        });

        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            if (selectedNGO != null) {
                loadMessages();
            }
        }));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();

        if (!ngoListView.getItems().isEmpty()) {
            ngoListView.getSelectionModel().selectFirst();
        }

        ngoListView.setCellFactory(lv -> new ListCell<Models.NGO>() {
        @Override
        protected void updateItem(Models.NGO ngo, boolean empty) {
            super.updateItem(ngo, empty);
            if (empty || ngo == null) {
                setText(null);
            } else {
                setText(ngo.getOrganizationName()); // Displays "PJ Food Rescue"
            }
        }
    });

    loadActiveChats();
    }

    public void stopTimer() {
        if (autoRefreshTimeline != null) autoRefreshTimeline.stop();
    }

    private void loadMessages() {
        // 1. Clear the ListView items first to ensure a fresh UI
    ngoListView.getItems().clear(); 
    
    // 2. Load the cleaned list from the NGOs table
    List<Models.NGO> ngos = ngoDAO.getAllNGOs(); 
    ObservableList<Models.NGO> items = FXCollections.observableArrayList(ngos);
    ngoListView.setItems(items);
    // 1. Clears current view to prevent duplication
    messageContainer.getChildren().clear(); 
    
    // 2. Safety check for selected contact
    if (selectedNGO == null) return;

    // 3. Fetch from DB using current Vendor session
    int vendorId = UserSession.getInstance().getVendor().getUserId();
    List<Message> history = messageDAO.getChatHistory(vendorId, selectedNGO.getUserId());
    
    // 4. Rebuild the UI bubbles
    for (Message msg : history) {
        addMessageToUI(msg.getContent(), msg.getSenderId() == vendorId);
    }
    
    chatScrollPane.setVvalue(1.0);
}

    private void addMessageToUI(String content, boolean isVendor) {
    VBox bubbleContainer = new VBox(8);
    bubbleContainer.setAlignment(isVendor ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

    // Main Card Container
    VBox card = new VBox(15);
    card.setStyle("-fx-background-color: " + (isVendor ? "#d4edc8" : "#FFFFFF") + "; " +
                  "-fx-padding: 20; -fx-background-radius: 20; " +
                  "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 5);");

    if (content.contains("New Surplus Alert")) {
        // 1. Header: NEW SURPLUS ALERT
        Label header = new Label("NEW SURPLUS ALERT");
        header.setStyle("-fx-font-weight: 900; -fx-font-size: 18px; -fx-text-fill: #145694;");

        // 2. Food Details (The "png" placeholder in your sketch)
        VBox foodDetails = new VBox(5);
        foodDetails.setStyle("-fx-background-color: #F1F5F9; -fx-padding: 15; -fx-background-radius: 12;");
        Label detailText = new Label(content.split("✨ AI Analysis:")[0]);
        detailText.setWrapText(true);
        detailText.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569;");
        foodDetails.getChildren().add(detailText);

        // 3. AI Analysis Section
        VBox aiSection = new VBox(8);
        aiSection.setStyle("-fx-border-color: #10B981; -fx-border-width: 2; -fx-border-radius: 12; " +
                           "-fx-padding: 15; -fx-background-color: #ECFDF5;");
        Label aiTitle = new Label("✨ AI SUSTAINABILITY REPORT");
        aiTitle.setStyle("-fx-font-weight: 800; -fx-font-size: 12px; -fx-text-fill: #065F46;");
        
        String aiBody = content.contains("✨ AI Analysis:") ? content.split("✨ AI Analysis:")[1].split("----------------")[0] : "Processing...";
        Label aiContent = new Label(aiBody.trim());
        aiContent.setWrapText(true);
        aiContent.setStyle("-fx-font-size: 13px; -fx-text-fill: #047857; -fx-font-style: italic;");
        aiSection.getChildren().addAll(aiTitle, aiContent);

        // 4. Traffic Duration
        Services.RoutingService rs = new Services.RoutingService();
        double distance = rs.calculateBasicDistance(UserSession.getInstance().getVendor(), selectedNGO);
        int estMins = (int)(distance * 2) + 5;
        Label traffic = new Label("🚗 Estimated Arrival: " + estMins + " mins to Vendor Site");
        traffic.setStyle("-fx-text-fill: #E53E3E; -fx-font-weight: 800; -fx-font-size: 12px;");

        // 5. Accept / Reject Buttons
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER);
        Button accept = new Button("Accept");
        accept.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 20; -fx-padding: 10 30; -fx-cursor: hand;");
        Button reject = new Button("Reject");
        reject.setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-font-weight: 800; -fx-border-color: #EF4444; -fx-border-radius: 20; -fx-padding: 10 30; -fx-cursor: hand;");
        
        // Handlers
        accept.setOnAction(e -> handleAcceptDonation(bubbleContainer));
        reject.setOnAction(e -> {
            messageContainer.getChildren().remove(bubbleContainer);
            showToast((Stage)bubbleContainer.getScene().getWindow(), "Donation Declined", false);
        });

        actions.getChildren().addAll(accept, reject);
        card.getChildren().addAll(header, foodDetails, aiSection, traffic, actions);
    } else {
        // Fallback for regular text messages
        Label text = new Label(content);
        text.setWrapText(true);
        text.setStyle("-fx-text-fill: " + (isVendor ? "white" : "#1A1A1A") + "; -fx-font-size: 14px;");
        card.getChildren().add(text);
    }

    bubbleContainer.getChildren().add(card);
    messageContainer.getChildren().add(bubbleContainer);
}
    
    private void applyPulseAnimation(Button button) {
    javafx.animation.ScaleTransition pulse = new javafx.animation.ScaleTransition(Duration.millis(1000), button);
    pulse.setFromX(1.0);
    pulse.setFromY(1.0);
    pulse.setToX(1.05);
    pulse.setToY(1.05);
    pulse.setCycleCount(javafx.animation.Animation.INDEFINITE);
    pulse.setAutoReverse(true);
    pulse.play();
}
   private void handleAcceptDonation(VBox container) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Confirm vehicle readiness?", ButtonType.YES, ButtonType.NO);
    alert.showAndWait().ifPresent(response -> {
        if (response == ButtonType.YES) {
            try {
                Services.RoutingService rs = new Services.RoutingService();
                
                // Use your existing generateGoogleMapsLink method
                // Note: Ensure your method accepts (listing, ngo) or update it to take lat/lon
                String mapsUrl = rs.generateGoogleMapsLink(currentListing, selectedNGO);

                java.awt.Desktop.getDesktop().browse(new java.net.URI(mapsUrl));
                showToast((Stage)container.getScene().getWindow(), "Opening Maps...", true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    });
}

@FXML
private void handleSendMessage() { // Remove any parameters like (ActionEvent event)
    String text = messageInputField.getText();
    
    if (text == null || text.trim().isEmpty() || selectedNGO == null) {
        return;
    }

    int vendorId = UserSession.getInstance().getVendor().getUserId();
    messageDAO.sendMessage(vendorId, selectedNGO.getUserId(), text.trim());
    addMessageToUI(text.trim(), true);
    messageInputField.clear();
}
    public void showToast(Stage stage, String message, boolean isSuccess) {
    Label label = new Label(message);
    label.setStyle("-fx-background-color: " + (isSuccess ? "#1A1A1A" : "#EF4444") + ";"
            + "-fx-text-fill: white; -fx-padding: 15 30; -fx-background-radius: 25;"
            + "-fx-font-weight: 700; -fx-font-size: 14px;");

    Popup popup = new Popup();
    popup.getContent().add(label);
    popup.show(stage);

    FadeTransition fadeOut = new FadeTransition(Duration.millis(2000), label);
    fadeOut.setFromValue(1.0);
    fadeOut.setToValue(0.0);
    fadeOut.setOnFinished(e -> popup.hide());
    fadeOut.play();
}

    private void loadActiveChats() {
    // Wrap in runLater to ensure thread safety and prevent IndexOutOfBounds
    javafx.application.Platform.runLater(() -> {
        // 1. Tell the selection model to let go first
        ngoListView.getSelectionModel().clearSelection();
        
        // 2. Clear existing items to prevent the "PJ Food Rescue" duplicates
        ngoListView.getItems().clear(); 
        
        // 3. Load unique NGOs from the database
        List<Models.NGO> ngos = ngoDAO.getAllNGOs(); 
        if (ngos != null && !ngos.isEmpty()) {
            ngoListView.setItems(javafx.collections.FXCollections.observableArrayList(ngos));
        }
    });
}

    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/Views/VendorDashboard.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void setListing(FoodListing listing) {
        this.currentListing = listing;

        if (listing != null) {
            listingTitleLabel.setText(listing.getFoodName());

            // Fake routing + duration (you can replace with Google API later)
            routeInfoLabel.setText("Estimated travel time: 18 minutes (Traffic considered)");
        }
    }
     @FXML
    private void handleAccept(ActionEvent event) {
    // 1. Logic for the global FXML button
    handleAcceptDonation(messageContainer); // Pass the main container for the Toast

    if (currentListing != null) {
        currentListing.setStatus("ACCEPTED");
        foodListingDAO.update(currentListing);
        showAlert("Success", "Donation accepted via dashboard.");
    }

    acceptButton.setDisable(true);
    rejectButton.setDisable(true);
}

    @FXML
private void handleReject(ActionEvent event) {
    // 2. Logic for the global FXML button
    if (currentListing != null) {
        currentListing.setStatus("REJECTED");
        foodListingDAO.update(currentListing);
    }
    
    statusLabel.setText("Rejected");
    statusIndicator.setFill(javafx.scene.paint.Color.RED);
    
    showToast((Stage) ((Node)event.getSource()).getScene().getWindow(), "Donation Declined", false);

    acceptButton.setDisable(true);
    rejectButton.setDisable(true);
}

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}