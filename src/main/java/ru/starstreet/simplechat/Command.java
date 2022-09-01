package ru.starstreet.simplechat;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Command {
    AUTH("/auth") {
        @Override
        public String[] parse(String commandMessage) {
            String[] split = commandMessage.split(TOKEN_DELIMITER);
            if (split.length !=3) {
                return new String[]{"",""};
            }
            return new String[]{split[1], split[2]};
        }
    },
    AUTH_OK("/auth_ok") {
        @Override
        public String[] parse(String commandMessage) {
            String[] split = commandMessage.split(TOKEN_DELIMITER);
            return new String[]{split[1]};
        }
    },
    END("/end") {
        @Override
        public String[] parse(String commandMessage) {
            return new String[0];
        }
    },
    PRIVATE_MESSAGE("/private_message") {
        @Override
        public String[] parse(String commandMessage) {
            String[] split = commandMessage.split(TOKEN_DELIMITER, 3);
            return new String[]{split[1], split[2]};
        }
    },
    CLIENTS("/clients") {
        @Override
        public String[] parse(String commandMessage) {
            String[] split = commandMessage.split(TOKEN_DELIMITER);
            final String[] nicks = new String[split.length - 1];
            for (int i = 0; i < nicks.length; i++) {
                nicks[i] = split[i + 1];
            }
            return nicks;
        }
    },
    ERROR("/error") {
        @Override
        public String[] parse(String commandMessage) {
            String[] split = commandMessage.split(TOKEN_DELIMITER, 2);
            return new String[]{split[1]};
        }
    },
    MESSAGE("/message") {
        @Override
        public String[] parse(String commandMessage) {
            String[] split = commandMessage.split(TOKEN_DELIMITER, 2);
            return new String[]{split[1]};
        }
    },
    DISCONNECT("/disconnect"){
        @Override
        public String[] parse(String commandMessage) {
            return new String[0];
        }
    },
    CHANGE_NICK("/changeNick"){
        @Override
        public String[] parse(String commandMessage) {
            String[] split = commandMessage.split(TOKEN_DELIMITER, 2);
            return new String[]{split[1]};
        }
    },
    HISTORY("/|history"){
        @Override
        public String[] parse(String commandMessage) {
            String[] split = commandMessage.split(TOKEN_DELIMITER, 2);
            return new String[]{split[1]};
        }
    };

    private final String command;
    static final String TOKEN_DELIMITER = "\\p{Blank}+";
    static final Map<String, Command> commandMap = Arrays.stream(values())
            .collect(Collectors.toMap(Command::getCommand, Function.identity()));


    Command(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public static boolean isCommand(String message) {
        return message.startsWith("/");
    }

    public static Command getCommand(String message) {
        if (!isCommand(message)) {
            throw new RuntimeException("'" + message + "' is not a command");
        }
        final String cmd = message.split(TOKEN_DELIMITER, 2)[0];
        Command command = commandMap.get(cmd);
        if (command == null) {
            throw new RuntimeException("Unknown command '" + cmd + "");
        }
        return command;
    }

    public abstract String[] parse(String commandMessage);

    public String collectMessages(String... params) {
        return this.command + " " + String.join(" ", params);
    }
}

