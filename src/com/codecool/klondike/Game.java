package com.codecool.klondike;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.util.*;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;


public class Game extends Pane {

    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();

    private static double STOCK_GAP = 1;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;


    private void shuffleDeck() {
        Collections.shuffle(deck);
    }


    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK && card.equals(stockPile.getTopCard())) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
        }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();
        Pile activePile = card.getContainingPile();
        int cardIndex = activePile.getCardIndex(card);
        Pile.PileType pileType = activePile.getPileType();

        if (pileType == Pile.PileType.STOCK) {
            return;
        }
        if (pileType == Pile.PileType.DISCARD && !card.equals(card.getContainingPile().getTopCard())) {
            return;
        }
        if (pileType == Pile.PileType.TABLEAU && card.isFaceDown()) {
            return;
        }
        if (activePile.getPileType() == Pile.PileType.FOUNDATION && !card.equals(card.getContainingPile().getTopCard())) {
            return;
        }
        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;

        draggedCards.clear();
        draggedCards.add(card);
        int topCardIndex = activePile.getTopCardIndex();
        if (pileType == Pile.PileType.TABLEAU && cardIndex != topCardIndex) {
            for (int i = cardIndex + 1; i < activePile.numOfCards(); i++) {
                draggedCards.add(activePile.getCard(i));
            }
        }

        for (Card draggedCard : draggedCards) {
            draggedCard.getDropShadow().setRadius(20);
            draggedCard.getDropShadow().setOffsetX(10);
            draggedCard.getDropShadow().setOffsetY(10);

            draggedCard.toFront();
            draggedCard.setTranslateX(offsetX);
            draggedCard.setTranslateY(offsetY);
        }
    };

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();
        List<Pile> validDropPiles = FXCollections.observableArrayList();
        validDropPiles.addAll(tableauPiles);
        validDropPiles.addAll(foundationPiles);
        Pile pile = getValidIntersectingPile(card, validDropPiles);
        if (pile != null) {
            handleValidMove(card, pile);
        } else {
            draggedCards.forEach(MouseUtil::slideBack);
            draggedCards.clear();
        }

    };

    private boolean areTableauPilesEmpty() {
        for (Pile pile : tableauPiles) {
            if (!pile.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean isGameWon() {
        if (stockPile.isEmpty() && discardPile.isEmpty() && areTableauPilesEmpty()) {
            System.out.println("Congratulations, you won!");
            return true;
        }
        /*
        for (Pile pile : foundationPiles){
            if (!pile.isEmpty()){
                return true;
            }
        }
        */
        return false;
    }

    public void checkAndHandleGameWon() {
        if (isGameWon()) {
            Popup popup = new Popup();
            popup.display();
        }
    }

    public Game() {
        deck = Card.createNewDeck();
        shuffleDeck();
        initPiles();
        dealCards();
        flipTopTableauCards();
    }

    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void refillStockFromDiscard() {
        for (int i = discardPile.numOfCards() - 1; i >= 0; i--) {
            discardPile.getCards().get(i).flip();
            discardPile.getCards().get(i).moveToPile(stockPile);
        }
        System.out.println("Stock refilled from discard pile.");
    }

    public boolean isMoveValid(Card card, Pile destPile) {
        if (destPile.getPileType().equals(Pile.PileType.FOUNDATION) && draggedCards.size() == 1) {
            Card topCard = destPile.getTopCard();

            if (topCard == null && card.getRank() == 1) {
                return true;
            } else if (topCard == null) {
                return false;
            } else if (topCard.getSuit() == card.getSuit() && topCard.getRank() + 1 == card.getRank()) {
                return true;
            }
        } else if (destPile.getPileType().equals(Pile.PileType.TABLEAU)) {
            Card topCard = destPile.getTopCard();

            // if there's no top card and only KING
            if (topCard == null && card.getRank() == 13) {
                return true;
            } else if (topCard == null) {
                return false;
            }
            // if diff color AND rank is +1
            else if (Card.isOppositeColor(card, topCard) && topCard.getRank() == (card.getRank() + 1)) {
                return true;
            }
        }
        return false;
    }

    private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = null;
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) &&
                    isOverPile(card, pile) &&
                    isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to a new pile.", card);
        } else {
            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
        }
        System.out.println(msg);
        //autoFlipTableauTops(card);
        MouseUtil.slideToDest(draggedCards, destPile, this);
        draggedCards.clear();
    }



    public void autoFlipTableauTops(Card card, Pile original) {
        if (!original.isEmpty() &&
                original.getPileType().equals(Pile.PileType.TABLEAU) && original.getTopCard().isFaceDown()) {
            flipTopCard(original);
        }
    }


    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    public void dealCards() {
        Iterator<Card> deckIterator = deck.iterator();
        int cardsToAdd = 1;
        for (Pile pile : tableauPiles) {
            for (int i = 0; i < cardsToAdd; i++) {
                Card card = deckIterator.next();
                pile.addCard(card);
                card.setContainingPile(pile);
                addMouseEventHandlers(card);
                getChildren().add(card);
            }
            cardsToAdd++;
        }
        deckIterator.forEachRemaining(card -> {
            stockPile.addCard(card);
            card.setContainingPile(stockPile);
            addMouseEventHandlers(card);
            getChildren().add(card);
        });

    }

    public void flipTopCard(Pile pile) {
        if (!pile.isEmpty()) {
            Card topCard = pile.getTopCard();
            topCard.flip();
        }
    }

    public void flipTopTableauCards() {
        for (Pile pile : tableauPiles) {
            if (!pile.isEmpty()) {
                flipTopCard(pile);
            }
        }
    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }


    public void makeThemeSwitcher() {
        ObservableList<String> options = FXCollections.observableArrayList("Basic Theme", "Hippi Theme", "Pokemon Theme");
        ComboBox comboBox = new ComboBox(options);
        comboBox.setPromptText("Switch Theme");
        comboBox.setLayoutX(1330);
        comboBox.setLayoutY(140);
        comboBox.setId("themeSwitcher");
        getChildren().add(comboBox);
        final int[] newThemeNr = {1};
        comboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (newValue.equals("Basic Theme")) {
                    newThemeNr[0] = 1;
                } else if (newValue.equals("Hippi Theme")) {
                    newThemeNr[0] = 2;
                } if (newValue.equals("Pokemon Theme")) {
                    newThemeNr[0] = 3;
                }
                setTableBackground(new Image("/table/"+ newThemeNr[0] + ".png"));
            }
        });

    }




    public class Popup {

        public void display() {
            Stage popupwindow = new Stage();

            popupwindow.initModality(Modality.APPLICATION_MODAL);
            popupwindow.setTitle("Congratulations!");

            Label label1 = new Label("You won!!!");

            label1.setFont(new Font("Times", 30));

            Button newGameButton = new Button("Close");

            newGameButton.setOnAction(e -> popupwindow.close());

            VBox layout = new VBox(50);

            layout.getChildren().addAll(label1, newGameButton);

            layout.setAlignment(Pos.CENTER);

            Scene popupScene = new Scene(layout, 250, 200);

            popupwindow.setScene(popupScene);

            popupwindow.show();

        }
    }
}
