package ru.starstreet.simplechat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.starstreet.simplechat.client.Client;

import java.io.IOException;

public class ClientApplication extends Application {
    private static Client client;
    public static void main(String[] args) {
        client = new Client();
        launch();
    }
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApplication.class.getResource("clientForm.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 250, 400);
        stage.setOnHidden(e -> client.close());
        stage.setResizable(false);
        stage.setTitle("Chat");
        stage.setScene(scene);
        stage.show();
    }
}
