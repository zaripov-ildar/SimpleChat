module ru.starstreet.simplechat {
    requires javafx.controls;
    requires javafx.fxml;

    exports ru.starstreet.simplechat.client;
    opens ru.starstreet.simplechat.client to javafx.fxml;

}