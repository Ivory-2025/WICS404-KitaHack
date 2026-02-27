package Controllers;

import Services.AnalyticsService;
import Models.Vendor;
import Models.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

public class VendorAnalyticsController {
    @FXML private Label lblAvgSurplus;
    @FXML private Label lblCO2;
    @FXML private TextArea txtAiInsight;

    private final AnalyticsService analyticsService = new AnalyticsService();

    @FXML
    public void initialize() {
        Vendor currentVendor = UserSession.getInstance().getVendor();
        if (currentVendor != null) {
            double avg = analyticsService.getSevenDayAverage(currentVendor);
            double co2 = analyticsService.calculateCO2Impact(currentVendor);
            
            lblAvgSurplus.setText(String.format("%.1f", avg));
            lblCO2.setText(String.format("%.1f kg", co2));
            txtAiInsight.setText(analyticsService.getWeeklyInsight(currentVendor));
        }
    }

    @FXML
    private void handleBack() {
        try {
            javafx.scene.Parent root = FXMLLoader.load(getClass().getResource("/Views/VendorDashboard.fxml"));
            Stage stage = (Stage) lblAvgSurplus.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("SavePlate - Vendor Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
