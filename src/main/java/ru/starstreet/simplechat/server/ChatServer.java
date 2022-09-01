package ru.starstreet.simplechat.server;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger log = LoggerFactory.getLogger(ChatServer.class);

    public AuthService getAuthService() {
        return authService;
    }

    public ChatServer() {
        this.clients = new HashMap<>();
    }

    public void run() {
        try (ServerSocket server = new ServerSocket(PORT);
             AuthService authService = new BaseAuthService(log)) {
            this.authService = authService;
            log.info("Сервер запущен");
            while (true) {
                final Socket socket = server.accept();
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            log.error("Произошла ошибка в работе сервера:\n" + e.getMessage());
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
        log.info("Клиент " + clientHandler.getNick() + " подключился");
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        log.info(clientHandler.getNick() + " покинул чат");
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

    public void setNick(ClientHandler clientHandler, String newNick) {
        if (!isNickBusy(newNick)) {
            authService.setNick(clientHandler.getNick(), newNick);
            broadcastMsg(Command.MESSAGE, clientHandler.getNick() + " сменил ник на " + newNick);
            clients.remove(clientHandler.getNick());
            clientHandler.setNick(newNick);
            clients.put(newNick, clientHandler);
            broadcastClientsList();
        }
    }

    public void log(ClientHandler client, String str) {
        log.trace("Клиент" + client.getNick() + "прислал сообщение/команду: " + str);
    }
}
