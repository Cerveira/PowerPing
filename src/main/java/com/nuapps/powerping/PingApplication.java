package com.nuapps.powerping;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class PingApplication extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/ivp_icon.png"))));

        FXMLLoader fxmlLoader = new FXMLLoader(PingApplication.class.getResource("ping-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 522);

        boolean wasSuccessful = new File(System.getenv("TEMP") + "/powerping").mkdir();
        if (wasSuccessful) {
            System.out.println("Pasta powerping criada com sucesso em ~/AppData/Local/Temp");
        }

        stage.setTitle("PowerPing");
        stage.setScene(scene);
        stage.show();
    }
}
