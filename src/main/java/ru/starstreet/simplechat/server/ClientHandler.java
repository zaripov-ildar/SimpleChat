package ru.starstreet.simplechat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private final MyServer myServer;
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private String name;

    public String getName() {
        return name;
    }

    public ClientHandler(MyServer myServer, Socket socket) {
        try {
            this.socket = socket;
            this.myServer = myServer;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream((socket.getOutputStream()));
            name = "";
            new Thread(() -> {
                try {
                    authentication();
                    readMessages();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException("Проблемы при создании обработчика клиента");
        }
    }

    private void readMessages() throws IOException {
        while (true) {
            String str = in.readUTF();
            System.out.println("от " + name + ": " + str);
            if (str.equals("/end"))
                return;
            if (str.startsWith("/w")) {
                String[] parts = str.split("\\s");
                if (myServer.isNickBusy(parts[1])) {
                    int commandAndNameLength = parts[0].length() + parts[1].length() + 2;
                    myServer.sendPrivateMsg(parts[1], str.substring(commandAndNameLength));
                }
            } else
                myServer.broadcastMsg(name + ": " + str);
        }
    }

    private void authentication() throws IOException {
        while (true) {
            String str = in.readUTF();
            if (str.startsWith("/auth")) {
                String[] parts = str.split("\\s");
                if (parts.length != 3) continue;
                String nick = myServer.getAuthService().getNickByLoginPass(parts[1], parts[2]);
                if (nick != null) {
                    if (!myServer.isNickBusy(nick)) {
                        sendMsg("authok " + nick);
                        name = nick;
                        myServer.broadcastMsg(name + " зашёл в чат");
                        myServer.subscribe(this);
                        return;
                    } else
                        sendMsg("Учётная запись уже используется");
                } else
                    sendMsg("Неверные логин/пароль");
            }
        }
    }

    public void sendMsg(String s) {
        try {
            out.writeUTF(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        myServer.unsubscribe(this);
        myServer.broadcastMsg(name + " вышел из чата");
        System.out.println((name + " вышел из чата"));
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
