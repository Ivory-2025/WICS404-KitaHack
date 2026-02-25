package Controllers;

import Models.User;
import Models.Vendor;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class VendorDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private VBox uploadCard;
    @FXML private VBox ordersCard;

    private Vendor currentVendor;

    public void setCurrentVendor(Vendor vendor) {
    if (vendor == null) {
        System.err.println("Error: Vendor object is null!");
        return;
    }
    this.currentVendor = vendor;
    welcomeLabel.setText("Welcome back, " + currentVendor.getName() + "!");
    System.out.println("Vendor set: " + currentVendor.getName());
}

    @FXML
    public void initialize() {
        setupCardInteractions(uploadCard);
        setupCardInteractions(ordersCard);
    }

    private void setupCardInteractions(VBox card) {
        if (card == null) return;

        ScaleTransition st = new ScaleTransition(Duration.millis(200), card);

        card.setOnMouseEntered(e -> {
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });

        card.setOnMouseExited(e -> {
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    // ===================== Navigation =====================

    @FXML
    private void handleUploadFoodSurplus(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/UploadFood.fxml"));
            Parent root = loader.load();

            // ✅ Pass vendor session
            UploadFoodController controller = loader.getController();
            controller.setVendor(currentVendor);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();

        } catch (IOException e) {
            System.err.println("Error loading UploadFood.fxml");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewOrders(MouseEvent event) {
        switchScreen(event, "/Views/ViewOrders.fxml");
    }

    @FXML
private void handleLogout(javafx.event.ActionEvent event) {
    // 1. Clear the session so the next user starts fresh
    Models.UserSession.getInstance().logout(); 

    try {
        // 2. Load the Login view
        Parent root = FXMLLoader.load(getClass().getResource("/Views/Login.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // 3. Keep your signature high-class fade transition
        root.setOpacity(0);
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(Duration.millis(500), root);
        ft.setFromValue(0);
        ft.setToValue(1);

        stage.setScene(new Scene(root));
        stage.centerOnScreen();
        ft.play();
        
        System.out.println("✅ Session cleared and user logged out.");
    } catch (IOException e) {
        System.err.println("Error: Could not load Login.fxml");
        e.printStackTrace();
    }
}

    private void switchScreen(MouseEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            root.setOpacity(0);
            javafx.animation.FadeTransition ft =
                    new javafx.animation.FadeTransition(Duration.millis(500), root);
            ft.setFromValue(0);
            ft.setToValue(1);

            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            ft.play();

        } catch (IOException e) {
            System.err.println("Error: Could not load " + fxmlPath);
            e.printStackTrace();
        }
    }
}