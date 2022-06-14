package ru.starstreet.simplechat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private final ChatServer chatServer;
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private String nick;

    public String getNick() {
        return nick;
    }

    public ClientHandler(ChatServer chatServer, Socket socket) {
        try {
            this.socket = socket;
            this.chatServer = chatServer;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream((socket.getOutputStream()));
            nick = "";
            new Thread(() -> {
                try {
                    authenticate();
                    readMessages();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException("Проблемы при создании обработчика клиента");
        }
    }

    private void readMessages() {
        try {
            while (true) {
                String str = in.readUTF();
                System.out.println("от " + nick + ": " + str);
                if (str.equals("/end"))
                    break;
                if (str.startsWith("/w")) {
                    String[] parts = str.split("\\s");
                    if (chatServer.isNickBusy(parts[1])) {
                        int commandAndNameLength = parts[0].length() + parts[1].length() + 2;
                        chatServer.sendPrivateMsg(parts[1], "private msg from " + nick + ": " + str.substring(commandAndNameLength));
                    }
                } else
                    chatServer.broadcastMsg(nick + ": " + str);
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении сообщений от клиента");
        }
    }

    private void authenticate() {
        try {
            while (true) {
                String str = in.readUTF();
                if (str.startsWith("/auth")) {
                    String[] parts = str.split("\\s+");
                    if (parts.length != 3) continue;
                    String nick = chatServer.getAuthService().getNickByLoginPass(parts[1], parts[2]);
                    if (nick != null) {
                        if (chatServer.isNickBusy(nick)) {
                            sendMsg("Учётная запись уже используется");
                            continue;
                        }
                        sendMsg("/authok " + nick);
                        this.nick = nick;
                        chatServer.broadcastMsg("Пользователь " + this.nick + " зашёл в чат");
                        chatServer.subscribe(this);
                        break;
                    } else {
                        sendMsg("/alert Неверные логин/пароль");
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка на этапе аутентификации");
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
        sendMsg("/end");
        chatServer.unsubscribe(this);
        chatServer.broadcastMsg(nick + " вышел из чата");
        System.out.println((nick + " вышел из чата"));
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
}
