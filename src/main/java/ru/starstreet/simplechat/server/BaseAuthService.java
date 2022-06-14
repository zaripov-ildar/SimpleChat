package ru.starstreet.simplechat.server;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BaseAuthService implements AuthService {
    private static class UserData {
        private String login;
        private String pass;
        private String nick;

        private UserData(String login, String pass, String nick) {
            this.login = login;
            this.pass = pass;
            this.nick = nick;
        }
    }

    private final List<UserData> entries;

    public BaseAuthService() {
        entries = new ArrayList<>();
        entries.add(new UserData("login1", "pass1", "nick1"));
        entries.add(new UserData("login2", "pass2", "nick2"));
        entries.add(new UserData("login3", "pass3", "nick3"));
    }

    @Override
    public void start() {
        System.out.println("Сервис аутентификации запущен");
    }

    @Override
    public String getNickByLoginPass(String login, String pass) {
        for (UserData userData : entries) {
            if (userData.login.equals(login) && userData.pass.equals(pass)) {
                return userData.nick;
            }
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        System.out.println("Сервис аутентификации остановлен");
    }
}
