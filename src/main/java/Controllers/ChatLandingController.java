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
    }

    public void stopTimer() {
        if (autoRefreshTimeline != null) autoRefreshTimeline.stop();
    }

    private void loadMessages() {
        messageContainer.getChildren().clear();
        int vendorId = UserSession.getInstance().getVendor().getUserId();
        List<Message> history = messageDAO.getChatHistory(vendorId, selectedNGO.getUserId());
        for (Message msg : history) {
            addMessageToUI(msg.getContent(), msg.getSenderId() == vendorId);
        }
        chatScrollPane.setVvalue(1.0);
    }

    private void addMessageToUI(String content, boolean isVendor) {
    VBox bubbleContainer = new VBox(8);
    bubbleContainer.setAlignment(isVendor ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

    VBox bubble = new VBox(12);
    bubble.setStyle("-fx-background-color: " + (isVendor ? "#1A1A1A" : "#FFFFFF") + "; " +
                    "-fx-padding: 18; -fx-background-radius: 20; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 5);");

    Label textLabel = new Label(content);
    textLabel.setWrapText(true);
    textLabel.setMaxWidth(350);
    textLabel.setStyle("-fx-text-fill: " + (isVendor ? "white" : "#1A1A1A") + "; " +
                       "-fx-font-family: 'System'; -fx-font-weight: 600; -fx-font-size: 15px;");
    
    bubble.getChildren().add(textLabel);

    // Interactive card logic for NGOs
    if (!isVendor && content.contains("New Surplus")) {
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_LEFT);
        
        // 1. Calculate Traffic Duration for the card
        // This simulates your RoutingService logic for the demo
        String duration = "18 mins (Moderate Traffic)"; 
        Label trafficInfo = new Label("🚗 " + duration);
        trafficInfo.setStyle("-fx-text-fill: #718096; -fx-font-size: 12px; -fx-font-weight: 700;");

        // 2. Add the Map Icon
        ImageView mapIcon = new ImageView(new Image(getClass().getResourceAsStream("/Assets/map_icon.png")));
        mapIcon.setFitHeight(16);
        mapIcon.setFitWidth(16);

        Button acceptBtn = new Button("Accept", mapIcon);
        acceptBtn.setGraphicTextGap(8);
        Button rejectBtn = new Button("Reject");

        // 3. Tooltip and Styling
        Tooltip routeTooltip = new Tooltip("View live route on Google Maps");
        routeTooltip.setShowDelay(Duration.millis(200));
        acceptBtn.setTooltip(routeTooltip);

        String acceptIdleStyle = "-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 12; -fx-padding: 8 20; -fx-cursor: hand;";
        String acceptHoverStyle = "-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 12; -fx-padding: 8 20; -fx-cursor: hand;";
        
        acceptBtn.setStyle(acceptIdleStyle);
        rejectBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-font-weight: 800; -fx-border-color: #EF4444; -fx-border-radius: 12; -fx-cursor: hand;");

        acceptBtn.setOnMouseEntered(e -> acceptBtn.setStyle(acceptHoverStyle));
        acceptBtn.setOnMouseExited(e -> acceptBtn.setStyle(acceptIdleStyle));

        // 4. Handlers
        applyPulseAnimation(acceptBtn);
        acceptBtn.setOnAction(e -> handleAcceptDonation(bubbleContainer));
        
        rejectBtn.setOnAction(e -> {
            statusLabel.setText("Rejected");
statusIndicator.setFill(javafx.scene.paint.Color.RED);

distanceLabel.setText("");
etaLabel.setText("");

messageContainer.getChildren().remove(bubbleContainer);

showToast((Stage) bubbleContainer.getScene().getWindow(),
        "Donation Declined", false);
        });

        actions.getChildren().addAll(acceptBtn, rejectBtn);
        bubble.getChildren().addAll(trafficInfo, actions); // Add traffic duration to card
    }

    bubbleContainer.getChildren().add(bubble);
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

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Transport Verification");
    alert.setHeaderText("Vehicle Ready?");
    alert.setContentText("Do you have a transport vehicle ready for pickup?");

    alert.showAndWait().ifPresent(response -> {

        if (response == ButtonType.OK) {

            try {
                Models.Vendor vendor = Models.UserSession.getInstance().getVendor();

                if (vendor == null) {
                    showToast((Stage) container.getScene().getWindow(),
                            "Session Error", false);
                    return;
                }

                double vLat = vendor.getLatitude();
                double vLon = vendor.getLongitude();

                // Simulated routing calculation
                double distance = 4.8; // replace with real API later
                int eta = 16; // replace with traffic API later

                // Update header route info
                distanceLabel.setText("Distance: " + distance + " km");
                etaLabel.setText("ETA: " + eta + " mins (Traffic)");

                // Update status
                statusLabel.setText("Accepted");
                statusIndicator.setFill(javafx.scene.paint.Color.LIMEGREEN);

                showToast((Stage) container.getScene().getWindow(),
                        "Donation Accepted ✔", true);

            } catch (Exception e) {
                e.printStackTrace();
                showToast((Stage) container.getScene().getWindow(),
                        "Error processing acceptance", false);
            }

        } else {
            showToast((Stage) container.getScene().getWindow(),
                    "Action Cancelled", false);
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
        ngoListView.getItems().clear();
        List<Models.NGO> ngos = ngoDAO.getAllNGOs(); 
        ObservableList<Models.NGO> items = FXCollections.observableArrayList(ngos);
        ngoListView.setItems(items);
        ngoListView.setCellFactory(lv -> new ListCell<Models.NGO>() {
            @Override
            protected void updateItem(Models.NGO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(item.getOrganizationName());
                    setStyle("-fx-font-family: 'System'; -fx-font-weight: 700; -fx-font-size: 15px; -fx-padding: 15; -fx-text-fill: #1A1A1A;");
                }
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
        VBox container = (VBox) ((Node) event.getSource()).getParent().getParent(); // bubble container
    handleAcceptDonation(container);
        if (currentListing == null) {
            showAlert("Error", "No listing found.");
            return;
        }

        // Update status
        currentListing.setStatus("ACCEPTED");
        foodListingDAO.update(currentListing);

        showAlert("Success", "You have accepted this food donation.\nRouting info sent.");

        // Disable buttons after action
        acceptButton.setDisable(true);
        rejectButton.setDisable(true);
    }

    private void handleReject(ActionEvent event) {
        VBox container = (VBox) ((Node) event.getSource()).getParent().getParent(); // bubble container
    messageContainer.getChildren().remove(container);
    showToast((Stage) container.getScene().getWindow(), "Donation Declined", false);
        if (currentListing == null) {
            showAlert("Error", "No listing found.");
            return;
        }

        currentListing.setStatus("REJECTED");
        foodListingDAO.update(currentListing);

        showAlert("Rejected", "You have rejected this food donation.");

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