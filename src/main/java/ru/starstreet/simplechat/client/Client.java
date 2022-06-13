package ru.starstreet.simplechat.client;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean authorized;
    @FXML
    private TextArea chatArea;
    @FXML
    private Button sendButton;
    @FXML
    private TextField textField;

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public Client() {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            setAuthorized(false);
            Thread t = new Thread(() -> {
                try {
                    while (true) {
                        String strFromServer = in.readUTF();
                        if (strFromServer.startsWith("/authok")) {
                            setAuthorized(true);
                            break;
                        }
                        chatArea.appendText(strFromServer + "\n");
                    }
                    while (true) {
                        String strFromServer = in.readUTF();
                        if (strFromServer.equalsIgnoreCase("/end")) {
                            break;
                        }
                        chatArea.appendText(strFromServer);
                        chatArea.appendText("\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void sendMsg() {
        try {
            String text = textField.getText();
            out.writeUTF(text);
            textField.clear();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            out.writeUTF("/end");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
//        try {
//            in.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            out.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
