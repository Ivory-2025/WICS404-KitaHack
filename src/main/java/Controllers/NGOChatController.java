package Controllers;

import DAO.FoodListingDAO;
import DAO.MessageDAO;
import DAO.VendorDAO;
import Models.FoodListing;
import Models.Message;
import Models.NGO;
import Models.UserSession;
import Models.Vendor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.Node;
import Services.*;
import java.util.List;

public class NGOChatController {
    @FXML private ListView<Vendor> vendorListView; // Now listing Vendors
    @FXML private Label chatHeaderLabel;
    @FXML private VBox messageContainer;
    @FXML private TextField messageInputField;
    @FXML private ScrollPane chatScrollPane;
    @FXML private Label statusLabel;
    @FXML private javafx.scene.shape.Circle statusIndicator;

    private final FoodListingDAO foodListingDAO = new FoodListingDAO();
    private final VendorDAO vendorDAO = new VendorDAO(); // Use VendorDAO instead of NGODAO
    private final MessageDAO messageDAO = new MessageDAO();
    private Vendor selectedVendor;

    @FXML
    public void initialize() {
        if (UserSession.getInstance().getNGO() == null) {
            System.err.println("CRITICAL: NGO Session is empty!");
        }

        // 1. Set cell factory for Vendor display
        vendorListView.setCellFactory(lv -> new ListCell<Models.Vendor>() {
            @Override
            protected void updateItem(Models.Vendor vendor, boolean empty) {
                super.updateItem(vendor, empty);
                setText((empty || vendor == null) ? null : vendor.getStoreName());
            }
        });

        loadActiveChats();

        // 2. Selection listener for Vendors
        vendorListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedVendor = newVal;
                chatHeaderLabel.setText(newVal.getStoreName());
                loadMessages();
                
                statusLabel.setText("Active Conversation");
                statusIndicator.setFill(javafx.scene.paint.Color.GREEN);
            }
        });
    }

    private void loadMessages() {
        messageContainer.getChildren().clear();
        if (selectedVendor == null) return;

        int ngoId = UserSession.getInstance().getNGO().getUserId();
        // Fetch history between this NGO and the selected Vendor
        List<Message> history = messageDAO.getChatHistory(selectedVendor.getUserId(), ngoId);

        for (Message msg : history) {
    boolean isFromVendor = (msg.getSenderId() != ngoId);
    // Wrap the UI addition in Platform.runLater
    javafx.application.Platform.runLater(() -> {
        addMessageToUI(msg.getContent(), isFromVendor);
    });
}
        chatScrollPane.setVvalue(1.0);
    }

    private void addMessageToUI(String content, boolean isFromVendor) {
        VBox bubbleContainer = new VBox(8);
        // Align Vendor messages (Incoming) to the Left, NGO messages (You) to the Right
        bubbleContainer.setAlignment(isFromVendor ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);

        VBox card = new VBox(15);
        // White for Vendor (incoming), Blue-ish/Green for NGO (outgoing)
        card.setStyle("-fx-background-color: " + (isFromVendor ? "#FFFFFF" : "#e4f1de") + "; " +
                      "-fx-padding: 20; -fx-background-radius: 20; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        if (content.toLowerCase().contains("new surplus alert")) {
            setupSurplusCard(card, content, bubbleContainer, isFromVendor);
        } else {
            Label text = new Label(content);
            text.setWrapText(true);
            text.setStyle("-fx-font-size: 14px;");
            card.getChildren().add(text);
        }

        bubbleContainer.getChildren().add(card);
        messageContainer.getChildren().add(bubbleContainer);
    }

    private void setupSurplusCard(VBox card, String content, VBox bubbleContainer, boolean isFromVendor) {
        Label header = new Label("NEW SURPLUS ALERT");
        header.setStyle("-fx-font-weight: 900; -fx-font-size: 18px; -fx-text-fill: #145694;");

        VBox foodDetails = new VBox(5);
        foodDetails.setStyle("-fx-background-color: #F1F5F9; -fx-padding: 15; -fx-background-radius: 12;");
        Label detailText = new Label(content.split("✨ AI Analysis:")[0]);
        detailText.setWrapText(true);
        foodDetails.getChildren().add(detailText);

        // Buttons only for the NGO (Receiver)
        if (isFromVendor) {
            HBox actions = new HBox(15);
            actions.setAlignment(Pos.CENTER);
            Button accept = new Button("Accept");
            accept.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 20; -fx-padding: 10 30;");
            
            accept.setOnAction(e -> {
                FoodListing listing = foodListingDAO.getLatestListingByVendor(selectedVendor.getUserId());
                handleAcceptDonation(bubbleContainer, listing);
            });

            actions.getChildren().add(accept);
            card.getChildren().addAll(header, foodDetails, actions);
        } else {
            card.getChildren().addAll(header, foodDetails);
        }
    }

    @FXML
    private void handleSendMessage() {
        String text = messageInputField.getText();
        if (text == null || text.trim().isEmpty() || selectedVendor == null) return;

        int ngoId = UserSession.getInstance().getNGO().getUserId();
        messageDAO.sendMessage(ngoId, selectedVendor.getUserId(), text.trim());
        addMessageToUI(text.trim(), false); // false = not from vendor, it's from the NGO
        messageInputField.clear();
    }

    private void loadActiveChats() {
        List<Models.Vendor> vendors = vendorDAO.getAllVendors();
        ObservableList<Vendor> observableVendors = FXCollections.observableArrayList(vendors);

        javafx.application.Platform.runLater(() -> {
            vendorListView.getSelectionModel().clearSelection();
            vendorListView.setItems(observableVendors);
            if (!observableVendors.isEmpty()) {
                vendorListView.getSelectionModel().selectFirst();
            }
        });
    }

    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/Views/NGOMainDashboard.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    // Reuse your existing toast method
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

    private void handleAcceptDonation(VBox container, Models.FoodListing listing) {
        if (listing == null) return;
        Services.RoutingService rs = new Services.RoutingService();
        String mapsLink = rs.generateGoogleMapsLink(listing, UserSession.getInstance().getNGO());
        
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(mapsLink));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}