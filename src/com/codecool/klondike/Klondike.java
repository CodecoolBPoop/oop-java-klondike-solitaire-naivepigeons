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
    private static int themeNr = 3;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Image cardback = Card.getCardBackImage(themeNr);
        Card.loadCardImages(cardback);
        Game game = new Game(this);
        game.setTableBackground(new Image("/table/" + themeNr + ".png"));
        game.makeThemeSwitcher();
        stage = primaryStage;
        newButtons(game);
        stage.setTitle("Klondike Solitaire");
        stage.setScene(new Scene(game, WINDOW_WIDTH, WINDOW_HEIGHT));
        stage.show();
    }

    private void newButtons(Game game) {
        Image restartImage = new Image("button_images/restart.png");
        Image undoImage = new Image("button_images/undo.png");

        Button restartButton = new Button("");
        restartButton.setGraphic(new ImageView(restartImage));
        restartButton.setLayoutX(1375);
        restartButton.setLayoutY(50);

        restartButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                restart();
            }
        });

        Button undoButton = new Button("");
        undoButton.setGraphic(new ImageView(undoImage));
        undoButton.setLayoutX(490);
        undoButton.setLayoutY(50);

        undoButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                game.undoLastMove();
            }
        });

        game.getChildren().add(restartButton);
        game.getChildren().add(undoButton);

    }

    public void restart() {
        start(stage);
    }

    public void setTheme(int themeNr) {
        this.themeNr = themeNr;
    }

}

