module ru.starstreet.simplechat {
    requires javafx.controls;
    requires javafx.fxml;


    opens ru.starstreet.simplechat to javafx.fxml;
    exports ru.starstreet.simplechat;
    exports ru.starstreet.simplechat.server;
    opens ru.starstreet.simplechat.server to javafx.fxml;
    exports ru.starstreet.simplechat.client;
    opens ru.starstreet.simplechat.client to javafx.fxml;

}