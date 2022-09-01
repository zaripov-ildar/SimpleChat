package ru.starstreet.simplechat.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.Optional;

import static ru.starstreet.simplechat.Command.*;

public class ChatController {
    @FXML
    private TextField newNickField;
    @FXML
    private ListView<String> clientList;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passField;
    @FXML
    private HBox authBox;
    @FXML
    private HBox messageBox;
    @FXML
    private TextArea messageArea;
    @FXML
    private TextField messageField;
    private ChatClient client;

    private String selectedNick;

    public ChatController() {
        initiate();
    }

    private void initiate() {
        this.client = new ChatClient(this);
        while (true) {
            try {
                client.openConnection();
                break;
            } catch (IOException e) {
                showNotification();
            }
        }
    }

    private void showNotification() {
        Alert alert = new Alert(Alert.AlertType.ERROR,
                "Не могу подключиться к серверу.\n" +
                        "Проверьте, что сервер запущен",
                new ButtonType("Попробовать снова", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Выйти", ButtonBar.ButtonData.CANCEL_CLOSE)
        );
        alert.setTitle("Ошибка подключения");
        final Optional<ButtonType> answer = alert.showAndWait();
        Boolean isExit = answer.map(select ->
                select.getButtonData().isCancelButton()).orElse(false);
        if (isExit) {
            System.exit(0);
        }
    }

    @FXML
    private void clickSendButton() {
        final String message = messageField.getText();
        if (message.isBlank()) {
            return;
        }
        if (selectedNick != null) {
            client.sendMessage(PRIVATE_MESSAGE, selectedNick, message);
            selectedNick = null;
        } else {
            client.sendMessage(MESSAGE, message);
        }
        messageField.clear();
        messageField.requestFocus();
    }

    public void addMessage(String message) {
        messageArea.appendText(message + "\n");
    }

    public void signInBtnClick() {
        client.sendMessage(AUTH, loginField.getText(), passField.getText());
    }

    public void setAuth(boolean success) {
        authBox.setVisible(!success);
        messageBox.setVisible(success);

    }

    public void showError(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR, errorMessage, new ButtonType("Ok"));
        alert.setTitle("Error!");
        alert.showAndWait();
    }

    public void selectClient(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            String selectedNick = clientList.getSelectionModel().getSelectedItem();
            if (selectedNick != null && !selectedNick.isEmpty())
                this.selectedNick = selectedNick;
        }
    }

    public void updateClientList(String[] clients) {
        clientList.getItems().clear();
        clientList.getItems().addAll(clients);
    }

    public void logout() {
        client.sendMessage(END);
        messageArea.clear();
        initiate();
    }

    public ChatClient getClient() {
        return client;
    }

    public void setNickName(MouseEvent mouseEvent) {
        TextInputDialog tid = new TextInputDialog("New nickname");
        Optional<String> result = tid.showAndWait();
        final String[] newNick = new String[1];
        result.ifPresent(s -> newNick[0] = s);
        client.sendMessage(CHANGE_NICK, newNick);
    }
}
