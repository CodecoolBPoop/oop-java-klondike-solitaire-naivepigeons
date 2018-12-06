package com.codecool.klondike;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

public class Klondike extends Application {

    private static final double WINDOW_WIDTH = 1500;
    private static final double WINDOW_HEIGHT = 900;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Card.loadCardImages();
        Game game = new Game();
        game.setTableBackground(new Image("/table/green.png"));

        newButtons(game, primaryStage);

        primaryStage.setTitle("Klondike Solitaire");
        primaryStage.setScene(new Scene(game, WINDOW_WIDTH, WINDOW_HEIGHT));
        primaryStage.show();
    }

    private void newButtons(Game game, Stage stage) {
        Image restartImage = new Image("button_images/restart.png");

        Button restartButton = new Button("");
        restartButton.setGraphic(new ImageView(restartImage));

        restartButton.setLayoutX(10);
        restartButton.setLayoutY(10);

        restartButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                restart(stage);
            }
        });

        game.getChildren().add(restartButton);
    }

    private void restart(Stage stage) {
        start(stage);
    }

}

