package Controllers;

import DAO.UserDAOImpl;
import Models.User;
import Services.UserService;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    private final UserService userService;

    public LoginController() {
        this.userService = new UserService(new UserDAOImpl());
    }

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Required: Please enter both credentials.", "#FEE2E2", "#991B1B", "⚠️");
            return;
        }

        User loggedInUser = userService.login(email, password);

        if (loggedInUser != null) {
            String role = loggedInUser.getRole();

            showToast("Welcome back, " + loggedInUser.getName() + "! ✨", "#D1FAE5", "#065F46", "✅");

            // Smooth transition after toast
            PauseTransition delay = new PauseTransition(Duration.seconds(0.8));
            delay.setOnFinished(e -> {
                String viewPath;
                if ("VENDOR".equalsIgnoreCase(role)) {
                    viewPath = "/Views/VendorDashboard.fxml";
                } else {
                    viewPath = "/Views/NGODashboard.fxml";
                }
                loadDashboard(viewPath, loggedInUser); // Pass logged-in user
            });
            delay.play();
        } else {
            showToast("Login Failed: Invalid credentials.", "#FEE2E2", "#991B1B", "❌");
        }
    }

    private void loadDashboard(String fxmlPath, User loggedInUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/VendorDashboard.fxml"));
            Parent root = loader.load();

            // Pass logged-in user to dashboard controller
            if (loggedInUser.getRole().equalsIgnoreCase("VENDOR")) {
                Controllers.VendorDashboardController controller = loader.getController();
                controller.setCurrentVendor(loggedInUser);
            }

            Stage stage = (Stage) emailField.getScene().getWindow();

            // Fade-in transition
            root.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            stage.setScene(new Scene(root));
            stage.setTitle("SavePlate - " + loggedInUser.getRole() + " Dashboard");
            stage.centerOnScreen();
        } catch (IOException e) {
            showToast("Critical Error: Could not load dashboard.", "#FEE2E2", "#991B1B", "🚫");
            e.printStackTrace();
        }
    }

    /**
     * High-Class Toast with Icon
     */
    private void showToast(String message, String bgColor, String textColor, String icon) {
        Stage stage = (Stage) emailField.getScene().getWindow();
        Popup popup = new Popup();

        HBox toastRoot = new HBox(12);
        toastRoot.setAlignment(Pos.CENTER_LEFT);
        toastRoot.setStyle(String.format(
            "-fx-background-color: %s; -fx-background-radius: 20; -fx-padding: 12 25;",
            bgColor
        ));

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle(String.format(
            "-fx-text-fill: %s; -fx-font-family: 'Inter'; -fx-font-weight: 800; -fx-font-size: 13px;",
            textColor
        ));

        toastRoot.getChildren().addAll(iconLabel, messageLabel);
        toastRoot.setEffect(new DropShadow(20, Color.web("#0000000D")));
        popup.getContent().add(toastRoot);
        popup.show(stage);

        toastRoot.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
            popup.setX(stage.getX() + stage.getWidth()/2 - toastRoot.getWidth()/2);
            popup.setY(stage.getY() + stage.getHeight() - 100);
        });

        FadeTransition in = new FadeTransition(Duration.millis(300), toastRoot);
        in.setFromValue(0); in.setToValue(1);
        PauseTransition stay = new PauseTransition(Duration.seconds(2.5));
        FadeTransition out = new FadeTransition(Duration.millis(400), toastRoot);
        out.setFromValue(1); out.setToValue(0);
        out.setOnFinished(e -> popup.hide());

        new SequentialTransition(in, stay, out).play();
    }

    @FXML
    public void handleShowSignUp() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Views/Register.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("SavePlate - Join the Mission");
        } catch (IOException e) {
            showToast("Navigation Error", "#FEE2E2", "#991B1B", "⚠️");
        }
    }
}