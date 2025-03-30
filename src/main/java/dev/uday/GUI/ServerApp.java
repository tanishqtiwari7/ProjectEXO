package dev.uday.GUI;

import atlantafx.base.theme.CupertinoDark;
import com.pixelduke.window.ThemeWindowManagerFactory;
import com.pixelduke.window.Win11ThemeWindowManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.Objects;

public class ServerApp extends Application {
    private Stage mainStage;

    @Override
    public void start(Stage stage) {
        showSplashScreen();
    }

    private void showSplashScreen() {
        try {
            // Create a new stage for the splash screen
            Stage splashStage = new Stage();
            splashStage.initStyle(StageStyle.UNDECORATED);

            try {
                Image icon = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("icons/logo.png")));
                splashStage.getIcons().add(icon);
            } catch (Exception e) {
                System.err.println("Could not load splash screen icon: " + e.getMessage());
            }

            // Load the splash video
            URL videoUrl = getClass().getClassLoader().getResource("splash.mp4");
            if (videoUrl == null) {
                // If video isn't found, skip splash and show main UI
                Platform.runLater(this::showMainUI);
                return;
            }

            Media media = new Media(videoUrl.toExternalForm());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            MediaView mediaView = new MediaView(mediaPlayer);

            // Create a container for the video
            StackPane root = new StackPane();
            root.getChildren().add(mediaView);

            // Set preferred size for the video
            mediaView.setFitWidth(852);
            mediaView.setFitHeight(480);

            // Create scene and display
            Scene scene = new Scene(root, 852, 480);
            splashStage.setScene(scene);

            // Center the splash screen
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            splashStage.setX((screenBounds.getWidth() - 852) / 2);
            splashStage.setY((screenBounds.getHeight() - 480) / 2);

            // Show the splash screen
            splashStage.show();

            // Play the video
            mediaPlayer.play();

            // When video ends, show the main UI and close the splash screen
            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.stop();
                splashStage.close();
                Platform.runLater(this::showMainUI);
            });

            // If something goes wrong with the video, close it after 5 seconds
            mediaPlayer.setOnError(() -> {
                System.err.println("Error playing splash video: " + mediaPlayer.getError());
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    Platform.runLater(() -> {
                        splashStage.close();
                        showMainUI();
                    });
                }).start();
            });
        } catch (Exception e) {
            System.err.println("Error showing splash screen: " + e.getMessage());
            Platform.runLater(this::showMainUI);
        }
    }

    private void showMainUI() {
        try {
            // Set the theme
            Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());

            // Load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("ServerUI.fxml"));
            Parent root = loader.load();

            // Create scene
            Scene scene = new Scene(root, 800, 600);

            // Create and configure main stage
            mainStage = new Stage();
            mainStage.setTitle("ProjectEXO Server");

            // Set application icon
            try {
                Image icon = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("icons/logo.png")));
                mainStage.getIcons().add(icon);
            } catch (Exception e) {
                System.err.println("Could not load application icon: " + e.getMessage());
            }

            mainStage.setScene(scene);
            mainStage.show();

            // Apply Windows 11 theme settings AFTER the stage is shown
            try {
                Win11ThemeWindowManager manager =
                        (Win11ThemeWindowManager) ThemeWindowManagerFactory.create();
                manager.setWindowCornerPreference(mainStage, Win11ThemeWindowManager.CornerPreference.ROUND);
                manager.setDarkModeForWindowFrame(mainStage, true);
            } catch (Exception e) {
                System.err.println("Error setting window theme: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error starting main UI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void startServerApp() {
        launch();
    }
}