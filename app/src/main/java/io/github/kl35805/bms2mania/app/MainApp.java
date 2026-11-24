package io.github.kl35805.bms2mania.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * bms2mania JavaFX 애플리케이션.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        String info = "bms2mania\n"
                + "Java " + System.getProperty("java.version")
                + " / JavaFX " + System.getProperty("javafx.version");

        Scene scene = new Scene(new StackPane(new Label(info)), 960, 640);

        stage.setTitle("bms2mania");
        stage.setScene(scene);
        stage.show();
    }
}