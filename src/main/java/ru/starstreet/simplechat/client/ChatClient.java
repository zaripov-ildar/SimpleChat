package ru.starstreet.simplechat.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClient {

    private final ChatController controller;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public ChatClient(ChatController controller) {
        this.controller = controller;
    }

    public void openConnection() throws IOException {
        this.socket = new Socket("localhost", 8189);
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                waitAuth();
                readMessages();
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
            if (msg.startsWith("/authok")) {
                String[] split = msg.split("\\s+");
                String nick = split[1];
                controller.setAuth(true);
                controller.addMessage("Успешная авторизация под ником " + nick);
                break;
            } else if (msg.startsWith("/alert")) {
                controller.addMessage(msg);
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
            if ("/end".equals(message)) {
                controller.setAuth(false);
                break;
            }
            controller.addMessage(message);
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
