package Controllers;

import DAO.UserDAOImpl;
import DAO.VendorDAO;
import DAO.NGODAO;
import Models.NGO;
import Models.User;
import Models.UserSession;
import Models.Vendor;
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

    private final UserService userService;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    private final VendorDAO vendorDAO = new VendorDAO(); 
    private final NGODAO ngoDAO = new NGODAO(); 

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

    if (loggedInUser == null) {
        showToast("Login Failed: Invalid credentials.", "#FEE2E2", "#991B1B", "❌");
        return;
    }

    UserSession session = UserSession.getInstance();

    // ✅ HANDLE VENDOR LOGIN
    if ("VENDOR".equalsIgnoreCase(loggedInUser.getRole())) {

        Vendor vendor = vendorDAO.getVendorByUserId(loggedInUser.getUserId());
        if (vendor == null) {
            showToast("Error: Vendor profile not found in DB.", "#FEE2E2", "#991B1B", "❌");
            return;
        }

        session.setVendor(vendor);  // 🔥 THIS FIXES YOUR NULL PROBLEM
        showToast("Welcome back, " + vendor.getName() + "! ✨", "#D1FAE5", "#065F46", "✅");

        loadDashboard("/Views/VendorDashboard.fxml", loggedInUser);
    }

    // ✅ HANDLE NGO LOGIN
    else if ("NGO".equalsIgnoreCase(loggedInUser.getRole())) {

        NGO ngo = ngoDAO.getNGOByUserId(loggedInUser.getUserId());
        if (ngo == null) {
            showToast("Error: NGO profile not found in DB.", "#FEE2E2", "#991B1B", "❌");
            return;
        }

        session.setNGO(ngo);
        showToast("Welcome back, " + ngo.getName() + "! ✨", "#D1FAE5", "#065F46", "✅");

        loadDashboard("/Views/NGODashboard.fxml", loggedInUser);
    }
}
    private void loadDashboard(String fxmlPath, User loggedInUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Pass the correct vendor/NGO to dashboard controller
            if ("VENDOR".equalsIgnoreCase(loggedInUser.getRole())) {
                VendorDashboardController controller = loader.getController();
                controller.setCurrentVendor(UserSession.getInstance().getVendor());
            } else if ("NGO".equalsIgnoreCase(loggedInUser.getRole())) {
                NGODashboardController controller = loader.getController();
                controller.setCurrentNGO(UserSession.getInstance().getNGO());
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
            e.printStackTrace();
            showToast("Critical Error: Could not load dashboard.", "#FEE2E2", "#991B1B", "🚫");
        }
    }

    private void showToast(String message, String bgColor, String textColor, String icon) {
        Stage stage = (Stage) emailField.getScene().getWindow();
        Popup popup = new Popup();

        HBox toastRoot = new HBox(12);
        toastRoot.setAlignment(Pos.CENTER_LEFT);
        toastRoot.setStyle(String.format(
                "-fx-background-color: %s; -fx-background-radius: 20; -fx-padding: 12 25;", bgColor));

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle(String.format(
                "-fx-text-fill: %s; -fx-font-family: 'Inter'; -fx-font-weight: 800; -fx-font-size: 13px;", textColor));

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