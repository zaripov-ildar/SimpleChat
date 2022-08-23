package ru.starstreet.simplechat.server;

public interface HistoryService {
    String getHistoryString();
    String getHistoryString(int lastRowsAmount);
    void addNewMessage(String msg);
}
