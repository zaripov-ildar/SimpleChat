package ru.starstreet.simplechat.server;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServer {
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField textField;
    @FXML
    private Button sendButton;

    private List<ClientHandler> clients;
    private final int PORT = 8189;
    private AuthService authService;

    public AuthService getAuthService() {
        return authService;
    }

    public MyServer() {
        try (ServerSocket server = new ServerSocket(PORT)) {
            authService = new BaseAuthService();
            authService.start();
            clients = new ArrayList<>();
            while (true) {
                System.out.println("Сервер ожидает подключения");
                Socket socket = server.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            System.out.println("Ошибка в работе сервера");
        } finally {
            if (authService != null)
                authService.stop();
        }
    }

    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler handler : clients) {
            if (handler.getName().equals(nick))
                return true;
        }
        return false;
    }

    public synchronized void broadcastMsg(String s) {
        for (ClientHandler handler : clients) {
            handler.sendMsg(s);
        }
    }

    public synchronized void sendPrivateMsg(String name, String msg) {
        for (ClientHandler handler : clients) {
            if (handler.getName().equals(name))
                handler.sendMsg(msg);
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public void sendMsg(ActionEvent actionEvent) {
        broadcastMsg(textField.getText());
    }
}
