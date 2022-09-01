module ru.starstreet.simplechat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.slf4j;

    exports ru.starstreet.simplechat.client;
    opens ru.starstreet.simplechat.client to javafx.fxml;

}