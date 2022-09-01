package ru.starstreet.simplechat.server;

import java.io.Closeable;

public interface AuthService extends Closeable {
    void start();

    String getNickByLoginPass(String login, String pass);

    void setNick(String oldNick, String newNick);
}
