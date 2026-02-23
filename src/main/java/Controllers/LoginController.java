package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
    @FXML private Label statusLabel; // Ensure this exists in your FXML

    private UserService userService;

    public LoginController() {
        this.userService = new UserService(new UserDAOImpl());
    }

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Validation Error", "Please enter both email and password.", Alert.AlertType.WARNING);
            return;
        }

        System.out.println("Attempting login with: " + email);
        User loggedInUser = userService.login(email, password);

        if (loggedInUser != null) {
            String role = loggedInUser.getRole();
            System.out.println("DEBUG: User found! Role is: [" + role + "]");
            
            showAlert("Login Successful", "Welcome, " + loggedInUser.getName() + "!", Alert.AlertType.INFORMATION);

            if ("VENDOR".equalsIgnoreCase(role)) {
                // Fixed: Pass 3 arguments as required by your loadDashboard method
                loadDashboard("/Views/VendorDashboard.fxml", "Vendor Dashboard", loggedInUser);
            } else if ("NGO".equalsIgnoreCase(role)) {
                loadDashboard("/Views/NGODashboard.fxml", "NGO Dashboard", loggedInUser);
            } else {
                System.out.println("DEBUG: Role not recognized: " + role);
                if (statusLabel != null) statusLabel.setText("Error: User role not recognized.");
            }
        } else {
            System.out.println("DEBUG: User not found or password incorrect.");
            showAlert("Login Failed", "Invalid email or password.", Alert.AlertType.ERROR);
        }
    }

    private void loadDashboard(String fxmlPath, String title, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // If you need to pass data to controllers, do it here
            // Example: if (user instanceof NGO) { ... }

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleShowSignUp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("SavePlate: Sign Up");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}