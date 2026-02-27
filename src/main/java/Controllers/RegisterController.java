package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import DAO.UserDAOImpl;
import Models.User;
import Services.UserService;
import java.io.IOException;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox; // Added for role selection

    private final UserService userService;

    public RegisterController() {
        this.userService = new UserService(new UserDAOImpl());
    }

    @FXML
    public void initialize() {
        // Populates the dropdown menu for your SDG project roles
        if (roleComboBox != null) {
            roleComboBox.getItems().addAll("VENDOR", "NGO","USER");
        }
    }

    @FXML
    public void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setRole(role);

        if (userService.registerUser(newUser)) {
            showAlert("Success", "Account created successfully!");
            handleBackToLogin(); // Automatically switch back to Login
        } else {
            showAlert("Error", "Registration failed. Email might already be in use.");
        }
    }

    @FXML
    public void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("SavePlate: Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}