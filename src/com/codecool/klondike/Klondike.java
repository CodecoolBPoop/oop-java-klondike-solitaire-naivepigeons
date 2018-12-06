package com.codecool.klondike;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

public class Klondike extends Application {

    private static final double WINDOW_WIDTH = 1500;
    private static final double WINDOW_HEIGHT = 900;
    private static Stage stage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        int themeNr = 1;
        Image cardback = Card.getCardBackImage(themeNr);
        System.out.println(cardback);
        Card.loadCardImages(cardback);
        Game game = new Game();
        game.setTableBackground(new Image("/table/" + themeNr + ".png"));
        game.makeThemeSwitcher();

        stage = primaryStage;
        newButtons(game, stage);
        stage.setTitle("Klondike Solitaire");
        stage.setScene(new Scene(game, WINDOW_WIDTH, WINDOW_HEIGHT));
        stage.show();
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
                restart();
            }
        });

        game.getChildren().add(restartButton);
    }

    public void restart() {
        start(stage);
    }

}

