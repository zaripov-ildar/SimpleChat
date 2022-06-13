package ru.starstreet.simplechat.server;


import java.util.ArrayList;
import java.util.List;

public class BaseAuthService implements AuthService {
    private class Entry {
        private String login;
        private String pass;
        private String nick;

        private Entry(String login, String pass, String nick) {
            this.login = login;
            this.pass = pass;
            this.nick = nick;
        }
    }
    private List<Entry> entries;

    public BaseAuthService() {
        entries = new ArrayList<>();
        entries.add(new Entry("login1", "pass1", "nick1"));
        entries.add(new Entry("login2", "pass2", "nick2"));
        entries.add(new Entry("login3", "pass3", "nick3"));
    }

    @Override
    public void start() {
        System.out.println("Сервис аутентификации запущен");
    }

    @Override
    public String getNickByLoginPass(String login, String pass) {
        for (Entry entry: entries){
            if(entry.login.equals(login) && entry.pass.equals(pass)) return entry.nick;
        }
        return null;
    }

    @Override
    public void stop() {
        System.out.println("Сервис аутентификации остановлен");
    }
}
