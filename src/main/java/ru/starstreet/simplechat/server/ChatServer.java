package ru.starstreet.simplechat.server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    private final List<ClientHandler> clients;
    private final int PORT = 8189;
    private AuthService authService;

    public AuthService getAuthService() {
        return authService;
    }

    public ChatServer() {
        this.clients = new ArrayList<>();
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
        for (ClientHandler handler : clients) {
            if (handler.getNick().equals(nick))
                return true;
        }
        return false;
    }

    public synchronized void broadcastMsg(String s) {
        for (ClientHandler handler : clients) {
            handler.sendMsg(s);
        }
    }

    public synchronized void sendPrivateMsg(String receiverName, String msg) {
        for (ClientHandler handler : clients) {
            if (handler.getNick().equals(receiverName))
                handler.sendMsg(msg);
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        System.out.println(clientHandler.getNick() + " вошёл в чат");
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        System.out.println(clientHandler.getNick() + " покинул чат");
        clients.remove(clientHandler);
    }

    public synchronized void close() {
        for (ClientHandler handler : clients) {
            handler.closeConnection();
        }
        System.out.println("Сервер успешно прекратил работу");

    }


}
