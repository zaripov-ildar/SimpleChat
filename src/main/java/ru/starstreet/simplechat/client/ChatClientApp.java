package ru.starstreet.simplechat.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatClientApp extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChatClientApp.class.getResource("client-View.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Chat Client");
        stage.setScene(scene);
        stage.show();
        ChatController controller = fxmlLoader.getController();
        stage.setOnCloseRequest(event -> controller.getClient().closeApp());
    }
}
