package ru.starstreet.simplechat.server;

import ru.starstreet.simplechat.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static ru.starstreet.simplechat.Command.*;

public class ClientHandler {
    private final ChatServer chatServer;
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private String nick;
    private boolean clientClosed;

    private final int WAITING_TIME_LIMIT = 10_000;

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
                    waitConnection();
                    authenticate();
                    if (!clientClosed) {
                        readMessages();
                    }
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException("Проблемы при создании обработчика клиента");
        }
    }

    private void waitConnection() {
        new Thread(()->{
            try {
                Thread.sleep(WAITING_TIME_LIMIT);
                sendMsg(DISCONNECT);
            } catch (InterruptedException e) {
                throw new RuntimeException("Ошибка во время ожидания аутентификации");
            }
        }).start();
    }

    private void readMessages() {
        try {
            while (true) {
                String str = in.readUTF();
                Command command = getCommand(str);
                if (command == END) {
                    break;
                }
                if (command == PRIVATE_MESSAGE) {
                    String[] params = command.parse(str);
                    String nickTo = params[0];
                    String message = params[1];
                    chatServer.sendPrivateMsg(this, nickTo, message);
                    continue;
                }
                chatServer.broadcastMsg(MESSAGE, nick + ": " + command.parse(str)[0]);
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении сообщений от клиента");
        }
    }

    private void authenticate() {
        try {
            while (true) {
                String str = in.readUTF();
                Command command = getCommand(str);
                if (command == AUTH) {
                    String[] params = command.parse(str);
                    String login = params[0];
                    String pass = params[1];
                    String nick = chatServer.getAuthService().getNickByLoginPass(login, pass);
                    if (nick != null) {
                        if (chatServer.isNickBusy(nick)) {
                            sendMsg(ERROR, "Учётная запись уже используется");
                            continue;
                        }
                        sendMsg(AUTH_OK, nick);
                        this.nick = nick;
                        chatServer.broadcastMsg(MESSAGE, "Пользователь " + this.nick + " зашёл в чат");
                        chatServer.subscribe(this);
                        break;
                    } else {
                        sendMsg(ERROR, "Неверные логин/пароль");
                    }
                }
                if (command == END) {
                    clientClosed = true;
                    break;
                }

            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка на этапе аутентификации");
        }
    }

    public void sendMsg(Command command, String... params) {
        sendMsg(command.collectMessages(params));
    }

    private void sendMsg(String s) {
        try {
            out.writeUTF(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        sendMsg(END);
        if (socket != null && socket.isConnected()) {
            chatServer.unsubscribe(this);
            chatServer.broadcastMsg(MESSAGE, nick + " вышел из чата");
        }
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
