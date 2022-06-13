package ru.starstreet.simplechat.client;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.starstreet.simplechat.HelloApplication;

import java.io.IOException;

public class Main extends Application {
    private static Client client;
    public static void main(String[] args) {
        client = new Client();
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("clientForm.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 250, 400);
        stage.setOnHidden(e -> client.close());
        stage.setResizable(false);
        stage.setTitle("Chat");
        stage.setScene(scene);
        stage.show();
    }
}
