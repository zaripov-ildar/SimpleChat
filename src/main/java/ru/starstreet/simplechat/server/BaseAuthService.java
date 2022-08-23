package ru.starstreet.simplechat.server;


import java.io.IOException;
import java.sql.*;

public class BaseAuthService implements AuthService {
    private Connection connection;
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

    public BaseAuthService() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/ChatClients.db");
        } catch (SQLException e) {
            System.out.println("something wrong with connection");
            e.printStackTrace();
        }
    }

    @Override
    public void start() {

        System.out.println("Сервис аутентификации запущен");
    }

    @Override
    public String getNickByLoginPass(String login, String pass) {
        try {
            Statement statement = connection.createStatement();
            String format = String.format("SELECT nick FROM auth WHERE login = '%s' and password = '%s';", login, pass);
            ResultSet resultSet = statement.executeQuery(format);
            return resultSet.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setNick(String oldNick, String newNick) {
        try {
            Statement statement = connection.createStatement();
            String query = String.format("UPDATE auth SET nick = '%s' WHERE nick = '%s';", newNick, oldNick);
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        if (connection!=null){
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Сервис аутентификации остановлен");
    }
}
