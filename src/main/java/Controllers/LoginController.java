package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import DAO.UserDAOImpl;
import Models.NGO;
import Models.User;
import Models.Vendor;
import Services.UserService;

import java.io.IOException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    private UserService userService;

    // No-arg constructor for JavaFX
    public LoginController() {
        this.userService = new UserService(new UserDAOImpl());
    }

    // Optional setter for dependency injection / testing
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Input validation
        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Validation Error", "Please enter both email and password.", Alert.AlertType.WARNING);
            return;
        }

        User loggedInUser = userService.login(email, password);

        if (loggedInUser != null) {
            showAlert("Login Successful", "Welcome, " + loggedInUser.getName() + "!", Alert.AlertType.INFORMATION);

            // Redirect based on role
            if (loggedInUser instanceof Vendor) {
                loadDashboard("/Views/VendorDashboard.fxml", "Vendor Dashboard");
            } else if (loggedInUser instanceof NGO) {
                loadDashboard("/Views/NGODashboard.fxml", "NGO Dashboard");
            } else {
                showAlert("Unknown Role", "User role not recognized.", Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Login Failed", "Invalid email or password.", Alert.AlertType.ERROR);
        }
    }

    // Generic alert helper
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Helper method to load dashboard FXML
    private void loadDashboard(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();

            // Close login window
            emailField.getScene().getWindow().hide();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load dashboard.", Alert.AlertType.ERROR);
        }
    }
}