package ru.starstreet.simplechat.client;


import javafx.application.Platform;
import ru.starstreet.simplechat.Command;

import static ru.starstreet.simplechat.Command.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClient {

    private final ChatController controller;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean closed;
    private boolean disconnectedByServer;

    public ChatClient(ChatController controller) {
        this.controller = controller;
    }

    public synchronized boolean isClosed() {
        return closed;
    }

    public synchronized void setClosed(boolean closed) {
        this.closed = closed;
    }

    public boolean isDisconnectedByServer() {
        return disconnectedByServer;
    }

    public void setDisconnectedByServer(boolean disconnectedByServer) {
        this.disconnectedByServer = disconnectedByServer;
    }

    public void openConnection() throws IOException {
        this.socket = new Socket("localhost", 8189);
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                waitAuth();
                if (!(isClosed() || isDisconnectedByServer())) {
                    readMessages();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        }).start();

    }

    private void waitAuth() throws IOException {
        while (true) {
            final String msg = in.readUTF();
            Command command = getCommand(msg);
            String[] params = command.parse(msg);
            if (command == AUTH_OK) {
                String nick = params[0];
                controller.setAuth(true);
                controller.addMessage("Успешная авторизация под ником " + nick);
                break;
            }
            if (command == ERROR) {
                Platform.runLater(() -> controller.showError(command.collectMessages(params)));
                continue;
            }
            if (command == END) {
                break;
            }
            if (command == DISCONNECT) {
                sendMessage(END);
                Platform.runLater(() -> {
                    controller.showError("Превышено время ожидания");
                    System.exit(0);
                });
                setDisconnectedByServer(true);
            }
        }

    }

    private void closeConnection() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readMessages() throws IOException {
        while (true) {
            String message = in.readUTF();
            Command command = getCommand(message);
            if (command == END) {
                controller.setAuth(false);
                break;
            }
            String[] params = command.parse(message);
            if (command == ERROR) {
                String msgError = params[0];
                Platform.runLater(() -> controller.showError(msgError));
                continue;
            }
            if (MESSAGE == command) {
                Platform.runLater(() -> controller.addMessage(params[0]));
            }

            if (CLIENTS == command) {
                Platform.runLater(() -> controller.updateClientList(params));
            }
            if (HISTORY == command){
                Platform.runLater(() -> controller.addMessage(params[0]));
            }
        }
    }

    private void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessages(params));
    }

    public void closeApp() {
        setClosed(true);
        sendMessage(END);
    }
}
