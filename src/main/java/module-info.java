module com.example.chessfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.datatransfer;
    requires java.desktop;


    opens controller to javafx.fxml;
    exports controller;
}