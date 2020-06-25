module geekbrainsChat.client {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires java.sql;
    requires sqlite.jdbc;

    opens geekbrainsChat.client;
}