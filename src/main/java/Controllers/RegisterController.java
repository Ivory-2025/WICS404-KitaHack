package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import Models.User;
import Services.UserService;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    private final UserService userService;

    // Constructor injection
    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @FXML
    public void handleRegister() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setRole("USER"); // Default role

        if (userService.registerUser(newUser)) {
            showAlert("Success", "Account created successfully!");
            // Logic to switch to Login screen would go here
        } else {
            showAlert("Error", "Registration failed. Email might already be in use.");
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