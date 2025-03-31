package dev.uday.GUI;

import atlantafx.base.theme.CupertinoDark;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ServerConfigDialog {
    private static Stage configStage;
    private static final CountDownLatch configLatch = new CountDownLatch(1);
    private static final AtomicReference<String> serverNameResult = new AtomicReference<>("ProjectEXO Server");
    private static final AtomicInteger portResult = new AtomicInteger(2005);
    private static final AtomicBoolean configCompleted = new AtomicBoolean(false);

    public static void showConfigDialog() {

        System.out.println("Showing server configuration dialog...");
        try {
            // Set the theme
            Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());

            // Create dialog stage
            configStage = new Stage();
            configStage.initStyle(StageStyle.DECORATED);
            configStage.setTitle("Server Configuration");
            configStage.setResizable(false);

            // Set application icon
            try {
                Image icon = new Image(Objects.requireNonNull(ServerConfigDialog.class.getClassLoader().getResourceAsStream("icons/logo.png")));
                configStage.getIcons().add(icon);
            } catch (Exception e) {
                System.err.println("Could not load configuration dialog icon: " + e.getMessage());
            }

            // Create the dialog layout
            GridPane grid = new GridPane();
            grid.setAlignment(Pos.CENTER);
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(25, 25, 25, 25));

            // Add header
            Text sceneTitle = new Text("ProjectEXO Server Configuration");
            sceneTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
            grid.add(sceneTitle, 0, 0, 2, 1);

            // Server name input
            Label nameLabel = new Label("Server Name:");
            grid.add(nameLabel, 0, 1);

            TextField serverNameField = new TextField();
            serverNameField.setText(serverNameResult.get());
            grid.add(serverNameField, 1, 1);

            // Port input
            Label portLabel = new Label("Server Port:");
            grid.add(portLabel, 0, 2);

            TextField portField = new TextField();
            portField.setText(String.valueOf(portResult.get()));
            grid.add(portField, 1, 2);

            // Buttons
            Button startButton = new Button("Start Server");
            Button cancelButton = new Button("Cancel");

            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
            buttonBox.getChildren().addAll(startButton, cancelButton);
            grid.add(buttonBox, 1, 4);

            // Event handlers
            startButton.setOnAction(event -> {
                // Validate port
                try {
                    int port = Integer.parseInt(portField.getText().trim());
                    if (port < 1024 || port > 65535) {
                        showAlert("Invalid Port", "Port must be between 1024 and 65535.");
                        return;
                    }
                    portResult.set(port);
                } catch (NumberFormatException e) {
                    showAlert("Invalid Port", "Please enter a valid port number.");
                    return;
                }

                // Validate server name
                String serverName = serverNameField.getText().trim();
                if (serverName.isEmpty()) {
                    showAlert("Invalid Name", "Server name cannot be empty.");
                    return;
                }
                serverNameResult.set(serverName);

                // Set config completed and close
                configCompleted.set(true);
                configStage.close();
                configStage = null;
                configLatch.countDown();
            });

            cancelButton.setOnAction(event -> {
                configStage.close();
                configLatch.countDown();
                Platform.exit();
            });

            // Handle window close
            configStage.setOnCloseRequest(event -> {
                configLatch.countDown();
                Platform.exit();
            });

            Scene scene = new Scene(grid, 400, 250);
            configStage.setScene(scene);
            configStage.showAndWait();
            configStage.show();
        } catch (Exception e) {
            System.err.println("Error showing config dialog: " + e.getMessage());
            e.printStackTrace();
            configLatch.countDown();
        }


        try {
            configLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static String getServerName() {
        return serverNameResult.get();
    }

    public static int getPort() {
        return portResult.get();
    }

    public static boolean isConfigCompleted() {
        return configCompleted.get();
    }
}