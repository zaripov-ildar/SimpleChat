package ru.starstreet.simplechat.server;

import ru.starstreet.simplechat.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;

import static ru.starstreet.simplechat.Command.*;

public class ClientHandler {
    private final ChatServer chatServer;
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private String nick;
    private boolean clientClosed;
    private boolean authenticated;
    private String login;
    private ChatStore chatStore;

    private final int WAITING_TIME_LIMIT = 120_000;

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
                    initiateChatStore();
                    if (!clientClosed) {
                        sendMsg(HISTORY, chatStore.getHistoryString(100));
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

    private void initiateChatStore() {
        chatStore = new ChatStore(Path.of("src", "main", "resources", "history_" + this.login + ".txt"));

    }

    private void waitConnection() {
        new Thread(() -> {
            try {
                Thread.sleep(WAITING_TIME_LIMIT);
                if (socket.isConnected() && !authenticated) {
                    sendMsg(DISCONNECT);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("Ошибка во время ожидания аутентификации");
            }
        }).start();
    }

    private void readMessages() {
        try {
            String message;
            while (true) {
                String str = in.readUTF();
                Command command = getCommand(str);
                if (command == END) {
                    break;
                }
                if (command == CHANGE_NICK) {
                    String newNick = command.parse(str)[0];
                    chatServer.setNick(this, newNick);
                    continue;
                }
                if (command == PRIVATE_MESSAGE) {
                    String[] params = command.parse(str);
                    String nickTo = params[0];
                    message = params[1];
                    chatServer.sendPrivateMsg(this, nickTo, message);
                    chatStore.addNewMessage(message);
                    continue;
                }
                if (command == MESSAGE) {
                    message = nick + ": " + command.parse(str)[0];
                    chatServer.broadcastMsg(MESSAGE, message);
                    chatStore.addNewMessage(message);
                }
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
                        this.login = login;
                        authenticated = true;
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
        if (command == MESSAGE || command == PRIVATE_MESSAGE) {
            chatStore.addNewMessage(params[0]);
        }
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

    public void setNick(String newNick) {
        this.nick = newNick;
    }
}
