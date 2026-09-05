package milo.gui;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import milo.Milo;

/**
 * A JavaFX GUI for Milo, loading its layout from {@code MainWindow.fxml}
 * and handing this session's {@link Milo} instance to the loaded window's
 * controller.
 */
public class Main extends Application {
    /** The chatbot this window talks to. */
    private final Milo milo = new Milo();

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
            AnchorPane ap = fxmlLoader.load();
            Scene scene = new Scene(ap);
            stage.setScene(scene);
            stage.setTitle("Milo");
            fxmlLoader.<MainWindow>getController().setMilo(milo);
            stage.show();
        } catch (IOException e) {
            throw new IllegalStateException("Could not load MainWindow.fxml", e);
        }
    }
}
