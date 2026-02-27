package app;

import Database.DatabaseInitializer;
import Utils.Config;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. Initialize Database Tables before the UI loads
            System.out.println("Initializing Database...");
            DatabaseInitializer.initialize();

            // 2. Validate API Configuration
            if ("your_actual_api_key_here".equals(Config.GEMINI_API_KEY)) {
                System.err.println("WARNING: Default API Key detected. AI features will fail.");
            }

            // 3. Load the Login Screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Login.fxml"));
            Parent root = loader.load();
            
            primaryStage.setTitle("SavePlate: SDG Food Surplus Network");
            primaryStage.setScene(new Scene(root));
            primaryStage.setResizable(false);
            primaryStage.show();
            
            System.out.println("Application started successfully.");

        } catch (Exception e) {
            System.err.println("Fatal Error during startup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Standard JavaFX launch call
        launch(args);
    }
}