package ru.starstreet.simplechat.server;


import ru.starstreet.simplechat.Command;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ChatServer {

    private final Map<String, ClientHandler> clients;
    private final int PORT = 8189;
    private AuthService authService;

    public AuthService getAuthService() {
        return authService;
    }

    public ChatServer() {
        this.clients = new HashMap<>();
    }

    public void run() {
        try (ServerSocket server = new ServerSocket(PORT);
             AuthService authService = new BaseAuthService()) {
            this.authService = authService;
            while (true) {
                System.out.println("Сервер ожидает подключения");
                final Socket socket = server.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            System.out.println("Ошибка в работе сервера");
        }
    }

    public synchronized boolean isNickBusy(String nick) {
        broadcastClientsList();
        return clients.get(nick) != null;
    }

    public synchronized void broadcastMsg(Command command, String message) {
        for (ClientHandler handler : clients.values()) {
            handler.sendMsg(command, message);
        }
    }


    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.put(clientHandler.getNick(), clientHandler);
        broadcastClientsList();
        System.out.println(clientHandler.getNick() + " вошёл в чат");
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        System.out.println(clientHandler.getNick() + " покинул чат");
        clients.remove(clientHandler.getNick());
        broadcastClientsList();
    }

    private void broadcastClientsList() {
        String nicks = clients.values().stream()
                .map(ClientHandler::getNick)
                .collect(Collectors.joining(" "));
        broadcastMsg(Command.CLIENTS, nicks);
    }


    public synchronized void close() {
        for (ClientHandler handler : clients.values()) {
            handler.closeConnection();
        }
        System.out.println("Сервер успешно прекратил работу");

    }


    public void sendPrivateMsg(ClientHandler from, String nickTo, String message) {
        ClientHandler clientTo = clients.get(nickTo);
        if (clientTo == null) {
            from.sendMsg(Command.ERROR, "Пользователь не авторизован");
            return;
        }
        clientTo.sendMsg(Command.MESSAGE, "От " + from.getNick() + ":" + message);
        from.sendMsg(Command.MESSAGE, "Участнику " + nickTo + ":" + message);
    }
}
