package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import Models.NGO;
import java.io.IOException;

public class NGODashboardController {

    @FXML private Label welcomeLabel;
    private NGO currentNGO;

    /**
     * This method is called by LoginController to pass the user data
     */
    public void setCurrentNGO(NGO ngo) {
        this.currentNGO = ngo;
        if (welcomeLabel != null && ngo != null) {
            welcomeLabel.setText("Welcome back, " + ngo.getName() + "!");
        }
    }

    @FXML
    public void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("SavePlate: Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}