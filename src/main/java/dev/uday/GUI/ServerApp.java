package dev.uday.GUI;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());

        // Load FXML
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("ServerUI.fxml"));
        Parent root = loader.load();

        // Create scene
        Scene scene = new Scene(root, 800, 600);

        // Set the title of the window
        stage.setTitle("ProjectEXO Server");
        stage.setScene(scene);
        stage.show();
    }

    public static void startServerApp() {
        launch();
    }
}