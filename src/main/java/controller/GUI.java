package controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;

public class GUI extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        File iconFile = new File("src/main/resources/icons/ChessIcon.jpg");
        Image icon = new Image(iconFile.toURI().toString());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("ChessGameSmall.fxml"));
        Parent root = loader.load();

        ChessGameController gameController = loader.getController();
        Scene gameScene = new Scene(root);
        gameController.init();

        stage.setOnCloseRequest((WindowEvent event) -> {
            gameController.closeStockfish();
        });

        stage.setTitle("ChessFX");
        stage.getIcons().add(icon);
        stage.setScene(gameScene);
        stage.setResizable(false);
        stage.requestFocus();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
