package ru.starstreet.simplechat.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatStore implements HistoryService {
    private final Path path;

    public ChatStore(Path path) {
        this.path = path;
    }

    @Override
    public String getHistoryString() {
        String history;
        try {
            history = Files.readString(path);
        } catch (IOException e) {
            history = "There is no history here";
        }
        return history;
    }

    @Override
    public String getHistoryString(int lastRowsAmount) {
        String history;
        try{
            List<String> allRows = Files.readAllLines(path);
            StringBuilder sb = new StringBuilder();
            int startIndex = Math.max(allRows.size() - lastRowsAmount, 0);
            for (int i = startIndex; i < allRows.size(); i++) {
                sb.append(allRows.get(i));
                sb.append("\n");
            }
            history = sb.toString();
        }
        catch (IOException e){
            history = "There is no history here";
        }
        return history;
    }

    @Override
    public void addNewMessage(String msg) {
        try {
            if (!path.toFile().exists()){
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            }
            LocalDateTime now =  LocalDateTime.now();
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String date = now.format(format);
            Files.writeString(path, date + ": " + msg + "\n", StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
